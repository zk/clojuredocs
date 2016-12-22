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

(defn $quick-search-bar [{:keys [autofocus?]} bus]
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
                        (let [res (and (not (empty? ac-results))
                                       (nth ac-results (or highlighted-index 0)))]
                          (if res
                            (ops/send bus
                              ::ac-select
                              (nth ac-results (or highlighted-index 0)))
                            (ops/send bus
                              ::var-search
                              ac-text)))
                        (.preventDefault e)
                        nil)
           :action "/search"
           :method :get}
          [:input.form-control.query
           (merge
             {:class (when search-loading? " loading")
              :placeholder (or placeholder "Looking for? (ctrl-s)")
              :name "q"
              :autoComplete "off"
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
                                                   (.preventDefault e)))))}
             (when autofocus?
               {:autoFocus "autofocus"}))]])
       :component-did-mount
       (fn [this]
         (when autofocus?
           (when-let [$form (rea/dom-node this)]
             (let [$input (dommy/sel1 $form :input.query)]
               (when (and $input
                          (not (focused? $input)))
                 (.focus $input)
                 (aset $input "value" (.-value $input)))))))
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
   (merge @!state {:placeholder "Looking for? (ctrl-s)"
                   :autofocus? false})
   bus])

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

(defn $quick-lookup-widget
  [!state bus]
  (let [{:keys [highlighted-index ac-results ac-text search-loading? results-empty?]
         :or {highlighted-index 0}
         :as app}
        @!state]
    [:div.quick-lookup-wrapper
     [$quick-search-bar
      (merge app {:placeholder "Looking for?"
                  :autofocus? true})
      bus]
     [$ac-results app bus]]))

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
                             (if (and (:ns res) (:name res))
                               (util/navigate-to
                                 (util/var-path
                                   (:ns res)
                                   (:name res)))))
               ::var-search (fn [state text]
                              (util/navigate-to
                                (str "/search?q=" (util/url-encode text))))})]

    (when-not (empty? (sel :.search-widget))
      (swap! !state assoc :search-focused? true))

    #_(go-loop []
        (when-let [res (<! action-ch)]
          (metrics/track-search-choose (:ac-text @!state) (:href res))
          (set-location-hash! (:ac-text @!state))
          (util/navigate-to (:href res))
          (recur)))

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

    (fn [])))
