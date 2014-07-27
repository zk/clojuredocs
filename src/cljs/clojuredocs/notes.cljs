(ns clojuredocs.notes
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
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
            js/marked
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
    (render-state [this {:keys [expanded? text]}]
      (dom/div {:class "add-note"}
        (dom/div {:class "toggle-controls"}
          (if true
            (dom/a {:class "toggle-link" :href "#" :on-click #(toggle owner :expanded?)}
              (if-not expanded?
                "Add Note"
                "Collapse"))
            (dom/span {:class "muted"} "log in to add a note")))
        (dom/div {:class (str "add-note-content" (when-not expanded? " hidden"))}
          (dom/div {:class "live-preview" :ref "live-preview"})
          (dom/form {:on-submit (constantly false) :autoComplete "off" :class "add-note-form"}
            (dom/div {:class "form-group"}
              (dom/textarea
                {:class "form-control"
                 :name "note-name"
                 :ref "input"
                 :on-input #(do
                              (om/set-state! owner :text (.. % -target -value))
                              false)})
              (dom/p {:class "instructions"} "Markdown allowed, code in <pre />.")
              (when error-message
                (dom/div {:class "form-group"}
                  (dom/div {:class "error-message text-danger"}
                    (dom/i {:class "fa fa-exclamation-circle"})
                    error-message)))
              (dom/div {:class "add-example-controls clearfix form-group"}
                (dom/button {:class "btn btn-default"
                             :disabled (when loading? "disabled")
                             :on-click #(cancel-clicked % owner)}
                  "Cancel")
                (dom/button {:class "btn btn-success pull-right"
                             :disabled (when loading? "disabled")}
                  "Add Note")
                (dom/img {:class (str "pull-right loading" (when-not loading? " hidden"))
                          :src "/img/loading.gif"})))))))))

(defn $note [{:keys [body user created-at]} owner]
  [:div.note
   [:div.note-meta
    "By "
    (util/$avatar user)
    ", "
    (util/timeago created-at)
    " ago."]
   [:div.note-body
    {:dangerouslySetInnerHTML
     {:__html
      (-> (js/marked body)
          (str/replace #"<pre><code>" "<pre>")
          (str/replace #"</code></pre>" "</pre>")
          (str/replace #"<pre>" "<pre class=\"brush: clojure\">"))}}]])

(defn $notes [{:keys [notes var] :as app} owner]
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
         (om/build $add app)]))))
