(ns clojuredocs.mods.search
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [sablono.core :as sab :refer-macros [html]]
            [clojuredocs.util :as util]
            [clojuredocs.metrics :as metrics])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [dommy.macros :refer [node sel1 sel]]))

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
        (for [{:keys [ns name href] :as sa} see-alsos]
          [:li
           [:a {:href href :class "var-link"}
            [:span.namespace ns]
            "/"
            [:span.name name]]])]
       (when (> num-left 0)
         [:span.remaining-label
          (str "+ " num-left " more")])])))

(defn $ac-entry-var [{:keys [href name ns doc see-alsos type examples-count]}]
  [:div.ac-entry
   [:span.ac-type type " / " examples-count " ex"  #_[:br] #_(util/pluralize examples-count "Example" "Examples")]
   [:h4
    [:a {:href href} name " (" ns ")"]]
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

(defn search-keydown [e app ac-results text-chan]
  (when app
    (let [ctrl? (.-ctrlKey e)
          key-code (.-keyCode e)
          {:keys [highlighted-index]} @app]
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


(defn focused? [$el]
  (= $el (.-activeElement js/document)))

(defn $quick-search-bar
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
                        false)}
         [:input.form-control
          {:class (when search-loading? " loading")
           :placeholder (or placeholder "Looking for? (ctrl-s)")
           :name "query"
           :autoComplete "off"
           :ref "input"
           :value ac-text
           :on-change #(let [text (.. % -target -value)]
                         (put! text-chan text)
                         (om/update! app :ac-text text))
           :on-key-down #(search-keydown % app ac-results text-chan)}]]))))

(defn $ac-results [{:keys [highlighted-index ac-results results-empty?]
                    :or {highlighted-index 0}
                    :as app}
                   owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (when (> (count ac-results) 0)
        (let [$el (om/get-node owner (:highlighted-index app))]
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

(defn $quick-lookup-widget
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

(defn submit-feedback [owner query clojure-level text]
  (om/set-state! owner :loading? true)
  (om/set-state! owner :error-message nil)
  (ajax
    {:method :post
     :path (str "/search-feedback")
     :data {:query query
            :clojure-level clojure-level
            :text text}
     :success (fn [_]
                (util/navigate-to "/search-feedback/success"))
     :error (fn [_]
              (om/set-state! owner
                :error-message
                "There was a problem sending your feedback, try again.")
              (om/set-state! owner :loading? false))})
  false)

(defn $search-feedback [app owner]
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
                                                :path (str "/search?query=" (util/url-encode ac-text))
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

(defn location-hash []
  (let [hash-str (.. js/window -location -hash)]
    (->> hash-str
         (drop 1)
         (apply str)
         util/url-decode)))

(defn init [$root]
  (let [prev-query (location-hash)
        !state (atom {:ac-text prev-query})
        text-chan (chan)
        action-ch (chan)]

    (when-not (empty? (sel :.search-widget))
      (swap! !state assoc :search-focused? true))

    (go-loop []
      (when-let [res (<! action-ch)]
        (metrics/track-search-choose (:ac-text @!state) (:href res))
        (set-location-hash! (:ac-text @!state))
        (util/navigate-to (:href res))
        (recur)))

    (wire-search text-chan !state)

    (dommy/listen! js/window :hashchange
      (fn [_]
        (let [loc-hash (location-hash)]
          (swap! !state assoc :ac-text loc-hash)
          (put! text-chan loc-hash))))

    (when-not (empty? prev-query)
      (swap! !state assoc :search-focused? true)
      (put! text-chan prev-query))

    (doseq [$el (sel :.search-widget)]
      (om/root
        $quick-lookup-widget
        !state
        {:init-state {:text-chan text-chan
                      :action-ch action-ch}
         :target $el}))

    (doseq [$el (sel :.quick-search-widget)]
      (om/root
        $quick-search-bar
        !state
        {:init-state {:text-chan text-chan
                      :action-ch action-ch}
         :target $el}))

    (doseq [$el (sel :.ac-results-widget)]
      (om/root
        $ac-results
        !state
        {:target $el
         :init-state {:action-ch action-ch}}))

    (doseq [$el (sel :.search-feedback-widget)]
      (om/root
        $search-feedback
        !state
        {:target $el}))))
