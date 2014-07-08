(ns clojuredocs.widgets
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout]]
            [clojuredocs.ajax :refer [ajax]]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [goog.crypt     :as gcrypt]
            [goog.crypt.Md5 :as Md5]
            [goog.crypt.Sha1 :as Sha1]
            [dommy.core :as dommy]
            )
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node]]))

(enable-console-print!)

(defn pluralize [n single plural]
  (str n " " (if (= 1 n) single plural)))

(defn ellipsis [n s]
  (if (> (- (count s) 3) n)
    (str (->> s
              (take (- n 3))
              (apply str))
         "...")
    s))

(defn cd-encode [s]
  (-> s
      (str/replace #"^\.\.$" "_.")
      (str/replace #"^\.$" "_..")
      (str/replace #"\+" "_plus")))

(defn url-encode
  [string]
  (some-> string
    str
    (js/encodeURIComponent)
    (.replace "+" "%20")))

(defn var-url [{:keys [ns name]}]
  (str "/" (url-encode (cd-encode ns)) "/" (url-encode (cd-encode name))))


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
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when (and (om/get-state owner :should-focus?)
                 (om/get-state owner :expanded?))
        (om/set-state! owner :should-focus? false)
        (.focus (om/get-node owner "textarea"))))
    om/IRenderState
    (render-state [this {:keys [expanded? text]}]
      (dom/div {:class "add-example"}
        (dom/a {:href "" :on-click #(set-expanded owner (not expanded?))} "Add an Example")
        (dom/form {:class (when-not expanded? "hidden") :on-submit #(validate-and-submit app owner)}
          (dom/textarea {:class "form-control" :cols 80 :on-input #(update-text % owner)
                         :ref "textarea"}
            text)
          (dom/div {:class "add-example-controls clearfix"}
            (dom/button {:class "btn btn-default" :on-click #(set-expanded owner (not expanded?))}
              "Cancel")
            (dom/button {:class "btn btn-success pull-right"} "Add Example"))
          (dom/div {:class "add-example-preview"}
            (dom/h4 "Live Preview")
            (dom/pre #_{:class "brush: clojure"}
              text)))))))

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

(defmethod ac-entry :function [{:keys [name ns doc] :as func}]
  (dom/div {:class "ac-entry"}
    (dom/i )
    (dom/h4
      (dom/a {:href (var-url func)}
        name " (" ns ")"))
    (dom/p (ellipsis 225 doc))))

(defmethod ac-entry :macro [{:keys [name ns doc]}]
  (dom/div
    (dom/h4
      name " (" ns ")")
    (dom/p (ellipsis 225 doc))))

(defmethod ac-entry :ns [{:keys [ns doc]}]
  (dom/div
    (dom/h4
      (dom/a {:href "#"} ns))
    (dom/p (ellipsis 225 doc))))

(defmethod ac-entry :page [{:keys [title desc]}]
  (dom/div
    (dom/h4
      (dom/a {:href "#"} title))
    (dom/p (ellipsis 255 desc))))

(defmethod ac-entry :default [{:keys [type]}]
  (.log js/console (str "Couldn't render ac entry:" type)))

(defn put-text [e text-chan owner]
  (let [text (.. e -target -value)]
    (put! text-chan text))
  (om/set-state! owner :loading? true))

(defn navigate-to [url]
  (aset (.-location js/window) "href" url))

(defn quick-lookup [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [ac-chan (or (om/get-state owner :ac-chan) (chan))]
        (go
          (while true
            (om/set-state! owner :autocomplete (<! ac-chan))
            (om/set-state! owner :loading? false)))))
    om/IRenderState
    (render-state [this {:keys [text-chan autocomplete loading? text] :or {text-chan (chan)}}]
      (dom/form {:class "search" :autoComplete "off" :on-submit identity}
        (dom/input {:class (str "form-control" (when loading? " loading"))
                    :placeholder "Looking for?"
                    :name "query"
                    :autoComplete "off"
                    :autoFocus "autofocus"
                    :on-input #(put-text % text-chan owner)})
        (dom/ul {:class "ac-results"}
          (for [{:keys [type href] :as res} autocomplete]
            (dom/li {:on-click #(when href (navigate-to href))}
              (ac-entry res))))
        (dom/div {:class "not-finding"}
          "Can't find what you're looking for? "
          (dom/a {:href "search-feedback"} "Help make ClojureDocs better")
          ".")))))

(def test-ac
  [{:type :function
    :name "map"
    :ns "clojure.core"
    :doc "Returns a lazy sequence consisting of the result of applying f to the
set of first items of each coll, followed by applying f to the set
of second items in each coll, until any one of the colls is
exhausted. Any remaining items in other colls are ignored. Function
f should accept number-of-colls arguments."}
   {:type :macro
    :name "apply"
    :ns "clojure.core"
    :doc "Applies fn f to the argument list formed by prepending intervening arguments to args."}])

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

(defn format-ac-result [res]
  (-> res
      (assoc :type :function)
      (assoc :href (var-url res))))

(defn wire-search [text-chan ac-chan]
  (go
    (while true
      (let [ac-text (<! text-chan)
            ac-response (<! (ajax-chan {:method :get
                                        :path (str "/search?query=" (url-encode ac-text))
                                        :data-type :edn}))
            data (-> ac-response :res :body)]
        (when (:success ac-response)
          (->> data
               (map format-ac-result)
               (>! ac-chan)))))))

(def app-state
  (atom (reader/read-string (aget js/window "PAGE_DATA"))))

#_(def app-state (atom {:examples []
                      :var {:name "hi"}}))

(def init
  [[:div.search-widget]
   (fn [$el]
     (let [text-chan (chan)
           ac-chan (chan)]
       (wire-search text-chan ac-chan)
       (om/root
         quick-lookup
         {}
         {:target $el
          :init-state {:text-chan text-chan
                       :ac-chan ac-chan}})))

   [:div.add-example-widget]
   (fn [$el]
     (om/root
       add-example
       {}
       {:target $el}))])
