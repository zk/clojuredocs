(ns clojuredocs.examples
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader])
  (:require-macros [dommy.macros :refer [node sel1]]))

(defn validate-and-submit [app owner example]
  (om/set-state! owner :loading? true)
  (ajax
    {:path "/api/examples"
     :method :post
     :data-type :edn})
  false)

(defn set-expanded [owner expanded?]
  (om/set-state! owner :expanded? expanded?)
  (om/set-state! owner :should-focus? true)
  false)

(defn update-text [e owner]
  (om/set-state! owner :text (.. e -target -value))
  false)

(defn cancel-clicked [e owner]
  (set-expanded owner (not expanded?))
  (om/set-state! owner :text nil)
  false)

(defn $toggle-controls [owner {:keys [expanded?]}]
  (dom/div {:class "toggle-controls"}
    (dom/a {:class "toggle-link"
            :href ""
            :on-click #(set-expanded owner (not expanded?))}
      (if-not expanded? "Add an Example" "Close"))))

(defn $editor [owner {:keys [expanded? text ns name] :as state}]
  (dom/div {:class (str "add-example-content" (when-not expanded? " hidden"))}
    (dom/h3 "New Example")
    (dom/div {:class "add-example-preview"}
      (dom/div {:ref "live-preview" :class "live-preview"}))
    (dom/form {:on-submit #(validate-and-submit app owner
                             {:text text
                              :ns ns
                              :name name})}
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
                     :on-click #(cancel-clicked % owner)}
          "Cancel")
        (dom/button {:class "btn btn-success pull-right"} "Add Example")
        (dom/img {:class (str "pull-right loading" (when-not loading? " hidden"))
                  :src "/img/loading.gif"})))))

(defn $add [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (dommy/append!
        (om/get-node owner "live-preview")
        (node [:div.empty-live-preview "Live Preview"])))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (prn (om/get-node owner))
      (let [{:keys [should-focus? expanded? text]} (om/get-state owner)]
        (when (and should-focus? expanded?)
          (om/set-state! owner :should-focus? false)
          (.focus (om/get-node owner "textarea"))
          (anim/scroll-to (om/get-node owner "wrapper") {:pad 10})))
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
    (render-state [this {:keys [expanded? text loading?] :as state}]
      (dom/div {:class "add-example" :ref "wrapper"}
        ($toggle-controls owner state)
        ($editor owner state)))))
