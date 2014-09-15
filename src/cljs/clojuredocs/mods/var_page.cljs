(ns clojuredocs.mods.var-page
  (:require [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [sablono.core :as sab :refer-macros [html]]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.util :as util]
            [clojuredocs.examples :as examples]
            [clojuredocs.see-alsos :as see-alsos]
            [clojuredocs.notes :as notes]
            [clojuredocs.anim :as anim]
            [clojuredocs.ajax :refer [ajax]])
  (:require-macros [dommy.macros :refer [node sel sel1]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn throttle [in ms]
  (let [c (chan)
        timer (atom nil)]
    (go-loop []
      (when-let [new-text (<! in)]
        (js/clearTimeout @timer)
        (reset! timer (js/setTimeout #(put! c new-text) ms))
        (recur)))
    c))

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

(defn $nav [{:keys [examples see-alsos notes]} owner]
  (reify
    om/IRender
    (render [_]
      (html
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
                  [:span.badge (count notes)]]])]]))))

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
     :user {:login "zk" :account-source "github"}}
    (util/page-data!)))

(defn new-example-loop [!state ex-chan]
  (go-loop []
    (when-let [ex (<! ex-chan)]
      (let [ex (-> ex
                   (dissoc :_id)
                   (assoc :var (select-keys (:var @!state) [:ns :name :library-url])))]
        (swap! !state update-in [:add-example]
          (fn [m]
            (-> m
                (dissoc :error)
                (assoc :loading? true))))
        (if-let [error (when (empty? (:body ex)) "Please provide an example body")]
          (swap! !state update-in [:add-example] assoc :error error)
          (let [{:keys [success error data]} (<! (req-create-example ex))]
            (if success
              (swap! !state
                (fn [m]
                  (-> m
                      (assoc :add-example {:body ""})
                      (assoc :examples (vec (concat (:examples m) [data]))))))
              (swap! !state update-in [:add-example] assoc :error error)))))
      (swap! !state update-in [:add-example] dissoc :loading?)
      (recur))))

(defn update-example [{:keys [examples] :as state} _id f]
  (assoc state
    :examples (->> examples
                   (map (fn [ex]
                          (if (= _id (:_id ex))
                            (f ex)
                            ex)))
                   vec)))

(defn update-sa [{:keys [see-alsos] :as state} _id f]
  (assoc state
    :see-alsos (->> see-alsos
                    (map (fn [ex]
                           (if (= _id (:_id ex))
                             (f ex)
                             ex)))
                    vec)))

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

(defn update-example-loop [!state ex-chan]
  (go-loop []
    (when-let [{:keys [body _id]} (<! ex-chan)]
      (swap! !state update-example _id
        #(merge % {:loading? true}))
      (let [res (<! (req-update-example _id body))]
        (if (:success res)
          (swap! !state update-example
            _id #(merge
                   %
                   (:data res)
                   {:loading? false :editing? false}))
          (swap! !state update-example
            _id #(merge % {:error (:message res)
                           :loading? false}))))
      (recur))))

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

(defn delete-example-loop [!state delete-ch]
  (go-loop []
    (when-let [_id (<! delete-ch)]
      (swap! !state update-example
        _id #(merge % {:delete-state :loading}))
      (let [{:keys [success data]} (<! (req-delete-example _id))]
        (if success
          (swap! !state
            update-in [:examples] (fn [es] (vec (remove #(= _id (:_id %)) es))))
          (swap! !state update-example
            _id #(merge % {:delete-state :error}))))
      (recur))))

(defn init-examples [$root !state]
  (let [ex-ch (chan)
        update-example-ch (chan)
        new-example-ch (chan)
        delete-ch (chan)]

    (new-example-loop !state new-example-ch)
    (update-example-loop !state update-example-ch)
    (delete-example-loop !state delete-ch)

    (doseq [$el (sel $root :.var-page-nav)]
      (om/root
        $nav
        !state
        {:target $el}))

    (om/root
      examples/$examples
      !state
      {:target (sel1 $root :.examples-widget)
       :init-state {:new-example-ch new-example-ch
                    :update-example-ch update-example-ch
                    :delete-ch delete-ch}})

    (om/root
      notes/$notes
      !state
      {:target (sel1 $root :.notes-widget)
       :init-state {:new-ch new-ch
                    :delete-ch delete-ch}})))


(defn init-notes [$root !state])

(defn init-see-alsos [$root !state]
  (let [new-ch (chan)
        delete-ch (chan)
        ac-ch (chan)
        throttled-ac-ch (throttle ac-ch 200)] ; ugly
    (go-loop []
      (when-let [ns-name-str (<! new-ch)]
        (if (empty? ns-name-str)
          (swap! !state assoc-in [:add-see-also :error]
            "Whoops, looks like the var name is blank.")
          (do
            (swap! !state assoc-in [:add-see-also :loading?] true)
            (let [{:keys [success res]}
                  (<! (ajax-chan
                        {:method :post
                         :path "/api/see-alsos"
                         :data-type :edn
                         :data {:fq-to-var-name ns-name-str
                                :from-var (select-keys (:var @!state) [:ns :name :library-url])}}))]
              (if success
                (swap! !state (fn [m]
                                (-> m
                                    (update-in [:see-alsos] #(vec (concat % [(:body res)])))
                                    (assoc :add-see-also {:ac-text ""}))))
                (swap! !state assoc-in [:add-see-also :error] (-> res :body :message))))
            (swap! !state assoc-in [:add-see-also :loading?] false)))
        (recur)))
    (go-loop []
      (when-let [ac-text (<! throttled-ac-ch)]
        (swap! !state assoc-in [:add-see-also :completing?] true)
        (let [{:keys [success res]}
              (<! (ajax-chan
                    {:method :get
                     :path (str "/ac-vars?query=" (util/url-encode ac-text))
                     :data-type :edn}))]
          (if success
            (swap! !state assoc-in [:add-see-also :ac-results] (vec (:body res)))
            (swap! !state assoc-in [:add-see-also :error] (-> res :body :error)))
          (swap! !state assoc-in [:add-see-also :completing?] false)
          (recur))))

    (go-loop []
      (when-let [to-del (<! delete-ch)]
        (swap! !state update-sa
          (:_id to-del)
          (fn [sa]
            (assoc sa :delete-state :loading)))
        (let [{:keys [success res]}
              (<! (ajax-chan
                    {:method :delete
                     :path (str "/api/see-alsos/" (:_id to-del))
                     :data-type :edn}))]
          (if success
            (swap! !state update-in [:see-alsos]
              (fn [sas]
                (->> sas
                     (remove #(= (:_id %) (:_id to-del)))
                     vec)))
            (swap! !state update-sa
              (:_id to-del)
              (fn [sa]
                (assoc sa :delete-state :error)))))
        (recur)))

    (om/root
      see-alsos/$see-alsos
      !state
      {:target (sel1 $root :.see-alsos-widget)
       :init-state {:new-ch new-ch
                    :delete-ch delete-ch
                    :ac-ch ac-ch}})))

(defn init [$root]
  (let [!state (atom (init-state))]
    (init-examples $root !state)
    (init-see-alsos $root !state)
    (init-notes $root !state)))
