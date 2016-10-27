(ns clojuredocs.examples
  (:require [dommy.core :as dommy :refer-macros [sel1]]
            [reagent.core :as rea]
            [nsfw.ops :as ops]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put!
                     alts! timeout pipe mult tap]]
            [clojuredocs.util :as util]
            [nsfw.util :as nu]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojuredocs.syntax :as syntax]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [clojure.data :refer [diff]])
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
        :autoFocus "autofocus"
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

(defn $tabbed-clojure-editor
  [!editor bus]
  (let [{:keys [text body error create-success? loading? active]
         :or {active :editor}} @!editor
         text (or text body)]
    [:div.tabbed-editor
     [:ul.nav.nav-tabs
      [:li
       {:class (when (= :editor active) "active")}
       [:a {:href "#"
            :on-click (fn [e]
                        (.preventDefault e)
                        (swap! !editor assoc :active :editor)
                        nil)}
        [:i.fa.fa-code] " Editor"]]
      [:li
       {:class (when (= :preview active) "active")}
       [:a {:href "#"
            :on-click (fn [e]
                        (.preventDefault e)
                        (swap! !editor assoc :active :preview)
                        nil)}
        [:i.fa.fa-eye] " Preview"]]]
     [:div {:class (when (= :preview active) "hidden")}
      [:div.example-editor
       {:class (when loading? "disabled")}
       ($expando-ta
         text
         {:on-change (fn [e]
                       (let [v (.. e -target -value)]
                         (.preventDefault e)
                         (swap! !editor assoc :text v)
                         false))
          :value text
          :disabled (when loading? "disabled")
          :placeholder "Code Here"})
       [:pre.columns-guide (eighty-columns)]]]
     [:div.live-preview {:class (when (= :editor active) "hidden")}
      (if-not (empty? text)
        (syntax/syntaxify text)
        [:div.null-state "Live Preview"])]]))

(defn user-can-delete? [user {:keys [author]}]
  (= (select-keys user [:login :account-source])
     (select-keys author [:login :account-source])))

(defn $example-meta [{:keys [can-delete? can-edit?]}
                     !state
                     bus]
  (let [{:keys [preview-text delete-state editing? _id author editors]} @!state
        authors (distinct
                  (concat
                    [author]
                    editors))
        num-to-show 7]
    [:div.example-meta
     [:div.contributors
      (->> authors
           (take num-to-show)
           (map util/$avatar))
      (when (> (count authors) num-to-show)
        [:div.contributors
         "+ "
         (- (count authors) num-to-show)
         " more"])]
     [:div.links
      [:a {:href (str "#example-" _id)}
       "link"]
      #_" / "
      #_[:a {:href (str "/ex/" _id)}
         "history"]
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
                            (swap! !state assoc :editing? true :text nil)
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
             "delete"]])])
      [:span.edit-example-widget]]]))

(def $example-instructions
  [:p.example-instructions
   "See our "
   [:a {:href "/examples-styleguide"} "examples style guide"]
   " for content and formatting guidelines. "
   "Examples submitted to ClojureDocs are licensed under the "
   [:a {:href "https://creativecommons.org/publicdomain/zero/1.0/"}
    "Creative Commons CC0 license"]
   "."])

(defn $example-editor [{:keys [submit-button-text]}
                       !state
                       bus]
  (let [{:keys [editing? loading? error var body text] :as ex} @!state
        text (or body text)]
    [:div
     [$tabbed-clojure-editor !state bus]
     $example-instructions
     (when error
       [:div.form-group
        [:div.error-message.text-danger
         [:i.fa.fa-exclamation-circle]
         error]])
     [:div.add-example-controls.form-group.clearfix
      [:button.btn.btn-default
       {:disabled (when loading? "disabled")
        :on-click (fn [e]
                    (.preventDefault e)
                    (swap! !state assoc :editing? false :text nil)
                    nil)}
       "Cancel"]
      [:button.btn.btn-success.pull-right
       {:disabled (when loading? "disabled")
        :on-click (fn [e]
                    (.preventDefault e)
                    (ops/send bus ::save {:var var :text text})
                    nil)}
       (or submit-button-text "Submit")]
      [:img.loading.pull-right
       {:class (when-not loading? " hidden")
        :src "/img/loading.gif"}]]]))


;; Example API
;; + [$example opts bus]

(defn $example [{:keys [can-delete? can-edit?] :as ex}
                !state
                bus]
  (let [{:keys [body editing? _id] :as ex} @!state]
    [:div.var-example
     {:class (if (= (str "example-" _id) (util/location-hash))
               "highlighted")}
     [:a {:id (str "example-" _id)}]
     [:div
      [$example-meta
       {:editing? editing?
        :can-delete? can-delete?
        :can-edit? can-edit?}
       !state
       bus]]
     (if editing?
       [:div
        [:h5 "Edit Example"]
        [$example-editor
         {:submit-button-text "Update Example"}
         !state
         bus]]
       (when body
         [:div.example-body
          (syntax/syntaxify body)]))]))

(defn $create-example [!state bus]
  (let [{:keys [editing? should-focus? var] :as app} @!state]
    [:div.add-example {:ref "wrapper"}
     [:div.toggle-controls
      [:a.toggle-link
       {:href "#"
        :on-click (fn [e]
                    (.preventDefault e)
                    (swap! !state update-in [:editing?] not)
                    nil)}
       (if-not editing? "Add an Example" "Collapse")]]
     [:div.add-example-content {:class (when-not editing? " hidden")}
      [$example-editor
       {:submit-button-text "Add Example"}
       !state
       bus]]]))

(defn $examples [!state bus]
  (let [{:keys [user examples var] :as app} @!state]
    [:div.var-examples
     [:h5 (util/pluralize (count examples) "Example" "Examples")]
     (if (empty? examples)
       [:div.null-state
        "No examples for " (:ns var) "/" (:name var) "."]
       (->> examples
            (map-indexed
              (fn [i example]
                (with-meta
                  [$example
                   {:can-delete? (user-can-delete? user example)
                    :can-edit? (not (nil? user))}
                   (rea/cursor !state [:examples i]) bus]
                  {:key (:_id example)})))))
     (if user
       [$create-example
        (rea/cursor !state [:add-example])
        bus]
       [:div.login-required-message
        "Log in to add an example"])]))

(defn $examples-widget [!state bus]
  [$examples !state bus])
