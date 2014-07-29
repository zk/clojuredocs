(ns clojuredocs.see-alsos
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [sablono.core :as sab :include-macros true]
            [clojuredocs.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1]]))

(defn toggle [owner key]
  (om/update-state! owner (fn [state]
                            (assoc state key (not (get state key)))))
  false)

(defn $see-also [{:keys [ns name created-at doc user] :as sa}]
  [:div.col-sm-6.see-also
   [:div
    (util/$var-link ns name
      [:span.ns ns "/"]
      [:span.name name])]
   [:p
    (->> doc
         (take 100)
         (apply str))
    (when (> (count doc) 100)
      "...")]
   [:div.meta
    "Added by " [:a {:href (str "/u/" (:login user))} (:login user)]]])

(defn $add [{:keys [user]} owner]
  (reify
    om/IDidUpdate
    (did-update [_ _ _]
      (.focus (om/get-node owner "input")))
    om/IRenderState
    (render-state [this {:keys [expanded?]}]
      (sab/html
        [:div.add-see-also
         [:div.toggle-controls
          (if true
            [:a.toggle-link {:href "#" :on-click #(toggle owner :expanded?)}
             (if-not expanded?
               "Add See Also"
               "Collapse")]
            [:span.muted "log in to add a see also"])]
         [:div {:class (when-not expanded? "hidden")}
          [:form {:on-submit (constantly false) :autoComplete "off"}
           [:div.input-group
            [:input.form-control
             {:name "see-also-name"
              :ref "input"
              :placeholder "Var Name"}]
            [:span.input-group-btn
             [:button.btn.btn-success "Add See-Also"]]]]]]))))

(defn $see-alsos [{:keys [see-alsos var user] :as app} owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div
         [:h5 "See Also"]
         (if (empty? see-alsos)
           [:div.null-state
            "No see-alsos for " [:code (:ns var) "/" (:name var)]]
           (->> see-alsos
                (map $see-also)
                (partition-all 2)
                (map (fn [cs] [:div.row cs]))))
         (if user
           (om/build $add app)
           [:div.login-required-message
            "Log in to add a see-also"])]))))
