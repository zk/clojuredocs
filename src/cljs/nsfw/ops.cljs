(ns nsfw.ops
  "Provides message-based dispatching and context sharing. This helps
  with decoupling disparate parts of an app while sharing a common
  context (e.g. app state, windows, connections) between those parts."
  (:require [nsfw.util :as util]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take!
                     alts! timeout pipe mult tap]]
            [cljs.core.async.impl.protocols])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defprotocol Dispatcher
  (send [this op] [this op data])
  (bind! [this kw->f])
  (unbind! [this kws])
  (set-ctx! [this ctx])
  (set-debug! [this id f])
  (clear-debug! [this id]))

(defn bus [context handlers & [{:keys [debug?]}]]
  (let [!handlers (atom handlers)
        !ctx (atom context)
        !debug-fns (atom {})
        bus (reify
              Dispatcher
              (send [this op]
                (send this op nil))
              (send [this op data]
                (when-let [msg {::op op ::data data}]
                  (let [op (or (::op msg) (:op msg))]
                    (when debug?
                      (println "[nsfw.ops] Dispatching " op))
                    (if-let [f (get @!handlers op)]
                      (do
                        (f
                          (merge {:bus this}
                                 @!ctx)
                          (::data msg)))
                      (println "[nsfw.ops] No handler for op" msg))
                    (when-not (empty? @!debug-fns)
                      (doseq [f (vals @!debug-fns)]
                        (f op))))))
              (bind! [_ kw->f]
                (swap! !handlers merge kw->f))
              (unbind! [_ kws]
                (swap! !handlers
                  #(apply dissoc % kws)))
              (set-ctx! [_ ctx]
                (reset! !ctx ctx))
              (set-debug! [_ id f]
                (swap! !debug-fns assoc id f))
              (clear-debug! [_ id]
                (swap! !debug-fns dissoc id)))]
    bus))

(defn data [op]
  (::data op))

(defn op [{:keys [op op-id data on-ack on-error auth]}]
  {::op op
   ::data data
   ::op-id (or op-id (util/uuid))
   ::on-ack on-ack
   ::auth auth
   ::on-error on-error})

(defn wrap-with-state
  ([with-state]
   (fn [h]
     (wrap-with-state h with-state)))
  ([h with-state & args]
   (fn [state params ctx]
     (let [res (h state params ctx)]
       (cond
         (map? res) (do (with-state res) res)
         (vector? res) (let [[state ch] res]
                         (with-state state)
                         [state
                          (async/map
                            (fn [state]
                              (with-state state)
                              state)
                            [ch])]))))))


(defn apply-state [!state state params]
  (let [state' (cond
                 (fn? state) (state @!state)
                 :else state)]
    (reset! !state state')))

(defn chan? [c]
  (satisfies?
    cljs.core.async.impl.protocols/Channel
    c))

(defn handle-step [res params !state ctx]
  (cond
    (chan? res) (go-loop []
                  (when-let [res (<! res)]
                    (handle-step
                      res
                      params
                      !state
                      ctx)
                    (recur)))
    (vector? res) (let [[res ch] res]
                    (go-loop [ch ch]
                      (when ch
                        (let [[res ch] (<! ch)]
                          (when res
                            (handle-step
                              res
                              params
                              !state
                              ctx))
                          (when ch
                            (recur ch)))))
                    (handle-step
                      res
                      params
                      !state
                      ctx))
    (fn? res) (recur
                (res @!state params ctx)
                params
                !state
                ctx)

    :else (when res (reset! !state res))))

(defn gen-lock-step [handler]
  (fn [{:keys [!state] :as ctx} params]
    (handle-step handler params !state ctx)))

(defn wrap-kit-handlers [handlers]
  (->> handlers
       (map (fn [[k v]]
              [k (gen-lock-step v)]))
       (into {})))

(defn kit [!state ctx handlers & [opts]]
  (bus
    (assoc ctx :!state !state)
    (wrap-kit-handlers handlers)
    opts))
