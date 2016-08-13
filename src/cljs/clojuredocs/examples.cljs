(ns clojuredocs.examples
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy :refer-macros [sel1]]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put!
                     alts! timeout pipe mult tap]]
            [clojuredocs.util :as util]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojuredocs.syntax :as syntax]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [sablono.core :as sab :refer-macros [html]]
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

(defn $tabbed-clojure-editor [{:keys [body error create-success? loading? _id] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:active :editor
       :text body})
    om/IRenderState
    (render-state [_ {:keys [text-ch active submit-btn-text text preview-state]}]
      (let [text-ch (or text-ch (chan))]
        (html
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
                :placeholder "Code Here"})
             [:pre.columns-guide (eighty-columns)]]]
           [:div.live-preview {:class (when (= :editor active) "hidden")}
            (if-not (empty? text)
              (syntax/syntaxify text)
              [:div.null-state "Live Preview"])]])))))

(defn user-can-delete? [user {:keys [author]}]
  (= (select-keys user [:login :account-source])
     (select-keys author [:login :account-source])))

(defn $example-meta [{:keys [_id editing? author
                             editors can-delete? can-edit?
                             delete-state] :as ex} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ch]}]
      (let [authors (distinct
                      (concat
                        [author]
                        editors))
            num-to-show 7
            delete-ch (or delete-ch (chan))]
        (html
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
                      :on-click #(do
                                   (om/update! ex [:editing?] false)
                                   false)}
                  "cancel edit"]
                 [:a {:href "#"
                      :on-click #(do
                                   (om/update! ex [:editing?] true)
                                   (om/update! ex [:preview-text] nil)
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
                         :on-click #(do (om/update! ex :delete-state :none)
                                        false)}
                     "cancel"]
                    " | "
                    [:a {:href "#"
                         :on-click #(do
                                      (put! delete-ch (:_id @ex))
                                      false)}
                     "confirm delete?"]])
                 [:span
                  {:class (when (= :error delete-state) "error-deleting bg-danger")}
                  [:a {:href "#"
                       :on-click #(do (om/update! ex :delete-state :confirm)
                                      false)}
                   "delete"]])])
            [:span.edit-example-widget]]])))))

(def $example-instructions
  [:p.example-instructions
   "See our "
   [:a {:href "/examples-styleguide"} "examples style guide"]
   " for content and formatting guidelines. "
   "Examples submitted to ClojureDocs are licensed under the "
   [:a {:href "https://creativecommons.org/publicdomain/zero/1.0/"}
    "Creative Commons CC0 license"]
   "."])

(defn $example-editor [{:keys [editing? loading? error var _id] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:text-ch (chan)})

    om/IWillMount
    (will-mount [_]
      (go-loop []
        (when-let [text (<! (om/get-state owner :text-ch))]
          (om/set-state! owner :text text)
          (recur))))
    om/IRenderState
    (render-state [_ {:keys [example-ch submit-button-text text text-ch] :as state}]
      (let [example-ch (or example-ch (chan))]
        (html
          [:div
           (om/build $tabbed-clojure-editor app {:init-state {:text-ch text-ch}})
           $example-instructions
           (when error
             [:div.form-group
              [:div.error-message.text-danger
               [:i.fa.fa-exclamation-circle]
               error]])
           [:div.add-example-controls.form-group.clearfix
            [:button.btn.btn-default
             {:disabled (when loading? "disabled")
              :on-click #(do
                           (om/update! app :editing? false)
                           false)}
             "Cancel"]
            [:button.btn.btn-success.pull-right
             {:disabled (when loading? "disabled")
              :on-click #(do
                           (put! example-ch {:body text :_id _id})
                           false)}
             (or submit-button-text "Submit")]
            [:img.loading.pull-right
             {:class (when-not loading? " hidden")
              :src "/img/loading.gif"}]]])))))

(defn $example [{:keys [body editing? _id] :as ex} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ch update-example-ch]}]
      (html
        [:div.var-example
         {:class (if (= (str "example-" _id) (util/location-hash))
                   "highlighted")}
         [:a {:id (str "example-" _id)}]
         [:div
          (om/build $example-meta ex {:init-state {:delete-ch delete-ch}})]
         (if editing?
           [:div
            [:h5 "Edit Example"]
            (om/build $example-editor ex
              {:init-state {:submit-button-text "Update Example"
                            :example-ch update-example-ch}})]
           (when body
             [:div.example-body
              (syntax/syntaxify body)]))]))))

(defn build-examples [user examples state]
  (om/build-all
    $example
    (->> examples
         (map #(assoc % :can-delete? (user-can-delete? user %)))
         (map #(assoc % :can-edit? (not (nil? user)))))
    {:init-state state}))

(defn $create-example [{:keys [editing? should-focus? var] :as app} owner]
  (reify
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when (and should-focus? editing?)
        (om/transact! app #(assoc % :should-focus? false))
        (anim/scroll-to (om/get-node owner "wrapper") {:pad 10})))

    om/IRenderState
    (render-state [this {:keys [text loading? example-ch] :as state}]
      (html
        [:div.add-example {:ref "wrapper"}
         [:div.toggle-controls
          [:a.toggle-link
           {:href "#"
            :on-click (fn []
                        (om/transact!
                          app
                          #(assoc %
                             :editing? (not editing?)
                             :should-focus? true))
                        false)}
           (if-not editing? "Add an Example" "Collapse")]]
         [:div.add-example-content {:class (when-not editing? " hidden")}
          (om/build $example-editor app
            {:init-state {:example-ch example-ch
                          :submit-button-text "Add Example"}})]]))))

(defn $examples [{:keys [user examples var] :as app} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ch new-example-ch] :as state}]
      (html
        [:div.var-examples
         [:h5 (util/pluralize (count examples) "Example" "Examples")]
         (if (empty? examples)
           [:div.null-state
            "No examples for " (:ns var) "/" (:name var) "."]
           (build-examples user examples state))
         (if user
           (om/build $create-example
               (:add-example app)
               {:init-state {:example-ch new-example-ch}})
             [:div.login-required-message
              "Log in to add an example"])]))))
