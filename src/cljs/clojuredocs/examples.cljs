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
  [{:keys [body error create-success? loading? _id] :as app} bus]
  (let [!editor (rea/atom {:text body
                           :error error
                           :create-success? create-success?
                           :loading? loading?
                           :active :editor})]
    (fn []
      (let [{:keys [text error create-success? loading? active]} @!editor]
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
            [:div.null-state "Live Preview"])]]))))

(defn user-can-delete? [user {:keys [author]}]
  (= (select-keys user [:login :account-source])
     (select-keys author [:login :account-source])))

(defn $example-meta [{:keys [_id editing? author editors
                             can-delete? can-edit? delete-state] :as ex}
                     bus]
  (let [!local (rea/atom {:preview-text nil})]
    (fn []
      (let [{:keys [preview-text]} @!local
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
                                (swap! !local assoc :editing? false)
                                nil)}
                "cancel edit"]
               [:a {:href "#"
                    :on-click (fn [e]
                                (.preventDefault e)
                                (swap! !local assoc
                                  :editing? true
                                  :preview-text nil)
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
                                   (swap! !local assoc :delete-state :none)
                                   nil)}
                   "cancel"]
                  " | "
                  [:a {:href "#"
                       :on-click (fn [e]
                                   (.preventDefault e)
                                   (ops/send bus ::delete (:_id @ex))
                                   nil)}
                   "confirm delete?"]])
               [:span
                {:class (when (= :error delete-state) "error-deleting bg-danger")}
                [:a {:href "#"
                     :on-click (fn [e]
                                 (.preventDefault e)
                                 (swap! !local assoc :delete-state :confirm)
                                 nil)}
                 "delete"]])])
          [:span.edit-example-widget]]]))))

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
                       {:keys [editing? loading? error var _id
                               text text-ch] :as app}
                       bus]
  (fn []
    [:div
     [$tabbed-clojure-editor app bus]
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
                    (ops/send bus ::cancel-editing)
                    nil)}
       "Cancel"]
      [:button.btn.btn-success.pull-right
       {:disabled (when loading? "disabled")
        :on-click (fn [e]
                    (.preventDefault e)
                    (ops/send bus ::update {:body text :_id _id})
                    nil)}
       (or submit-button-text "Submit")]
      [:img.loading.pull-right
       {:class (when-not loading? " hidden")
        :src "/img/loading.gif"}]]]))

(defn $example [{:keys [body editing? _id] :as ex} bus]
  [:div.var-example
   {:class (if (= (str "example-" _id) (util/location-hash))
             "highlighted")}
   [:a {:id (str "example-" _id)}]
   [:div
    [$example-meta ex bus]]
   (if editing?
     [:div
      [:h5 "Edit Example"]
      [$example-editor
       (merge ex {:submit-button-text "Update Example"})
       bus]]
     (when body
       [:div.example-body
        (syntax/syntaxify body)]))])

(defn build-examples [user examples bus]
  [:div
   (->> examples
        (map-indexed
          (fn [i ex]
            (with-meta
              [$example
               (merge
                 ex
                 {:can-delete? (user-can-delete? user ex)
                  :can-edit? (not (nil? user))})
               bus]
              {:key i}))))])

(defn $create-example [{:keys [editing? should-focus? var] :as app} bus]
  [:div.add-example {:ref "wrapper"}
   [:div.toggle-controls
    [:a.toggle-link
     {:href "#"
      :on-click (fn [e]
                  (.preventDefault e)
                  #_(om/transact!
                      app
                      #(assoc %
                         :editing? (not editing?)
                         :should-focus? true))
                  nil)}
     (if-not editing? "Add an Example" "Collapse")]]
   [:div.add-example-content {:class (when-not editing? " hidden")}
    [$example-editor
     {:submit-button-text "Add Example"}
     app
     bus]]]
  #_(reify
      om/IDidUpdate
      (did-update [this prev-props prev-state]
        (when (and should-focus? editing?)
          (om/transact! app #(assoc % :should-focus? false))
          (anim/scroll-to (om/get-node owner "wrapper") {:pad 10})))

      om/IRenderState
      ))

(defn $examples [!state bus]
  (let [{:keys [user examples var] :as app} @!state]
    [:div.var-examples
     [:h5 (util/pluralize (count examples) "Example" "Examples")]
     (if (empty? examples)
       [:div.null-state
        "No examples for " (:ns var) "/" (:name var) "."]
       (build-examples user examples bus))
     (if user
       [$create-example
        (:add-example app)
        bus]
       [:div.login-required-message
        "Log in to add an example"])]))
