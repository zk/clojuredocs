(ns clojuredocs.widgets
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [goog.crypt :as gcrypt]
            [goog.crypt.Md5 :as Md5]
            [goog.crypt.Sha1 :as Sha1])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1]]))

(enable-console-print!)

(defn cd-encode [s]
  (cond
    (= "." s) "_."
    (= ".." s) "_.."
    :else (-> s
              (str/replace #"/" "_fs")
              (str/replace #"\\" "_bs")
              (str/replace #"\?" "_q"))))

(defn pluralize [n single plural]
  (str n " " (if (= 1 n) single plural)))

(defn ellipsis [n s]
  (if (> (- (count s) 3) n)
    (str (->> s
              (take (- n 3))
              (apply str))
         "...")
    s))

(defn url-encode
  [string]
  (some-> string
    str
    (js/encodeURIComponent)
    (.replace "+" "%20")))

(defn var-url [{:keys [ns name]}]
  (str "/" (url-encode ns) "/" (url-encode (cd-encode name))))


;; Add an example

(defn validate-and-submit [app owner]
  (om/set-state! owner :expanded? false)
  false)

(defn set-expanded [owner expanded?]
  (om/set-state! owner :expanded? expanded?)
  (om/set-state! owner :should-focus? true)
  false)

(defn update-text [e owner]
  (om/set-state! owner :text (.. e -target -value))
  false)

(defn add-example [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (dommy/append!
        (om/get-node owner "live-preview")
        (node [:div.empty-live-preview "Live Preview"])))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when (and (om/get-state owner :should-focus?)
                 (om/get-state owner :expanded?))
        (om/set-state! owner :should-focus? false)
        (.focus (om/get-node owner "textarea"))
        (anim/scroll-to (om/get-node owner "wrapper") {:pad 10}))
      (let [text (om/get-state owner :text)
            preview (om/get-node owner "live-preview")
            el (node [:pre {:class "brush: clojure"} text])]
        (dommy/clear! preview)
        (if-not (empty? text)
          (do
            (dommy/append! preview el)
            (try
              (.highlight js/SyntaxHighlighter el)
              ;; Not handling this error prevents subsequent
              ;; highlights from succeeding
              (catch js/Error e (prn "Error highlighting example"))))
          (dommy/append! preview (node [:div.empty-live-preview "Live Preview"])))))
    om/IRenderState
    (render-state [this {:keys [expanded? text]}]
      (dom/div {:class "add-example" :ref "wrapper"}
        (dom/div {:class "toggle-controls"}
          (dom/a {:class "toggle-link"
                  :href ""
                  :on-click #(set-expanded owner (not expanded?))}
            (if-not expanded? "Add an Example" "Close")))
        (dom/div {:class (str "add-example-content" (when-not expanded? " hidden"))}
          (dom/h4 "Your Example")
          (dom/div {:class "add-example-preview"}
            (dom/div {:ref "live-preview" :class "live-preview"}))
          (dom/form {:on-submit #(validate-and-submit app owner)}
            (dom/textarea {:class "form-control"
                           :cols 80 :on-input #(update-text % owner)
                           :ref "textarea"}
              text)
            (dom/p {:class "instructions"}
              "See our "
              (dom/a {:href "/examples-styleguide"} "examples style guide")
              " for content and formatting guidelines. "
              "Examples submitted to ClojureDocs are licensed under the "
              (dom/a {:href "https://creativecommons.org/publicdomain/zero/1.0/"}
                "Creative Commons CC0 license")
              ".")
            (dom/div {:class "add-example-controls clearfix"}
              (dom/button {:class "btn btn-default"
                           :on-click (fn [e]
                                       (set-expanded owner (not expanded?))
                                       (om/set-state! owner :text nil)
                                       false)}
                "Cancel")
              (dom/button {:class "btn btn-success pull-right"} "Add Example")
              (dom/img {:class (str "pull-right loading" (when-not loading? " hidden"))
                        :src "/img/loading.gif"}))))))))

;; Examples

#_(defn $example [{:keys [body _id history created-at updated-at] :as ex}]
  [:div.row
   [:div.col-md-10
    [:a {:id (str "example_" _id)}]
    ($example-body ex)]
   [:div.col-md-2
    (let [users (->> history
                     (map :user)
                     distinct
                     reverse)]
      [:div.example-meta
       [:div.contributors
        (->> users
             (take 10)
             (map common/$avatar))]
       (when (> (count users) 10)
         [:div.contributors
          (count users) " contributors total."])
       #_[:div.created
        "Created " (util/timeago created-at) " ago."]
       #_(when-not (= created-at updated-at)
         [:div.last-updated
          "Updated " (util/timeago updated-at) " ago."])
       [:div.links
        [:a {:href (str "#example_" _id)}
         "link"]
        " / "
        [:a {:href (str "/ex/" _id)}
         "history"]]])]])

(defn string->bytes [s]
  (gcrypt/stringToUtf8ByteArray s))  ;; must be utf8 byte array

(defn bytes->hex
  "convert bytes to hex"
  [bytes-in]
  (gcrypt/byteArrayToHex bytes-in))

(defn hash-bytes [digester bytes-in]
  (do
    (.update digester bytes-in)
    (.digest digester)))

(defn md5-
  "convert bytes to md5 bytes"
  [bytes-in]
  (hash-bytes (goog.crypt.Md5.) bytes-in))

(defn md5-bytes
  "convert utf8 string to md5 byte array"
  [string]
  (md5- (string->bytes string)))

(defn md5-hex [string]
  "convert utf8 string to md5 hex string"
  (bytes->hex (md5-bytes string)))

(defn avatar [{:keys [email login]}]
  (dom/a {:href (str "/u/" login)}
    (dom/img {:class "avatar"
              :src (str "https://www.gravatar.com/avatar/" (md5-hex email) "?r=PG&s=48&default=identicon")})))

(defn example-widget [{:keys [body history _id]} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [el (om/get-node owner "source")
            pre (node [:pre {:class "brush: clojure"} body])]
        (dommy/clear! el)
        (dommy/append! el pre)
        (.highlight js/SyntaxHighlighter #js {:toolbar false :gutter false} pre)))

    om/IDidUpdate
    (did-update [_ _ _]
      (let [el (om/get-node owner "source")]
        (dommy/clear! el)
        (dommy/append! el (node
                            [:pre {:class "brush: clojure"}
                             body]))))
    om/IRender
    (render [this]
      (let [users (->> history
                       (map :user)
                       distinct
                       reverse)
            num-to-show 6]
        (dom/div {:class "row var-example"}
          (dom/div {:class "col-md-10"}
            (dom/div {:class "example-body" :ref "source"}))
          (dom/div {:class "col-md-2"}
            (dom/div {:class "example-meta"}
              (dom/div {:class "contributors"}
                (->> users
                     (take num-to-show)
                     (map avatar))
                (when (> (count users) num-to-show)
                  (dom/div {:class "contributors"}
                    "+ "
                    (- (count users) num-to-show)
                    " more")))
              (dom/div {:class "links"}
                (dom/a {:href (str "#example_" _id)} "link")
                " / "
                (dom/a {:href (str "/ex/" _id)} "history")))))))))

(defn examples [{:keys [examples var]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/div {:class "var-examples"}
        (dom/h3 (pluralize (count examples) "Example" "Examples"))
        (if (> (count examples) 0)
          (for [example examples]
            (om/build example-widget example))
          (dom/div {:class "null-state"}
            "No examples for " (:ns var) "/" (:name var) "."))))))

;; Landing page autocomplete

(defmulti ac-entry :type)

(defn see-alsos-widget [see-alsos]
  (when-not (empty? see-alsos)
    (let [limit 5
          num-left (- (count see-alsos) limit)
          see-alsos (take limit see-alsos)]
      (dom/div {:class "see-alsos"}
        (dom/span {:class "see-also-label"} "see also:")
        (dom/ul
          (for [{:keys [ns name href] :as sa} see-alsos]
            (dom/li
              (dom/a {:href href :class "var-link"}
                (dom/span {:class "namespace"} ns)
                "/"
                (dom/span {:class "name"} name)))))
        (when (> num-left 0)
          (dom/span {:class "remaining-label"} "+ " num-left " more"))))))

(defmethod ac-entry "function" [{:keys [href name ns doc type see-alsos] :as func}]
  (dom/div {:class "ac-entry"}
    (dom/span {:class "ac-type"} "fn")
    (dom/h4
      (dom/a {:href href}
        name " (" ns ")"))
    (dom/p (ellipsis 100 doc))
    (see-alsos-widget see-alsos)))

(defmethod ac-entry "var" [{:keys [href name ns doc type see-alsos] :as func}]
  (dom/div {:class "ac-entry"}
    (dom/span {:class "ac-type"} "var")
    (dom/h4
      #_(dom/i {:class "fa fa-exclamation"})
      (dom/a {:href href}
        name " (" ns ")"))
    (dom/p (ellipsis 100 doc))
    (see-alsos-widget see-alsos)))

(defmethod ac-entry "special-form" [{:keys [href name ns doc type see-alsos] :as func}]
  (dom/div {:class "ac-entry"}
    (dom/span {:class "ac-type"} "special form")
    (dom/h4
      #_(dom/i {:class "fa fa-exclamation"})
      (dom/a {:href href}
        name " (" ns ")"))
    (dom/p (ellipsis 100 doc))
    (see-alsos-widget see-alsos)))

(defmethod ac-entry "macro" [{:keys [href name ns doc type see-alsos]}]
  (dom/div {:class "ac-entry"}
    (dom/span {:class "ac-type"} "macro")
    (dom/h4
      (dom/a {:href href}
        name " (" ns ")"))
    (dom/p (ellipsis 225 doc))
    (see-alsos-widget see-alsos)))

(defmethod ac-entry "namespace" [{:keys [href name doc type desc]}]
  (dom/div {:class "ac-entry"}
    (dom/span {:class "ac-type"} "namespace")
    (dom/h4
      (dom/a {:href href} name))
    (dom/p (ellipsis 225 (or doc desc)))))

(defmethod ac-entry "page" [{:keys [href name desc type href]}]
  (dom/div {:class "ac-entry"}
    (dom/span {:class "ac-type"} "page")
    (dom/h4
      (dom/a {:href href} name))
    (dom/p (ellipsis 255 desc))))

(defmethod ac-entry :default [{:keys [type]}]
  (.log js/console (str "Couldn't render ac entry:" type)))

(defn put-text [e text-chan owner]
  (let [text (.. e -target -value)]
    (put! text-chan text)
    (om/set-state! owner :loading? true)
    (om/set-state! owner :text text)))

(defn navigate-to [url]
  (aset (.-location js/window) "href" url))

(defn search-submit [{:keys [href]}]
  (when href
    (navigate-to href))
  false)

(defn maybe-nav [e app ac-results]
  (when app
    (let [ctrl? (.-ctrlKey e)
          key-code (.-keyCode e)
          {:keys [highlighted-index]} @app]
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

(defn throttle [ms f]
  (let [timer (atom nil)]
    (fn [e]
      (.persist e)
      (js/clearTimeout @timer)
      (reset! timer (js/setTimeout #(f e) ms)))))

(defn quick-search [{:keys [highlighted-index loading? ac-results] :as app} owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [text-chan text]}]
      (dom/form {:class "search" :autoComplete "off"
                 ;; the search widget should prob not need to know about
                 ;; autocomplete results (or act on them)
                 :on-submit #(search-submit (nth ac-results highlighted-index))}
        (dom/input {:class (str "form-control" (when loading? " loading"))
                    :placeholder "Looking for? (ctrl-s)"
                    :name "query"
                    :autoComplete "off"
                    :on-input (throttle 200 #(put-text % text-chan owner))
                    :on-key-down #(maybe-nav % app ac-results)})))))

(defn ac-results [{:keys [highlighted-index ac-results]
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
            (anim/scroll-into-view $el {:pad 30})))))
    om/IRender
    (render [this]
      (dom/ul {:class "ac-results"}
        (map-indexed
          (fn [i {:keys [href type] :as res}]
            (dom/li {:on-click #(when href (navigate-to href))
                     :class (when (= i highlighted-index)
                              "highlighted")
                     :ref i}
              (ac-entry res)))
          ac-results)))))

(defn quick-lookup [{:keys [highlighted-index ac-results loading?]
                     :or {highlighted-index 0}
                     :as app}
                    owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (when (and (not= (:highlighted-index prev-props)
                       (:highlighted-index app))
                 (> (count ac-results) 0))
        (anim/scroll-into-view (om/get-node owner (:highlighted-index app)) {:pad 30})))
    om/IRenderState
    (render-state [this {:keys [text-chan text]}]
      (dom/form {:class "search"
                 :autoComplete "off"
                 :on-submit #(search-submit (nth ac-results highlighted-index))}
        (dom/input {:class (str "form-control" (when loading? " loading"))
                    :placeholder "Looking for?"
                    :name "query"
                    :autoComplete "off"
                    :autoFocus "autofocus"
                    :on-input (throttle 200 #(put-text % text-chan owner))
                    :on-key-down #(maybe-nav % app ac-results)})
        (dom/div {:class "not-finding"}
          "Can't find what you're looking for? "
          (dom/a {:href (str "search-feedback" (when text (str "?query=" (url-encode text))))} "Help make ClojureDocs better")
          ".")
        (dom/ul {:class "ac-results"}
          (map-indexed
            (fn [i {:keys [href type] :as res}]
              (dom/li {:on-click #(when href (navigate-to href))
                       :class (when (= i highlighted-index)
                                "highlighted")
                       :ref i}
                (ac-entry res)))
            ac-results))))))

(defn toggle [owner key]
  (om/update-state! owner (fn [state]
                            (assoc state key (not (get state key)))))
  false)

(defn add-see-also [{:keys [user]} owner]
  (reify
    om/IDidUpdate
    (did-update [_ _ _]
      (.focus (om/get-node owner "input")))
    om/IRenderState
    (render-state [this {:keys [expanded?]}]
      (dom/div {:class "add-see-also"}
        (dom/div {:class "toggle-controls"}
          (if true
            (dom/a {:class "toggle-link" :href "#" :on-click #(toggle owner :expanded?)}
              (if-not expanded?
                "Add See Also"
                "Close"))
            (dom/span {:class "muted"} "log in to add a see also")))
        (dom/div {:class (when-not expanded? "hidden")}
          (dom/form {:on-submit (constantly false) :autoComplete "off"}
            (dom/div {:class "input-group"}
              (dom/input {:class "form-control"
                          :name "see-also-name"
                          :ref "input"
                          :placeholder "Var Name"})
              (dom/span {:class "input-group-btn"}
                (dom/button {:class "btn btn-success"} "Add See-Also")))))))))

(defn $add-comment [{:keys [user]} owner]
  (reify
    om/IDidUpdate
    (did-update [_ _ _]
      (.focus (om/get-node owner "input"))
      #_(when-let [text (om/get-state owner :text)]
        (aset (om/get-node owner "live-preview")
          "innerHTML"
          (-> text
              md->html
              (str/replace #"<pre>" "<pre class=\"brush: clojure\">")))))
    om/IRenderState
    (render-state [this {:keys [expanded? text]}]
      (dom/div {:class "add-comment"}
        (dom/div {:class "toggle-controls"}
          (if true
            (dom/a {:class "toggle-link" :href "#" :on-click #(toggle owner :expanded?)}
              (if-not expanded?
                "Add Comment"
                "Close"))
            (dom/span {:class "muted"} "log in to add a comment")))
        (dom/div {:class (when-not expanded? "hidden")}
          (dom/form {:on-submit (constantly false) :autoComplete "off" :class "add-comment-form"}
            (dom/div {:class "form-group"}
              (dom/div {:class "instructions"}
                (dom/p "Markdown allowed, code in <pre />"))
              (dom/textarea
                {:class "form-control"
                 :name "comment-name"
                 :ref "input"
                 :value text
                 :placeholder "Comment goes here"
                 :on-input #(do
                              (om/set-state! owner :text (.. % -target -value))
                              false)})))
          (dom/div {:class "live-preview" :ref "live-preview"}))))))

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
                (navigate-to "/search-feedback/success"))
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
      (dom/form {:class "form" :on-submit #(submit-feedback owner query clojure-level text)}
        (dom/div {:class "form-group"}
          (dom/label {:for "clojure-level"}
            "Level of Clojuring")
          (dom/div {:class "radio"}
            (dom/label {:class "radio"}
              (dom/input {:type "radio"
                          :name "clojure-level"
                          :value "beginner"
                          :on-click #(om/set-state! owner :clojure-level "beginner")
                          :disabled (if loading? "disabled")})
              "I haven't written any Clojure")
            (dom/label {:class "radio"}
              (dom/input {:type "radio"
                          :name "clojure-level"
                          :value "intermediate"
                          :on-click #(om/set-state! owner :clojure-level "intermediate")
                          :disabled (if loading? "disabled")})
              "I've done a few things in Clojure")
            (dom/label {:class "radio"}
              (dom/input {:type "radio"
                          :name "clojure-level"
                          :value "advanced"
                          :on-click #(om/set-state! owner :clojure-level "advanced")
                          :disabled (if loading? "disabled")})
              "I'm comfortable contributing to Clojure projects")))
        (dom/div {:class "form-group"}
          (dom/label {:for "feedback"} "Feedback")
          (dom/textarea {:class "form-control"
                         :autoFocus "autofocus"
                         :rows 10
                         :name "feedback"
                         :value text
                         :on-input #(om/set-state! owner :text (.. % -target -value))
                         :disabled (if loading? "disabled")}))
        (dom/div {:class "form-group"}
          (dom/span {:class (str "error-message" (when-not error-message " hidden"))}
            (dom/i {:class "fa fa-exclamation-circle"})
            error-message)
          (dom/button {:class "btn btn-default pull-right"
                       :disabled (if loading? "disabled")}
            "Send Feedback")
          (dom/img {:class (str "pull-right loading" (when-not loading? " hidden"))
                    :src "/img/loading.gif"}))))))

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

(defn wire-search [text-chan app-state]
  (go
    (while true
      (let [ac-text (<! text-chan)
            _ (swap! app-state assoc :loading? true)
            ac-response (<! (ajax-chan {:method :get
                                        :path (str "/search?query=" (url-encode ac-text))
                                        :data-type :edn}))
            data (-> ac-response :res :body)]
        (when (:success ac-response)
          #_(anim/scroll-to (sel1 :div.search-widget) {:pad 30})
          (swap! app-state
            assoc
            :highlighted-index 0
            :loading? false
            :ac-results data))))))

(def app-state
  (atom (reader/read-string (aget js/window "PAGE_DATA"))))

(def text-chan (chan))

(wire-search text-chan app-state)

(def init
  [[:div.search-widget]
   (fn [$el]
     (om/root
         quick-lookup
         app-state
         {:target $el
          :init-state {:text-chan text-chan}}))

   [:div.quick-search-widget]
   (fn [$el]
     (om/root
       quick-search
       app-state
       {:target $el
        :init-state {:text-chan text-chan}}))

   [:div.ac-results-widget]
   (fn [$el]
     (om/root
       ac-results
       app-state
       {:target $el}))

   [:div.add-example-widget]
   (fn [$el]
     (om/root
       add-example
       {}
       {:target $el
        :init-state {:expanded? true}}))

   [:div.add-see-also-widget]
   (fn [$el]
     (om/root
       add-see-also
       {}
       {:target $el}))

   [:div.search-feedback-widget]
   (fn [$el]
     (om/root
       $search-feedback
       {}
       {:target $el
        :init-state {:query (dommy/attr $el :data-query)}}))])
