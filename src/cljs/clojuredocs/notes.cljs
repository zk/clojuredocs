(ns clojuredocs.notes
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy :refer-macros [sel1]]
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
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn $expando-ta
  "A textarea the expands downward with the content (no scroll)"
  [text opts]
  (let [rows (Math/max
               (+ (->> text
                       (filter #(= "\n" %))
                       count)
                 3)
               10)]
    [:textarea
     (merge
       {:class "form-control"
        :autofocus "autofocus"
        :cols 80
        :rows rows
        :value text}
       opts)]))

(defn eighty-columns []
  (let [text " 80 columns "
        char "-"
        pre-text ";;"
        post-text ">"
        n (- 80 (count pre-text) (count text) (count post-text))
        pre-n (Math/ceil (/ n 2))
        post-n (Math/floor (/ n 2))]
    (str
      pre-text
      (apply str (repeat pre-n char))
      text
      (apply str (repeat post-n char))
      post-text)))

(defn $tabbed-markdown-editor [{:keys [body error create-success? loading? _id] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:active :editor
       :text body})
    om/IRenderState
    (render-state [_ {:keys [text-ch active text]}]
      (let [text-ch (or text-ch (chan))]
        (sab/html
          [:div.tabbed-editor
           [:ul.nav.nav-tabs
            [:li
             {:class (when (= :editor active) "active")}
             [:a {:href "#"
                  :on-click #(do
                               (om/set-state! owner :active :editor)
                               false)}
              [:i.fa.fa-code] " Editor"]]
            [:li
             {:class (when (= :preview active) "active")}
             [:a {:href "#"
                  :on-click #(do
                               (om/set-state! owner :active :preview)
                               false)}
              [:i.fa.fa-eye] " Preview"]]]
           [:div {:class (when (= :preview active) "hidden")}
            [:div.example-editor
             {:class (when loading? "disabled")}
             ($expando-ta
               text
               {:on-change #(let [v (.. % -target -value)]
                              (om/set-state! owner :text v)
                              (put! text-ch v)
                              false)
                :value text
                :disabled (when loading? "disabled")
                :placeholder "Note Here"})
             [:pre.columns-guide (eighty-columns)]]]
           [:div.live-preview.markdown
            {:class (when (= :editor active) "hidden")}
            (if-not (empty? text)
              [:div {:dangerouslySetInnerHTML {:__html (util/markdown text)}}]
              [:div.null-state "Live Preview"])]])))))

(defn toggle [owner key]
  (om/update-state! owner (fn [state]
                            (assoc state key (not (get state key)))))
  false)

(defn $add [{:keys [expanded? loading? text error] :as app} owner]
  (reify
    om/IInitState
    (init-state [_] {:text-ch (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (when-let [text (<! (om/get-state owner :text-ch))]
          (om/update! app :text text)
          (recur))))
    om/IRenderState
    (render-state [this {:keys [text-ch new-ch]}]
      (let [new-ch (or new-ch (chan))]
        (sab/html
          [:div.add-note
           [:div.toggle-controls
            (if true
              [:a.toggle-link
               {:href "#"
                :on-click #(do
                             (om/update! app :expanded? (not expanded?))
                             false)}
               (if-not expanded?
                 "Add Note"
                 "Collapse")]
              [:span.muted "log in to add a note"])]
           [:div.add-note-content {:class (when-not expanded? " hidden")}
            [:h5 "New Note"]
            (om/build $tabbed-markdown-editor app
              {:init-state {:text-ch text-ch}})
            [:p.instructions "Markdown allowed, code in <pre />."]
            (when error
              [:div.form-group
               [:div.error-message.text-danger
                [:i.fa.fa-exclamation-circle]
                error]])
            [:div.add-example-controls.form-group.clearfix
             [:button {:class "btn btn-default"
                       :disabled (when loading? "disabled")
                       :on-click #(do
                                    (om/update! app :expanded? false)
                                    (om/update! app :text nil)
                                    false)}
              "Cancel"]
             [:button {:class "btn btn-success pull-right"
                       :disabled (when loading? "disabled")
                       :on-click #(do
                                    (put! new-ch (or text ""))
                                    false)}
              "Add Note"]
             [:img.loading.pull-right
              {:class (when-not loading? " hidden")
               :src "/img/loading.gif"}]]]])))))

(defn $edit-note [{:keys [_id error body loading? editing?] :as note} owner]
  (reify
    om/IInitState
    (init-state [_] {:text-ch (chan)
                     :text body})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (when-let [text (<! (om/get-state owner :text-ch))]
          (om/set-state! owner :text text)
          (recur))))
    om/IRenderState
    (render-state [_ {:keys [edit-ch text-ch text]}]
      (let [edit-ch (or edit-ch (chan))]
        (sab/html
          [:div
           (om/build $tabbed-markdown-editor note
             {:init-state {:text-ch text-ch}})
           [:p.instructions "Markdown allowed, code in <pre />."]
           (when error
             [:div.form-group
              [:div.error-message.text-danger
               [:i.fa.fa-exclamation-circle]
               error]])
           [:div.add-example-controls.form-group.clearfix
            [:button {:class "btn btn-default"
                      :disabled (when loading? "disabled")
                      :on-click #(do
                                   (om/update! note :editing? false)
                                   (om/set-state! owner :text nil)
                                   false)}
             "Cancel"]
            [:button {:class "btn btn-success pull-right"
                      :disabled (when loading? "disabled")
                      :on-click #(do
                                   (put! edit-ch {:text text :_id _id})
                                   false)}
             "Update Note"]
            [:img.loading.pull-right
             {:class (when-not loading? " hidden")
              :src "/img/loading.gif"}]]])))))


(defn $note [{:keys [body author created-at updated-at can-delete?
                     can-edit? editing? delete-state] :as note} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ch edit-ch]}]
      (let [delete-ch (or delete-ch (chan))]
        (sab/html
          [:div.note
           [:div.note-meta
            "By "
            (util/$avatar author)
            ", created "
            (util/timeago created-at)
            " ago"
            (when (not= created-at updated-at)
              [:span
               ", updated "
               (util/timeago updated-at)
               " ago"])
            (when can-edit?
              [:span
               " / "
               (if editing?
                 [:a {:href "#"
                      :on-click #(do
                                   (om/update! note [:editing?] false)
                                   false)}
                  "cancel edit"]
                 [:a {:href "#"
                      :on-click #(do
                                   (om/update! note [:editing?] true)
                                   false)}
                  "edit"])])
            (when (and can-delete? (not editing?))
              [:span
               " / "
               (if (get #{:confirm :loading} delete-state)
                 (if (= :loading delete-state)
                   [:img.loading {:src "/img/loading.gif"}]
                   [:span
                    [:a {:href "#"
                         :on-click #(do (om/update! note :delete-state :none)
                                        false)}
                     "cancel"]
                    " | "
                    [:a {:href "#"
                         :on-click #(do
                                      (put! delete-ch (:_id @note))
                                      false)}
                     "confirm delete?"]])
                 [:span
                  {:class (when (= :error delete-state) "error-deleting bg-danger")}
                  [:a {:href "#"
                       :on-click #(do (om/update! note :delete-state :confirm)
                                      false)}
                   "delete"]])])]
           (if editing?
             (om/build $edit-note note {:init-state {:edit-ch edit-ch}})
             [:div.note-body.markdown
              {:dangerouslySetInnerHTML
               {:__html
                (-> body
                    util/markdown
                    (str/replace #"<pre><code>" "<pre>")
                    (str/replace #"</code></pre>" "</pre>")
                    (str/replace #"<pre>" "<pre class=\"syntaxify\">"))}}])])))))

(defn $notes [{:keys [notes var user add-note] :as app} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [new-ch delete-ch edit-ch]}]
      (sab/html
        [:div.var-notes
         [:h5 (util/pluralize (count notes) "Note" "Notes")]
         [:div
          (if (empty? notes)
            [:div.null-state "No notes for " [:code (:name var)]]
            [:ul
             (for [n notes]
               (om/build $note n {:init-state {:delete-ch delete-ch
                                               :edit-ch edit-ch}}))])]
         (if user
           (om/build $add add-note {:init-state {:new-ch new-ch}})
           [:div.login-required-message
            "Log in to add a note"])]))))
