(ns clojuredocs.notes
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojuredocs.examples :as examples]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [goog.crypt :as gcrypt]
            [goog.crypt.Md5 :as Md5]
            [goog.crypt.Sha1 :as Sha1]
            [sablono.core :as sab :include-macros true]
            [clojuredocs.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1]]))

(defn toggle [owner key]
  (om/update-state! owner (fn [state]
                            (assoc state key (not (get state key)))))
  false)

(defn set-expanded [owner expanded?]
  (om/set-state! owner :expanded? expanded?)
  (om/set-state! owner :should-focus? true)
  false)

(defn cancel-clicked [e owner]
  (set-expanded owner (not (om/get-state owner :expanded?)))
  (om/set-state! owner :text nil)
  false)

(defn update-preview [owner]
  (let [text (om/get-state owner :text)
        $preview (om/get-node owner "live-preview")
        el (node [:pre {:class "brush: clojure"} text])]
    (dommy/clear! $preview)
    (if-not (empty? text)
      (aset $preview
        "innerHTML"
        (-> text
            util/markdown
            (str/replace #"<pre>" "<pre class=\"brush: clojure\">")))
      (dommy/append! $preview
        (node [:div.empty-live-preview "Live Preview"])))))

(defn $add [{:keys [user]} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (update-preview owner))
    om/IDidUpdate
    (did-update [_ _ _]
      (.focus (om/get-node owner "input"))
      (update-preview owner))
    om/IRenderState
    (render-state [this {:keys [expanded? loading? text error-message]}]
      (sab/html
        [:div.add-note
         [:div.toggle-controls
          (if true
            [:a.toggle-link {:href "#" :on-click #(toggle owner :expanded?)}
             (if-not expanded?
               "Add Note"
               "Collapse")]
            [:span.muted "log in to add a note"])]
         [:div.add-note-content {:class (when-not expanded? " hidden")}
          [:div.live-preview {:ref "live-preview"}]
          [:form.add-note-form
           {:on-submit (constantly false) :autoComplete "off"}
           [:div.form-group
            [:textarea.form-control
             {:name "note-name"
              :ref "input"
              :on-input #(do
                           (om/set-state! owner :text (.. % -target -value))
                           false)}]
            [:p.instructions "Markdown allowed, code in <pre />."]
            (when error-message
              [:div.form-group
               [:div.error-message.text-danger
                [:i.fa.fa-exclamation-circle]
                error-message]])
            [:div.add-example-controls.form-group.clearfix
             [:button {:class "btn btn-default"
                       :disabled (when loading? "disabled")
                       :on-click #(cancel-clicked % owner)}
              "Cancel"]
             [:button {:class "btn btn-success pull-right"
                       :disabled (when loading? "disabled")}
              "Add Note"]
             [:img.loading.pull-right
              {:class (when-not loading? " hidden")
               :src "/img/loading.gif"}]]]]]]))))

(defn $note [{:keys [body author created-at]}]
  [:div.note
   [:div.note-meta
    "By "
    (util/$avatar author)
    ", "
    (util/timeago created-at)
    " ago."]
   [:div.note-body.markdown
    {:dangerouslySetInnerHTML
     {:__html
      (-> body
          util/markdown
          (str/replace #"<pre><code>" "<pre>")
          (str/replace #"</code></pre>" "</pre>")
          (str/replace #"<pre>" "<pre class=\"brush: clojure\">"))}}]])

(defn $notes [{:keys [notes var user] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div.var-notes
         [:h5 (util/pluralize (count notes) "Note" "Notes")]
         [:div
          (if (empty? notes)
            [:div.null-state "No notes for " [:code (:name var)]]
            [:ul
             (for [n notes]
               ($note n))])]
         (if user
           (om/build $add app)
           [:div.login-required-message
            "Log in to add a note"])]))))
