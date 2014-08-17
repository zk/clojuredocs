(ns clojuredocs.examples
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [sablono.core :as sab :refer-macros [html]]
            [clojuredocs.util :as util]
            [clojuredocs.schemas :as schemas]
            [clojure.data :refer [diff]])
  (:require-macros [dommy.macros :refer [node sel1]]))

(defn client-call [comp context payload success error]
  (let [{:keys [api-root]} comp
        {:keys [http-method]} (-> comp :contexts context)]
    (ajax
      {:path (str "/api" api-root)
       :method http-method
       :data-type :edn
       :data payload
       :success success
       :error error})))

(defn validate-and-submit [app owner payload]
  (om/set-state! owner :loading? true)
  (om/set-state! owner :error-message nil)
  (client-call schemas/ExampleComp :create
    (-> payload
        (update-in [:var] #(select-keys % [:ns :name :library-url]))
        (assoc :history []))
    (fn [data]
      (om/transact! app (fn [app]
                          (update-in app [:examples] (fn [es] (vec
                                                                (concat
                                                                  es
                                                                  [(assoc (:body data)
                                                                     :new? true)]))))))
      (om/set-state! owner :loading? false)
      (om/set-state! owner :text nil)
      (om/set-state! owner :expanded? false))
    (fn [{:keys [status body]}]
      (om/set-state! owner :loading? false)
      (cond
        (= 422 status)
        (om/update-state! owner #(merge % body))

        :else
        (om/set-state! owner :error-message "There was a problem contacting the server."))))
  false)

(defn update-example [{:keys [_id var]} owner text]
  (om/set-state! owner :loading? true)
  (om/set-state! owner :error-message nil)
  (client-call schemas/ExampleComp :update
    {:var (select-keys var [:ns :name :library-url])
     :example-id _id
     :body text}
    (fn [data]
      (om/transact! app (fn [app]
                          (update-in app [:examples] (fn [es] (concat es [(assoc (:body data)
                                                                            :new? true)])))))
      (om/set-state! owner :loading? false)
      (om/set-state! owner :text nil)
      (om/set-state! owner :expanded? false))
    (fn [{:keys [status body]}]
      (om/set-state! owner :loading? false)
      (cond
        (= 422 status)
        (om/update-state! owner #(merge % body))

        :else
        (om/set-state! owner :error-message "There was a problem contacting the server."))))
  false)

(defn update-text [e owner on-text]
  (let [new-text (.. e -target -value)]
    (om/set-state! owner :text new-text)
    (when on-text (on-text new-text)))
  false)

(defn cancel-clicked [e example]
  (om/update! example [:editing?] false)
  false)

(defn $toggle-controls [{:keys [editing?] :as example}]
  [:div.toggle-controls
   [:a.toggle-link {:href "#"
                    :on-click (fn []
                                (om/transact!
                                  example
                                  #(assoc %
                                     :editing? (not editing?)
                                     :should-focus? true))
                                false)}
    (if-not editing? "Add an Example" "Collapse")]])

(defn $editor [{:keys [var body loading?] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:text body})
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
          (.focus (om/get-node owner "textarea"))))
      (update-preview owner))

    om/IRenderState
    (render-state [_ {:keys [text on-text]}]
      (html
        [:div.example-editor
         [:div.live-preview {:ref "live-preview"}]
         [:textarea
          {:class "form-control"
           :cols 80 :on-input #(update-text % owner on-text)
           :ref "textarea"
           :disabled (when loading? "disabled")}]
         [:p.instructions
          "See our "
          [:a {:href "/examples-styleguide"} "examples style guide"]
          " for content and formatting guidelines. "
          "Examples submitted to ClojureDocs are licensed under the "
          [:a {:href "https://creativecommons.org/publicdomain/zero/1.0/"}
           "Creative Commons CC0 license"]
          "."]]))))

(defn $add-example-editor [{:keys [var loading? body] :as example} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [error-message] :as state}]
      (html
        [:div.add-example-content
         [:h5 "New Example"]
         [:form
          {:on-submit #(validate-and-submit
                         example
                         owner
                         {:body body
                          :var @var})}
          (om/build $editor example {:init-state {:on-text #(om/update! example [:body] %)} :state state})
          (when error-message
            [:div.form-group
             [:div.error-message.text-danger
              [:i.fa.fa-exclamation-circle]
              error-message]])
          [:div.add-example-controls.form-group.clearfix
           [:button.btn.btn-default
            {:disabled (when loading? "disabled")
             :on-click #(cancel-clicked % example)}
            "Cancel"]
           [:button.btn.btn-success.pull-right
            {:disabled (when loading? "disabled")}
            "Add Example"]
           [:img.loading.pull-right
            {:class (when-not loading? " hidden")
             :src "/img/loading.gif"}]]]]))))

(defn $update-example-editor [{:keys [var loading? body] :as example} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [error-message text] :as state}]
      (html
        [:div.add-example-content
         [:h5 "Edit Example"]
         [:form
          {:on-submit #(validate-and-submit
                         example
                         owner
                         {:body body
                          :var @var})}
          (om/build $editor example {:init-state {:on-text #(om/set-state! owner text %)} :state state})
          (when error-message
            [:div.form-group
             [:div.error-message.text-danger
              [:i.fa.fa-exclamation-circle]
              error-message]])
          [:div.add-example-controls.form-group.clearfix
           [:button.btn.btn-default
            {:disabled (when loading? "disabled")
             :on-click #(cancel-clicked % example)}
            "Cancel"]
           [:button.btn.btn-success.pull-right
            {:disabled (when loading? "disabled")}
            "Update Example"]
           [:img.loading.pull-right
            {:class (when-not loading? " hidden")
             :src "/img/loading.gif"}]]]]))))

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

(defn $add [{:keys [editing? should-focus?] :as example} owner]
  (reify
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (when (and should-focus? editing?)
        (om/transact! example #(assoc % :should-focus? false))
        (anim/scroll-to (om/get-node owner "wrapper") {:pad 10})))
    om/IRenderState
    (render-state [this {:keys [text loading?] :as state}]
      (sab/html
        [:div.add-example {:ref "wrapper"}
         ($toggle-controls example)
         [:div.add-example-content {:class (when-not editing? " hidden")}
          (om/build $add-example-editor example {:state state})]]))))

(defn user-can-delete? [user {:keys [author]}]
  (= (select-keys user [:login :account-source])
     (select-keys author [:login :account-source])))

(defn update-example-body [owner {:keys [body]}]
  (let [el (om/get-node owner "example-body")
        pre (node [:pre.raw-example {:class "brush: clojure"}
                   body])]
    (dommy/clear! el)
    (dommy/append! el pre)
    (try
      (.highlight js/SyntaxHighlighter pre)
      ;; Not handling this error prevents subsequent
      ;; highlights from succeeding
      (catch js/Error e (prn "Error highlighting example")))))

(defn delete-example [owner ex del-fn]
  (om/set-state! owner :delete-state :loading)
  (ajax
    {:path (str "/api/examples")
     :method :delete
     :data-type :edn
     :data @ex
     :success (fn [data]
                (om/set-state! owner :delete-state :none)
                (del-fn @ex))
     :error (fn [resp]
              (om/set-state! owner :delete-state :error))})
  false)

(defn $example-meta [{:keys [editing? author editors can-delete?] :as ex} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ex delete-state]}]
      (let [authors (distinct
                      (concat
                        [author]
                        editors))
            num-to-show 7]
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
            [:a {:href (str "#example_" _id)}
             "permalink"]
            " / "
            [:a {:href (str "/ex/" _id)}
             "history"]
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
                                false)}
               "edit"])
            (when (and can-delete? (not editing?))
              [:span
               " / "
               (if (get #{:confirm :loading} delete-state)
                 (if (= :loading delete-state)
                   [:img.loading {:src "/img/loading.gif"}]
                   [:span
                    [:a {:href "#"
                         :on-click #(do
                                      (om/set-state! owner :delete-state :none)
                                      false)}
                     "cancel"]
                    " | "
                    [:a {:href "#"
                         :on-click #(delete-example owner ex delete-ex)}
                     "confirm delete?"]
                    ])
                 [:span
                  {:class (when (= :error delete-state) "error-deleting bg-danger")}
                  [:a {:href "#"
                       :on-click #(do
                                    (om/set-state! owner :delete-state :confirm)
                                    false)}
                   "delete"]])])
            [:span.edit-example-widget]]])))))

(defn $example-body [ex owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (update-example-body owner ex))
    om/IDidUpdate
    (did-update [_ _ _]
      (update-example-body owner ex))
    om/IRender
    (render [_]
      (html
        [:div.example-body {:ref "example-body" :class (when new? "just-created")}]))))

(defn $example [{:keys [editing? body _id author history created-at updated-at new? can-delete?] :as ex} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ex]}]
      (html
        [:div.var-example
         [:a {:id (str "example_" _id)}]
         [:div
          (om/build $example-meta ex {:init-state {:delete-ex delete-ex}})]
         (if editing?
           (om/build $update-example-editor ex)
           (om/build $example-body ex))]))))

(defn $examples [{:keys [examples var user] :as app} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ex]}]
      (html
        [:div.var-examples
         [:h5 (util/pluralize (count examples) "Example" "Examples")]
         (if (empty? examples)
           [:div.null-state
            "No examples for " (:ns var) "/" (:name var) "."]
           (om/build-all
             $example
             (->> (:examples app)
                  (map #(assoc % :can-delete? (user-can-delete? user %))))
             {:init-state {:delete-ex delete-ex}}))
         (if user
           (om/build $add app)
           [:div.login-required-message
            "Log in to add an example"])]))))
