(ns clojuredocs.notes
  (:require [clojure.string :as str]
            [clojuredocs.anim :as anim]
            [clojuredocs.util :as util]
            [dommy.core :as dommy :refer-macros [sel1]]
            [nsfw.ops :as ops]
            [reagent.core :as rea]))

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

(defn $tabbed-markdown-editor
  [!state bus]
  (let [{:keys [error text create-success? loading? active] :as app} @!state]
    (rea/create-class
      {:component-did-update (fn [this prev]
                               (when (and (not (-> prev second :expanded?))
                                          (-> this rea/argv second :expanded?))
                                 (let [$el (dommy/sel1
                                             (rea/dom-node this)
                                             "textarea")]
                                   (.focus $el))))
       :reagent-render
       (fn []
         (let [{:keys [text active]
                :or {active :editor}} @!state]
           [:div.tabbed-editor
            [:ul.nav.nav-tabs
             [:li
              {:class (when (= :editor active) "active")}
              [:a {:href "#"
                   :on-click (fn [e]
                               (.preventDefault e)
                               (swap! !state assoc :active :editor)
                               nil)}
               [:i.fa.fa-code] " Editor"]]
             [:li
              {:class (when (= :preview active) "active")}
              [:a {:href "#"
                   :on-click (fn [e]
                               (.preventDefault e)
                               (swap! !state assoc :active :preview)
                               nil)}
               [:i.fa.fa-eye] " Preview"]]]
            [:div {:class (when (= :preview active) "hidden")}
             [:div.example-editor
              {:class (when loading? "disabled")}
              ($expando-ta
                text
                {:on-change (fn [e]
                              (let [v (.. e -target -value)]
                                (swap! !state assoc :text v)
                                (ops/send bus ::text-change v)
                                false))
                 :value text
                 :disabled (when loading? "disabled")
                 :placeholder "Note Here"})
              [:pre.columns-guide (eighty-columns)]]]
            [:div.live-preview.markdown
             {:class (when (= :editor active) "hidden")}
             (if-not (empty? text)
               [:div {:dangerouslySetInnerHTML {:__html (util/markdown text)}}]
               [:div.null-state "Live Preview"])]]))})))

(defn $add [!state bus]
  (let [{:keys [expanded? body text error loading?]} @!state
        comp (rea/current-component)]
    [:div.add-note
     [:div.toggle-controls
      (if true
        [:a.toggle-link
         {:href "#"
          :on-click (fn [e]
                      (.preventDefault e)
                      (swap! !state assoc :expanded? (not expanded?))
                      (when (not expanded?)
                        (anim/scroll-to
                          (rea/dom-node comp)
                          {:pad 10}))
                      nil)}
         (if-not expanded?
           "Add Note"
           "Collapse")]
        [:span.muted "log in to add a note"])]
     [:div.add-note-content {:class (when-not expanded? " hidden")}
      [:h5 "New Note"]
      [$tabbed-markdown-editor !state bus]
      [:p.instructions "Markdown allowed, code in <pre />."]
      (when error
        [:div.form-group
         [:div.error-message.text-danger
          [:i.fa.fa-exclamation-circle]
          error]])
      [:div.add-example-controls.form-group.clearfix
       [:button {:class "btn btn-default"
                 :disabled (when loading? "disabled")
                 :on-click (fn [e]
                             (.preventDefault e)
                             (swap! !state
                               assoc
                               :expanded? false
                               :text nil)
                             nil)}
        "Cancel"]
       [:button {:class "btn btn-success pull-right"
                 :disabled (when (or loading? (empty? text)) "disabled")
                 :on-click (fn [e]
                             (.preventDefault e)
                             (ops/send bus ::new text)
                             nil)}
        "Add Note"]
       [:img.loading.pull-right
        {:class (when-not loading? " hidden")
         :src "/img/loading.gif"}]]]]))

(defn $edit-note [{:keys [_id error body loading? editing?] :as note} bus]
  (let [!local (rea/atom {:text body
                          :editing? editing?})]
    (fn []
      (let [{:keys [text editing?]} @!local]
        [:div
         [$tabbed-markdown-editor !local bus]
         [:p.instructions "Markdown allowed, code in <pre />."]
         (when error
           [:div.form-group
            [:div.error-message.text-danger
             [:i.fa.fa-exclamation-circle]
             error]])
         [:div.add-example-controls.form-group.clearfix
          [:button {:class "btn btn-default"
                    :disabled (when loading? "disabled")
                    :on-click (fn [e]
                                (.preventDefault e)
                                (swap! !local assoc
                                  :editing? false
                                  :text nil)
                                nil)}
           "Cancel"]
          [:button {:class "btn btn-success pull-right"
                    :disabled (when loading? "disabled")
                    :on-click (fn [e]
                                (.preventDefault e)
                                (ops/send bus ::update {:text text :_id _id})
                                nil)}
           "Update Note"]
          [:img.loading.pull-right
           {:class (when-not loading? " hidden")
            :src "/img/loading.gif"}]]]))))

(defn $note [!state bus]
  (fn []
    (let [{:keys [body author created-at updated-at can-delete?
                  can-edit? editing? delete-state _id] :as note} @!state]
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
                  :on-click (fn [e]
                              (.preventDefault e)
                              (swap! !state assoc :editing? false)
                              nil)}
              "cancel edit"]
             [:a {:href "#"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (swap! !state assoc :editing? true)
                              nil)}
              "edit"])])
        (when (and can-delete? (not editing?))
          [:span
           " / "
           (if (get #{:confirm :loading} delete-state)
             (if (= :loading delete-state)
               [:img.loading {:src "/img/loading.gif"}]
               [:span
                [:a {:href "#"
                     :on-click (fn [e]
                                 (.preventDefault e)
                                 (swap! !state assoc :delete-state :none)
                                 nil)}
                 "cancel"]
                " | "
                [:a {:href "#"
                     :on-click (fn [e]
                                 (.preventDefault e)
                                 (ops/send bus ::delete _id)
                                 nil)}
                 "confirm delete?"]])
             [:span
              {:class (when (= :error delete-state) "error-deleting bg-danger")}
              [:a {:href "#"
                   :on-click (fn [e]
                               (.preventDefault e)
                               (swap! !state assoc :delete-state :confirm)
                               nil)}
               "delete"]])])]
       (if editing?
         [$edit-note note bus]
         [:div.note-body.markdown
          {:dangerouslySetInnerHTML
           {:__html
            (-> body
                util/markdown
                (str/replace #"<pre><code>" "<pre>")
                (str/replace #"</code></pre>" "</pre>")
                (str/replace #"<pre>" "<pre class=\"syntaxify\">"))}}])])))

(defn $notes [!state bus]
  (let [{:keys [notes var user add-note] :as app} @!state]
    [:div.var-notes
     [:h5 (util/pluralize (count notes) "Note" "Notes")]
     [:div
      (if (empty? notes)
        [:div.null-state "No notes for " [:code (:name var)]]
        [:ul
         (->> notes
              (map-indexed
                (fn [i note]
                  (with-meta
                    [$note (rea/cursor !state [:notes i]) bus]
                    {:key (:_id note)}))))])]
     (if user
       [$add (rea/cursor !state [:add-note]) bus]
       [:div.login-required-message
        "Log in to add a note"])]))
