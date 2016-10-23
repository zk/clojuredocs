(ns clojuredocs.mods.search
  (:require [reagent.core :as rea]
            [nsfw.ops :as ops]
            [nsfw.page :as page]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [clojuredocs.util :as util]
            [clojuredocs.metrics :as metrics])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn ellipsis [n s]
  (if (> (- (count s) 3) n)
    (str (->> s
              (take (- n 3))
              (apply str))
         "...")
    s))

(defn handle-search-active-state [ac-text]
  (if (empty? ac-text)
    (dommy/remove-class! (sel1 :body) :search-active)
    (dommy/add-class! (sel1 :body) :search-active)))

;; Landing page autocomplete

(defn $ac-see-alsos [see-alsos]
  (when-not (empty? see-alsos)
    (let [limit 5
          num-left (- (count see-alsos) limit)
          see-alsos (take limit see-alsos)]
      [:div.see-alsos
       [:span.see-also-label "see also:"]
       [:ul
        (->> see-alsos
             (map-indexed
               (fn [i {:keys [ns name] :as sa}]
                 ^{:key i}
                 [:li
                  [:a {:href (util/var-path ns name) :class "var-link"}
                   [:span.namespace ns]
                   "/"
                   [:span.name name]]])))]
       (when (> num-left 0)
         [:span.remaining-label
          (str "+ " num-left " more")])])))

(defn $ac-entry-var [{:keys [href name ns doc see-alsos type examples-count]}]
  [:div.ac-entry
   [:span.ac-type type " / " examples-count " ex"  #_[:br] #_(util/pluralize examples-count "Example" "Examples")]
   [:h4
    [:a {:href href
         :on-click #(do
                      (when (or (.-ctrlKey %)
                                (.-metaKey %)
                                (= 1 (.-button %))) ; middle click
                        (.stopPropagation %)))}
     name " (" ns ")"]]
   [:p (ellipsis 100 doc)]
   ($ac-see-alsos see-alsos)])

(defn $ac-entry-ns [{:keys [href name ns doc desc see-alsos type]}]
  [:div.ac-entry
   [:span.ac-type type]
   [:h4
    [:a {:href href} name]]
   [:p (ellipsis 100 (or doc desc))]
   ($ac-see-alsos see-alsos)])

(defn $ac-entry-page [{:keys [href name desc type href]}]
  [:div.ac-entry
   [:span.ac-type type]
   [:h4
    [:a {:href href} name]]
   [:p (ellipsis 100 desc)]])

(defn $ac-entry [{:keys [type] :as ac-entry}]
  (cond
    (get #{"function" "macro" "var" "special-form"} type)
    ($ac-entry-var ac-entry)

    (= "namespace" type) ($ac-entry-ns ac-entry)

    (= "page" type) ($ac-entry-page ac-entry)
    :else (.log js/console (str "Couldn't render ac entry:" type))))

(defn focused? [$el]
  (= $el (.-activeElement js/document)))

#_(defn $quick-search-bar
    [{:keys [highlighted-index search-loading?
             ac-results ac-text search-focused?] :as app} owner]
    (reify
      om/IDidMount
      (did-mount [_]
        (let [$input (om/get-node owner "input")]
          (when (and (not (focused? $input))
                     search-focused?)
            (.focus $input)
            (aset $input "value" (.-value $input)))))
      om/IDidUpdate
      (did-update [_ _ _]
        (handle-search-active-state ac-text)
        (let [$input (om/get-node owner "input")]
          (when (and (not (focused? $input))
                     search-focused?)
            (.focus $input)
            (aset $input "value" (.-value $input)))))
      om/IRenderState
      (render-state [this {:keys [text-chan action-ch placeholder]}]
        (sab/html
          [:form.search
           {:autoComplete "off"
            :on-submit #(do
                          (let [{:keys [highlighted-index ac-results]} @app]
                            (put! action-ch (nth ac-results (or highlighted-index 0))))
                          false)
            :action "/search"
            :method :get}
           [:input.form-control.query
            {:class (when search-loading? " loading")
             :placeholder (or placeholder "Looking for? (ctrl-s)")
             :name "q"
             :autoComplete "off"
             :ref "input"
             :value ac-text
             :on-change #(let [text (.. % -target -value)]
                           (put! text-chan text)
                           (om/update! app :ac-text text))
             :on-key-down #(search-keydown % app ac-results text-chan)}]]))))

#_ #(search-keydown % app ac-results text-chan)

#_(defn search-keydown [e app ac-results text-chan]
    (when app
      (let [ctrl? (.-ctrlKey e)
            key-code (.-keyCode e)
            {:keys [highlighted-index]} @app]

        ;; execute search
        (when (= 27 key-code)
          (om/update! app :ac-text "")
          (om/update! app :ac-results nil)
          (put! text-chan ""))


        (let [f (cond
                  (and ctrl? (= 78 key-code)) inc ; ctrl-n
                  (= 40 key-code) inc             ; down arrow
                  (and ctrl? (= 80 key-code)) dec ; ctrl-p
                  (= 38 key-code) dec             ; up arrow
                  :else identity)]
          (when (and (not (= identity f))
                     (not (and (= inc f)
                               (= highlighted-index (dec (count ac-results)))))
                     (not (and (= dec f)
                               (= highlighted-index 0))))
            (om/transact! app :highlighted-index f))
          (when (not (= identity f))
            false)))))

(defn cancel-search? [{:keys [key-code]}]
  (= 27 key-code))

(defn next-res? [{:keys [key-code ctrl?]}]
  (or (and ctrl? (= 78 key-code)) ; ctrl-n
      (= 40 key-code)             ; down arrow
      ))

(defn prev-res? [{:keys [key-code ctrl?]}]
  (or (and ctrl? (= 80 key-code)) ; ctrl-p
      (= 38 key-code)             ; up arrow
      ))

(defn $quick-search-bar [app bus]
  (let [on-change-throttle
        (page/throttle-debounce
          (fn [text]
            (ops/send bus ::ac-text-throttled text))
          {:throttle 200
           :debounce 200})]
    (rea/create-class
      {:reagent-render
       (fn [{:keys [highlighted-index search-loading?
                    ac-results ac-text search-focused?
                    placeholder] :as app}]
         [:form.search
          {:autoComplete "off"
           :on-submit (fn [e]
                        (ops/send bus
                          ::ac-select
                          (nth ac-results (or highlighted-index 0)))
                        (.preventDefault e)
                        nil)
           :action "/search"
           :method :get}
          [:input.form-control.query
           {:class (when search-loading? " loading")
            :placeholder (or placeholder "Looking for? (ctrl-s)")
            :name "q"
            :autoComplete "off"
            :ref "input"
            :value ac-text
            :on-change (fn [e]
                         (let [text (.. e -target -value)]
                           (ops/send bus ::ac-text-changed text)
                           (on-change-throttle text)))
            :on-key-down (fn [e]
                           (let [act {:key-code (.-keyCode e)
                                      :ctrl? (.-ctrlKey e)}]
                             (cond
                               (cancel-search? act) (do
                                                      (ops/send bus ::cancel-search ac-text)
                                                      (.preventDefault e))
                               (next-res? act) (do
                                                 (ops/send bus ::move-highlight 1)
                                                 (.preventDefault e))
                               (prev-res? act) (do
                                                 (ops/send bus ::move-highlight -1)
                                                 (.preventDefault e)))))}]])
       :did-mount (fn []
                    #_(let [$input (om/get-node owner "input")]
                        (when (and (not (focused? $input))
                                   search-focused?)
                          (.focus $input)
                          (aset $input "value" (.-value $input)))))
       :did-update (fn [])
       #_(fn []
           (handle-search-active-state ac-text)
           (let [$input (om/get-node owner "input")]
             (when (and (not (focused? $input))
                        search-focused?)
               (.focus $input)
               (aset $input "value" (.-value $input)))))})))

(defn $nav-search-widget [!state bus]
  [$quick-search-bar
   (merge @!state {:placeholder "Looking for? (ctrl-s)"})
   bus])

#_(defn $ac-results [{:keys [highlighted-index ac-results results-empty?]
                      :or {highlighted-index 0}
                      :as app}
                     owner]
    (reify
      om/IDidUpdate
      (did-update [_ prev-props prev-state]
        (when (> (count ac-results) 0)
          (let [$el (om/get-node owner)]
            (when (and (not= (:highlighted-index prev-props)
                             (:highlighted-index app))
                       $el)
              (anim/scroll-into-view $el {:pad 130})))))
      om/IRenderState
      (render-state [this {:keys [action-ch]}]
        (sab/html
          [:ul.ac-results
           (if results-empty?
             [:li.null-state "Nothing Found"]
             (map-indexed
               (fn [i {:keys [href type] :as res}]
                 [:li {:on-click #(do
                                    (put! action-ch res)
                                    false)
                       :class (when (= i highlighted-index)
                                "highlighted")
                       :ref i}
                  ($ac-entry res)])
               ac-results))]))))

(defn $ac-results [{:keys [highlighted-index ac-results results-empty?]
                    :or {highlighted-index 0}
                    :as app}
                   bus]
  (rea/create-class
    {:component-did-update (fn [this old-argv]
                             (let [{:keys [ac-results] :as app}
                                   (second (rea/argv this))
                                   old-app (second old-argv)]
                               (when (> (count ac-results) 0)
                                 (let [$el (dommy/sel1
                                             (rea/dom-node this)
                                             (str "#ac-" (:highlighted-index app)))]
                                   (when (and (not= (:highlighted-index old-app)
                                                    (:highlighted-index app))
                                              $el)
                                     (anim/scroll-into-view $el {:pad 130}))))))
     :reagent-render (fn [{:keys [highlighted-index ac-results results-empty?]
                           :or {highlighted-index 0}
                           :as app}
                          bus]
                       [:ul.ac-results
                        (if results-empty?
                          [:li.null-state "Nothing Found"]
                          (map-indexed
                            (fn [i {:keys [href type] :as res}]
                              ^{:key i}
                              [:li {:on-click (fn [e]
                                                (.preventDefault e)
                                                (ops/send bus
                                                  ::ac-select
                                                  res))
                                    :class (when (= i highlighted-index)
                                             "highlighted")
                                    :id (str "ac-" i)}
                               ($ac-entry res)])
                            ac-results))])}))

(defn $ac-results-widget [!state bus]
  [$ac-results @!state bus])

#_(defn $quick-lookup-widget
    [{:keys [highlighted-index ac-results ac-text search-loading? results-empty?]
      :or {highlighted-index 0}
      :as app}
     owner]
    (reify
      om/IRenderState
      (render-state [this {:keys [text-chan
                                  action-ch]}]
        (sab/html
          [:div.quick-lookup-wrapper
           (om/build $quick-search-bar app {:init-state {:text-chan text-chan
                                                         :action-ch action-ch
                                                         :placeholder "Looking for?"}})
           [:div.not-finding {:class "not-finding"}
            "Can't find what you're looking for? "
            [:a.search-feedback
             {:href (str "/search-feedback"
                         (when ac-text (str "?query=" (util/url-encode ac-text))))}
             "Help make ClojureDocs better"]
            "."]
           (om/build $ac-results app {:init-state {:action-ch action-ch}})]))))

(defn $quick-lookup-widget
  [!state bus]
  (let [{:keys [highlighted-index ac-results ac-text search-loading? results-empty?]
         :or {highlighted-index 0}
         :as app}
        @!state]
    [:div.quick-lookup-wrapper
     [$quick-search-bar
      (merge app {:placeholder "Looking for?"})
      bus]
     [:div.not-finding {:class "not-finding"}
      "Can't find what you're looking for? "
      [:a.search-feedback
       {:href (str "/search-feedback"
                   (when ac-text (str "?query=" (util/url-encode ac-text))))}
       "Help make ClojureDocs better"]
      "."]
     [$ac-results app bus]]))

#_(defn submit-feedback [owner query clojure-level text]
    (om/set-state! owner :loading? true)
    (om/set-state! owner :error-message nil)
    (ajax
      {:method :post
       :path (str "/search-feedback")
       :data {:query query
              :clojure-level clojure-level
              :text text}
       :data-type :edn
       :success (fn [_]
                  (util/navigate-to "/search-feedback/success"))
       :error (fn [& args]
                (om/set-state! owner
                  :error-message
                  "There was a problem sending your feedback, try again.")
                (om/set-state! owner :loading? false))})
    false)

#_(defn $search-feedback [app owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (let [query (om/get-state owner :query)]
          (om/set-state! owner
            :text (when-not (empty? query)
                    (str
                      "Hey ClojureDocs, I searched for \""
                      query
                      "\", but couldn't find what I was looking for. Here's a description of what I would have liked to find:")))))
      om/IRenderState
      (render-state [_ {:keys [text loading? clojure-level query error-message]}]
        (sab/html
          [:form {:on-submit #(submit-feedback owner query clojure-level text)}
           [:div.form-group
            [:label.clojure-level
             "Level of Clojuring"]
            [:div.radio
             [:label.radio
              [:input {:type "radio"
                       :name "clojure-level"
                       :value "beginner"
                       :on-click #(om/set-state! owner :clojure-level "beginner")
                       :disabled (if loading? "disabled")}]
              "I haven't written any Clojure"]
             [:label.radio
              [:input {:type "radio"
                       :name "clojure-level"
                       :value "intermediate"
                       :on-click #(om/set-state! owner :clojure-level "intermediate")
                       :disabled (if loading? "disabled")}]
              "I've done a few things in Clojure"]
             [:label.radio
              [:input.radio
               {:type "radio"
                :name "clojure-level"
                :value "advanced"
                :on-click #(om/set-state! owner :clojure-level "advanced")
                :disabled (if loading? "disabled")}]
              "I'm comfortable contributing to Clojure projects"]]]
           [:div.form-group
            [:label {:for "feedback"} "Feedback"]
            [:textarea {:class "form-control"
                        :rows 10
                        :name "feedback"
                        :value text
                        :on-input #(om/set-state! owner :text (.. % -target -value))
                        :disabled (if loading? "disabled")}]]
           [:div.form-group
            [:span {:class (str "error-message" (when-not error-message " hidden"))}
             [:i.fa.fa-exclamation-circle]
             error-message]
            [:button.btn.btn-default.pull-right
             {:disabled (if loading? "disabled")}
             "Send Feedback"]
            [:img {:class (str "pull-right loading" (when-not loading? " hidden"))
                   :src "/img/loading.gif"}]]]))))

#_(fn []
    (om/set-state! owner
      :text (when-not (empty? query)
              (str
                "Hey ClojureDocs, I searched for \""
                query
                "\", but couldn't find what I was looking for. Here's a description of what I would have liked to find:"))))

(defn $search-feedback [{:keys [query clojure-level text
                                loading? error-message] :as app} bus]
  (let [!form (rea/atom {:text (when-not (empty? query)
                                 (str
                                   "Hey ClojureDocs, I searched for \""
                                   query
                                   "\", but couldn't find what I was looking for. Here's a description of what I would have liked to find:"))
                         :clojure-level clojure-level
                         :query query})]
    (fn []
      [:form {:on-submit (fn [e]
                           (ops/send bus
                             ::submit-feedback
                             {:query query
                              :clojure-level clojure-level
                              :text text}))}
       [:div.form-group
        [:label.clojure-level
         "Level of Clojuring"]
        [:div.radio
         [:label.radio
          [:input {:type "radio"
                   :name "clojure-level"
                   :value "beginner"
                   :on-click (fn [_]
                               (swap! !form assoc :clojure-level "beginner"))
                   :disabled (if loading? "disabled")}]
          "I haven't written any Clojure"]
         [:label.radio
          [:input {:type "radio"
                   :name "clojure-level"
                   :value "intermediate"
                   :on-click (fn [_]
                               (swap! !form assoc :clojure-level "intermediate"))
                   :disabled (if loading? "disabled")}]
          "I've done a few things in Clojure"]
         [:label.radio
          [:input.radio
           {:type "radio"
            :name "clojure-level"
            :value "advanced"
            :on-click (fn [_]
                        (swap! !form assoc :clojure-level "advanced"))
            :disabled (if loading? "disabled")}]
          "I'm comfortable contributing to Clojure projects"]]]
       [:div.form-group
        [:label {:for "feedback"} "Feedback"]
        [:textarea {:class "form-control"
                    :rows 10
                    :name "feedback"
                    :value text
                    :on-input (fn [e]
                                (swap! !form assoc :text (.. e -target -value)))
                    :disabled (if loading? "disabled")}]]
       [:div.form-group
        [:span {:class (str "error-message" (when-not error-message " hidden"))}
         [:i.fa.fa-exclamation-circle]
         error-message]
        [:button.btn.btn-default.pull-right
         {:disabled (if loading? "disabled")}
         "Send Feedback"]
        [:img {:class (str "pull-right loading" (when-not loading? " hidden"))
               :src "/img/loading.gif"}]]])))

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

(defn throttle [in ms]
  (let [c (chan)
        timer (atom nil)]
    (go-loop []
      (when-let [new-text (<! in)]
        (js/clearTimeout @timer)
        (reset! timer (js/setTimeout #(put! c new-text) ms))
        (recur)))
    c))

(defn set-location-hash! [s]
  (if-not (empty? s)
    (aset (.. js/window -location) "hash" s)
    (.replaceState (.-history js/window)
      #js {}
      nil
      (.. js/window -location -pathname))))

(defn wire-search [text-chan app-state]
  (let [throttled-text-chan (throttle text-chan 200)]
    (go
      (while true
        (let [ac-text (<! throttled-text-chan)]
          (if (empty? ac-text)
            (do
              (swap! app-state assoc :results-empty? false :ac-results [])
              (set-location-hash! ""))
            (do
              (swap! app-state assoc :search-loading? true)
              (let [ac-response (<! (ajax-chan {:method :get
                                                :path (str "/ac-search?query=" (util/url-encode ac-text))
                                                :data-type :edn}))
                    data (-> ac-response :res :body)]
                (when (:success ac-response)
                  (metrics/track-search ac-text)
                  (swap! app-state
                    assoc
                    :highlighted-index 0
                    :search-loading? false
                    :results-empty? (and (empty? data) (not (empty? ac-text)))
                    :ac-results data))))
            (swap! app-state assoc :search-loading? false)))))))

(defn init [$root]
  (let [prev-query (util/location-hash)
        !state (rea/atom {:ac-text (when-not (re-find #"^example[_-]" prev-query)
                                     prev-query)})
        bus (ops/kit
              !state
              {}
              {::ac-text-changed (fn [state ac-text]
                                   (assoc state :ac-text ac-text))

               ::ac-text-throttled
               (fn [state ac-text]
                 (let [ch (chan)]
                   (go
                     (put! ch (merge state {:search-loading? true}))
                     (if (empty? ac-text)
                       (do
                         (put! ch (merge state {:results-empty? false
                                                :ac-results []
                                                :search-loading? false}))
                         (set-location-hash! ""))
                       (do
                         (put! ch (merge state {:search-loading? true}))
                         (let [ac-response (<! (ajax-chan {:method :get
                                                           :path (str "/ac-search?query=" (util/url-encode ac-text))
                                                           :data-type :edn}))
                               data (-> ac-response :res :body)]
                           (when (:success ac-response)
                             (metrics/track-search ac-text)
                             (put! ch
                               (fn [state]
                                 (merge
                                   state
                                   {:highlighted-index 0
                                    :results-empty? (and (empty? data) (not (empty? ac-text)))
                                    :ac-results data
                                    :search-loading? false})))))))
                     (close! ch))
                   ch))

               ::move-highlight (fn [state dir]

                                  (assoc state
                                    :highlighted-index
                                    (min
                                      (dec (count (:ac-results state)))
                                      (max 0 (+ (:highlighted-index state) dir)))))

               ::cancel-search (fn [state]
                                 (dissoc state :search-loading? :ac-results :ac-text :results-empty?))

               ::ac-select (fn [state res]
                             (util/navigate-to (util/var-path
                                                 (:ns res)
                                                 (:name res))))})]

    (when-not (empty? (sel :.search-widget))
      (swap! !state assoc :search-focused? true))

    #_(go-loop []
        (when-let [res (<! action-ch)]
          (metrics/track-search-choose (:ac-text @!state) (:href res))
          (set-location-hash! (:ac-text @!state))
          (util/navigate-to (:href res))
          (recur)))

    #_(wire-search text-chan !state)

    #_(dommy/listen! js/window :hashchange
        (fn [_]
          (let [loc-hash (util/location-hash)]
            (when-not (re-find #"^example[_-]" loc-hash)
              (swap! !state assoc :ac-text loc-hash)
              (put! text-chan loc-hash)))))

    #_(when (and (not (empty? prev-query))
                 (not (re-find #"^example[_-]" prev-query)))
        (swap! !state assoc :search-focused? true)
        (put! text-chan prev-query))

    (doseq [$el (sel :.search-widget)]
      (rea/render-component
        [$quick-lookup-widget !state bus]
        $el))

    (doseq [$el (sel :.nav-search-widget)]
      (rea/render-component
        [$nav-search-widget !state bus]
        $el))

    (doseq [$el (sel :.ac-results-widget)]
      (rea/render-component
        [$ac-results-widget !state bus]
        $el))

    (doseq [$el (sel :.search-feedback-widget)]
      (rea/render-component
        [$search-feedback @!state bus]
        $el))
    (fn [])))
