(ns clojuredocs.mods.var-page
  (:require [dommy.core :as dommy :refer-macros [sel sel1]]
            [reagent.core :as rea]
            [nsfw.ops :as ops]
            [nsfw.page :as page]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.util :as util]
            [clojuredocs.examples :as examples]
            [clojuredocs.see-alsos :as see-alsos]
            [clojuredocs.notes :as notes]
            [clojuredocs.anim :as anim]
            [clojuredocs.ajax :refer [ajax]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn ajax-chan [opts]
  (let [c (chan)]
    (ajax
      (merge
        opts
        {:success (fn [res]
                    (put! c {:success true :res res}))
         :error (fn [res]
                  (put! c {:success false :res res}))}))
    c))

(defn $nav [!state]
  (let [{:keys [examples see-alsos notes]} @!state]
    [:div
     [:h5 "Nav"]
     [:ul
      [:li [:a {:href "#"
                :data-animate-scroll "true"
                :data-animate-buffer "20"}
            "Top"]]
      [:li [:a {:href "#examples"
                :data-animate-scroll "true"
                :data-animate-buffer "20"}
            "Examples "
            [:span.badge (count examples)]]]
      [:li [:a {:href "#see-also"
                :data-animate-scroll "true"
                :data-animate-buffer "10"}
            "See Also "
            [:span.badge (count see-alsos)]]]
      (when (> (count notes) 0)
        [:li [:a {:href "#notes"
                  :data-animate-scroll "true"
                  :data-animate-buffer "10"}
              "Notes"
              [:span.badge (count notes)]]])]]))

(defn req-create-example [ex]
  (let [ch (chan)]
    (ajax
      {:path "/api/examples"
       :method :post
       :data-type :edn
       :data ex
       :success (fn [{:keys [body] :as resp}]
                  (put! ch {:success true :data body}))
       :error (fn [{:keys [status body]}]
                (prn "error creating example" status body)
                (put! ch {:success false
                          :error
                          (condp = status
                            401 "You must be logged in to add an example"
                            "Unknown server error")}))})
    ch))

(defn init-state []
  (merge
    {:examples []
     :vars []
     :add-example {}
     :add-see-also {}
     :add-note {}
     :user nil}
    (util/page-data!)))

(defn update-example [{:keys [examples] :as state} _id f & args]
  (assoc state
    :examples (->> examples
                   (map (fn [ex]
                          (if (= _id (:_id ex))
                            (apply f ex args)
                            ex)))
                   vec)))

(defn merge-with-ex [state _id & maps]
  (update-example
    state
    _id
    #(apply merge % maps)))

(defn req-update-example [_id body]
  (let [c (chan)]
    (ajax
      {:method :patch
       :path (str "/api/examples/" _id)
       :data-type :edn
       :data {:body body}
       :success (fn [{:keys [body] :as resp}]
                  (put! c {:success true :data body}))
       :error (fn [{:keys [status body] :as r}]
                (put! c
                  (merge
                    {:success false}
                    (condp = status
                      422 body
                      401 body
                      {:message "Unknown server error"}))))})
    c))

(defn handle-new-example [state {:keys [text _id]}]
  (let [var (select-keys (:var state) [:ns :name :library-url])
        ch (chan)]
    (go
      (if (empty? text)
        (put! ch #(-> %
                      (assoc-in
                        [:add-example :error]
                        "Please provide an example body")))
        (let [{:keys [success error data]}
              (<! (req-create-example {:body text
                                       :var var}))]
          (put! ch
            (fn [state]
              (if success
                (-> state
                    (assoc :add-example {:text nil :body nil})
                    (assoc :examples (vec (concat (:examples state) [data]))))
                (-> state
                    (update-in [:add-example] assoc :error error)))))))
      (close! ch))
    ch))

(defn handle-update-example [state {:keys [text _id]}]
  (let [ch (chan)]
    (put! ch (merge-with-ex state _id {:loading? true}))
    (go
      (let [res (<! (req-update-example _id text))]
        (put! ch
          (fn [state]
            (if (:success res)
              (merge-with-ex state _id
                (:data res)
                {:loading? false :editing? false})

              (merge-with-ex state
                {:error (:message res)
                 :loading? false})))))
      (close! ch))
    ch))

(defn handle-save-example [state {:keys [_id] :as pl}]
  (if _id
    (handle-update-example state pl)
    (handle-new-example state pl)))

(defn update-sa [{:keys [see-alsos] :as state} _id f]
  (assoc state
    :see-alsos (->> see-alsos
                    (map (fn [ex]
                           (if (= _id (:_id ex))
                             (f ex)
                             ex)))
                    vec)))

(defn update-note [{:keys [notes] :as state} _id f & args]
  (assoc state
    :notes (->> notes
                (map (fn [ex]
                       (if (= _id (:_id ex))
                         (apply f ex args)
                         ex)))
                vec)))

(defn req-delete-example [_id]
  (let [c (chan)]
    (ajax
      {:method :delete
       :path (str "/api/examples/" _id)
       :success (fn [{:keys [body] :as resp}]
                  (put! c {:success true :data body}))
       :error (fn [{:keys [status body] :as r}]
                (put! c {:success false :data body}))})
    c))

(defn init-examples [$root !state]
  (let [bus (ops/kit
              !state
              {}
              {:clojuredocs.examples/delete
               (fn [state _id]
                 (let [ch (chan)]
                   (go
                     (put! ch #(merge-with-ex % _id
                                 {:delete-state :loading}))
                     (let [{:keys [success data]} (<! (req-delete-example _id))]
                       (put! ch (fn [state]
                                  (if success
                                    (update-in
                                      state
                                      [:examples]
                                      (fn [es] (vec (remove #(= _id (:_id %)) es))))
                                    (merge-with-ex
                                      state
                                      _id
                                      {:delete-state :error})))))
                     (close! ch))
                   ch))
               :clojuredocs.examples/save handle-save-example})]

    (rea/render-component
      [examples/$examples-widget !state bus]
      (sel1 $root :.examples-widget))))

(defn update-new-note [state f & args]
  (update-in
    state
    [:add-note]
    #(apply f % args)))

(defn handle-new-note [state text]
  (let [user (:user state)
        ch (chan)]
    (go
      (put! ch #(update-new-note
                  state
                  merge
                  {:error nil
                   :loading? false}))
      (if (empty? text)
        (put! ch #(update-new-note
                    state
                    merge
                    {:error "Whoops, looks like your note is empty."}))
        (do
          (put! ch #(update-new-note
                      state
                      merge
                      {:loading? true}))
          (let [{:keys [success res]}
                (<! (ajax-chan
                      {:method :post
                       :path "/api/notes"
                       :data-type :edn
                       :data {:body text
                              :var (select-keys (:var state) [:ns :name :library-url])}}))]
            (put! ch (fn [state]
                       (if success
                         (-> state
                             (update-in [:notes]
                               #(vec (concat % [(:body res)])))
                             (update-new-note
                               merge
                               {:text nil
                                :loading? false
                                :expanded? false}))
                         (-> state
                             (update-new-note
                               merge
                               {:loading? false
                                :error (-> res :body :message)}))))))))
      (close! ch))
    ch))

(defn handle-update-note [state {:keys [_id text]}]
  (let [ch (chan)]
    (go
      (put! ch #(update-note % _id assoc :error nil))
      (if (empty? text)
        (put! ch #(update-note % _id assoc :error "Whoops, looks like your note is empty."))
        (do
          (put! ch #(update-note % _id assoc :loading? true))

          (let [{:keys [success res]}
                (<! (ajax-chan
                      {:method :patch
                       :path (str "/api/notes/" _id)
                       :data-type :edn
                       :data {:body text}}))]
            (put! ch #(if success
                        (update-note % _id
                          (constantly (merge
                                        (:body res)
                                        {:error nil
                                         :loading? false})))
                        (update-note % _id
                          merge
                          {:error (-> res :body :message)
                           :loading? false}))))))
      (close! ch))
    ch))

(defn handle-delete-note [state _id]
  (let [ch (chan)]
    (go
      (put! ch #(update-note % _id merge {:delete-state :loading}))
      (let [{:keys [success re]}
            (<! (ajax-chan
                  {:method :delete
                   :path (str "/api/notes/" _id)
                   :data-type :edn}))]
        (if success
          (put! ch (fn [state]
                     (if success
                       (update-in
                         state
                         [:notes]
                         (fn [notes]
                           (->> notes
                                (remove #(= _id (:_id %)))
                                vec)))
                       (update-note
                         state
                         _id
                         assoc :delete-state :error)))))))
    ch))

(defn init-notes [$root !state]
  (let [bus (ops/kit
              !state
              {}
              {:clojuredocs.notes/new handle-new-note
               :clojuredocs.notes/update handle-update-note
               :clojuredocs.notes/delete handle-delete-note})]
    (rea/render-component
      [notes/$notes !state bus]
      (sel1 $root :.notes-widget))))

(defn format-ac-results [current-var see-alsos ac-results]
  (let [existing (->> see-alsos
                      (map :to-var)
                      set)]
    (->> ac-results
         (map (fn [ac-result]
                (cond
                  (= (select-keys current-var [:ns :name :library-url])
                     (select-keys ac-result [:ns :name :library-url]))
                  (merge ac-result
                         {:disabled? true
                          :disabled-text "This Var"})

                  (get existing (select-keys ac-result [:ns :name :library-url]))
                  (merge ac-result
                         {:disabled? true
                          :disabled-text "Already Exists"})

                  :else ac-result))))))

(defn handle-sa-ac-text [state ac-text]
  (let [ch (chan)]
    (put! ch #(assoc-in % [:add-see-also :completing?] true))
    (go
      (let [{:keys [success res]}
            (<! (ajax-chan
                  {:method :get
                   :path (str "/ac-vars?query=" (util/url-encode ac-text))
                   :data-type :edn}))]
        (put! ch
          (fn [state]
            (-> (if success
                  (-> state
                      (assoc-in
                        [:add-see-also :ac-results]
                        (format-ac-results
                          (:var state)
                          (:see-alsos state)
                          (vec (:body res))))
                      (assoc-in
                        [:add-see-also :error]
                        nil))
                  (assoc-in state
                    [:add-see-also :error]
                    (-> res :body :error)))
                (assoc-in [:add-see-also :completing?] false)))))
      (close! ch))
    ch))

(defn handle-sa-create [state var]
  (let [ch (chan)]
    (put! ch #(assoc-in % [:add-see-also :loading?] true))
    (go
      (let [{:keys [success res]}
            (<! (ajax-chan
                  {:method :post
                   :path "/api/see-alsos"
                   :data-type :edn
                   :data {:fq-to-var-name (str (:ns var)
                                               "/"
                                               (:name var))
                          :from-var (select-keys (:var state) [:ns :name :library-url])}}))]
        (put! ch
          (fn [state]
            (if success
              (-> state
                  (update-in [:see-alsos] #(vec (concat % [(:body res)])))
                  (assoc :add-see-also {:ac-text ""
                                        :loading? false}))
              (update-in state
                [:add-see-also]
                merge
                {:error (-> res :body :message)
                 :loading? false})))))
      (close! ch))
    ch))

(defn handle-sa-delete [state to-del]
  (let [ch (chan)]
    (put! ch #(update-sa
                state
                (:_id to-del)
                (fn [sa]
                  (assoc sa :delete-state :loading))))
    (go
      (let [{:keys [success res]}
            (<! (ajax-chan
                  {:method :delete
                   :path (str "/api/see-alsos/" (:_id to-del))
                   :data-type :edn}))]
        (put! ch
          (fn [state]
            (if success
              (update-in state
                [:see-alsos]
                (fn [sas]
                  (->> sas
                       (remove #(= (:_id %) (:_id to-del)))
                       vec)))
              (update-sa
                state
                (:_id to-del)
                (fn [sa]
                  (assoc sa :delete-state :error)))))))
      (close! ch))
    ch))

(defn init-see-alsos [$root !state]
  (let [bus (ops/kit !state
              {}
              {:clojuredocs.see-alsos/ac-text handle-sa-ac-text
               :clojuredocs.see-alsos/create handle-sa-create
               :clojuredocs.see-alsos/delete handle-sa-delete})]
    (rea/render-component
      [see-alsos/$see-alsos !state bus]
      (sel1 $root :.see-alsos-widget))))

(defn init-nav [$root !state]
  (rea/render-component
    [$nav !state]
    (sel1 $root :.var-page-nav)))

(defn init [$root]
  (let [!state (rea/atom (init-state))]
    (init-nav $root !state)
    (init-examples $root !state)
    (init-see-alsos $root !state)
    (init-notes $root !state)))
