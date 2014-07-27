(ns clojuredocs.examples
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [sablono.core :as sab :refer-macros [html]]
            [clojuredocs.util :as util])
  (:require-macros [dommy.macros :refer [node sel1]]))

(defn validate-and-submit [app owner {:keys [text var]}]
  (om/set-state! owner :loading? true)
  (om/set-state! owner :error-message nil)
  (ajax
    {:path "/api/examples"
     :method :post
     :data-type :edn
     :data (merge var {:body text})
     :success (fn []
                (.reload js/location))
     :error (fn [{:keys [status body]}]
              (om/set-state! owner :loading? false)
              (cond
                (= 422 status)
                (om/set-state! owner :error-message (:error-message body))

                :else
                (om/set-state! owner :error-message "There was a problem contacting the server.")))})
  false)

(defn set-expanded [owner expanded?]
  (om/set-state! owner :expanded? expanded?)
  (om/set-state! owner :should-focus? true)
  false)

(defn update-text [e owner]
  (om/set-state! owner :text (.. e -target -value))
  false)

(defn cancel-clicked [e owner]
  (set-expanded owner (not (om/get-state owner :expanded?)))
  (om/set-state! owner :text nil)
  false)

(defn $toggle-controls [owner {:keys [expanded?]}]
  (dom/div {:class "toggle-controls"}
    (dom/a {:class "toggle-link"
            :href ""
            :on-click #(set-expanded owner (not expanded?))}
      (if-not expanded? "Add an Example" "Collapse"))))

(defn $editor [owner {:keys [expanded? text var loading? error-message] :as state}]
  (dom/div {:class (str "add-example-content" (when-not expanded? " hidden"))}
    (dom/h5 "New Example")
    (dom/div {:class "add-example-preview"}
      (dom/div {:ref "live-preview" :class "live-preview"}))
    (dom/form {:on-submit #(validate-and-submit app owner
                             {:text text
                              :var var})}
      (dom/textarea {:class "form-control"
                     :cols 80 :on-input #(update-text % owner)
                     :ref "textarea"
                     :disabled (when loading? "disabled")})
      (dom/p {:class "instructions"}
        "See our "
        (dom/a {:href "/examples-styleguide"} "examples style guide")
        " for content and formatting guidelines. "
        "Examples submitted to ClojureDocs are licensed under the "
        (dom/a {:href "https://creativecommons.org/publicdomain/zero/1.0/"}
          "Creative Commons CC0 license")
        ".")
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
          "Add Example")
        (dom/img {:class (str "pull-right loading" (when-not loading? " hidden"))
                  :src "/img/loading.gif"})))))

(defn update-preview [owner]
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

(defn $add [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (dommy/append!
        (om/get-node owner "live-preview")
        (node [:div.empty-live-preview "Live Preview"]))
      (update-preview owner)
      (dommy/set-value!
        (om/get-node owner "textarea")
        (om/get-state owner :text)))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (let [{:keys [should-focus? expanded? text]} (om/get-state owner)]
        (when (and should-focus? expanded?)
          (om/set-state! owner :should-focus? false)
          (.focus (om/get-node owner "textarea"))
          (anim/scroll-to (om/get-node owner "wrapper") {:pad 10})))
      (update-preview owner))

    om/IRenderState
    (render-state [this {:keys [expanded? text loading?] :as state}]
      (dom/div {:class "add-example" :ref "wrapper"}
        ($toggle-controls owner state)
        ($editor owner state)))))

(defn $example-body [{:keys [body]}]
  [:div.example-body
   [:pre.raw-example {:class "brush: clojure"} body]])

(defn $example [{:keys [body _id user history created-at updated-at] :as ex}]
  [:div.var-example
   [:div
    (let [users (distinct
                  (concat
                    [user]
                    (->> history
                         (map :user)
                         reverse)))
          num-to-show 7]
      [:div.example-meta
       [:div.contributors
        (->> users
             (take num-to-show)
             (map util/$avatar))
        (when (> (count users) 10)
          [:div.contributors
           "+ "
           (- (count users) num-to-show)
           " more"])]

       [:div.links
        [:a {:href (str "#example_" _id)}
         "permalink"]
        " / "
        [:a {:href (str "/ex/" _id)}
         "history"]
        [:span.edit-example-widget]]])]
   [:div
    [:a {:id (str "example_" _id)}]
    ($example-body ex)]])

(defn $examples [{:keys [examples var user] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div.var-examples
         [:h5 (util/pluralize (count examples) "Example" "Examples")]
         (if (empty? examples)
           [:div.null-state
            "No examples for " (:ns var) "/" (:name var) "."]
           (map $example examples))
         (if user
           (om/build $add app)
           [:div.login-required-message
            [:a {:href "#" #_(common/gh-auth-url uri)} "Log in"]
            " to add an example"])]))))
1
