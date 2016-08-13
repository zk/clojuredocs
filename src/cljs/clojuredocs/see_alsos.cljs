(ns clojuredocs.see-alsos
  (:require [om.core :as om :include-macros true]
            [dommy.core :as dommy :refer-macros [sel1]]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [sablono.core :as sab :include-macros true]
            [clojuredocs.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn toggle [owner key]
  (om/update-state! owner (fn [state]
                            (assoc state key (not (get state key)))))
  false)

(defn $see-also [{:keys [from-var to-var
                         created-at
                         doc
                         author
                         can-delete?
                         delete-state] :as sa}
                 owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [delete-ch]}]
      (let [delete-ch (or delete-ch (chan))]
        (sab/html
          [:div.col-sm-6.see-also
           [:div
            (util/$var-link (:ns to-var) (:name to-var)
              [:span.ns (:ns to-var) "/"]
              [:span.name (:name to-var)])]
           [:p
            (->> doc
                 (take 100)
                 (apply str))
            (when (> (count doc) 100)
              "...")]
           [:div.meta
            "Added by "
            [:a {:href (str "/u/" (:login author))} (:login author)]
            (when can-delete?
              [:span.delete-controls
               " / "
               (condp = delete-state
                 :loading [:img.loading {:src "/img/loading.gif"}]
                 :confirm [:span.delete-confirmation
                           [:a {:href "#"
                                :on-click #(do
                                             (om/update! sa :delete-state :none)
                                             false)}
                            "cancel"]
                           " | "
                           [:a {:href "#"
                                :on-click #(do
                                             (put! delete-ch @sa)
                                             false)}
                            "confirm delete"]]

                 [:span
                  {:class (when (= :error delete-state) "error-deleting bg-danger")}
                  [:a {:href "#"
                       :on-click #(do
                                    (om/update! sa :delete-state :confirm)
                                    false)}
                   "delete"]])])]])))))

(defn $add [{:keys [expanded? loading? completing? error ac-results ac-text] :as app} owner]
  (reify
    om/IDidUpdate
    (did-update [_ _ _]
      (.focus (om/get-node owner "input")))
    om/IRenderState
    (render-state [this {:keys [new-ch ac-ch]}]
      (let [new-ch (or new-ch (chan))
            ac-ch (or ac-ch (chan))]
        (sab/html
          [:div.add-see-also
           [:div.toggle-controls
            (if true
              [:a.toggle-link {:href "#"
                               :on-click #(do
                                            (om/update! app :expanded? (not expanded?))
                                            false)}
               (if-not expanded?
                 "Add See Also"
                 "Collapse")]
              [:span.muted "log in to add a see also"])]
           [:div.add-see-also-content {:class (when-not expanded? "hidden")}
            [:form {:on-submit #(do
                                  (put! new-ch (or ac-text ""))
                                  false)
                    :autoComplete "off"}
             [:div.input-group
              [:input.form-control
               {:class (when (or loading? completing?) "loading")
                :name "see-also-name"
                :ref "input"
                :placeholder "Var Name"
                :disabled (when loading? "disabled")
                :value ac-text
                :on-change #(let [text (.. % -target -value)]
                              (om/update! app :ac-text text)
                              (put! ac-ch (or text ""))
                              false)}]
              [:span.input-group-btn
               [:button.btn.btn-success
                {:disabled (when loading? "disabled")}
                "Add See-Also"]]]
             (when error
               [:div.error-message.text-danger
                [:i.fa.fa-exclamation-circle]
                error])]
            [:div.ac-results
             [:ul
              (for [{:keys [ns name]} ac-results]
                [:li ns "/" name])]]]])))))

(defn $see-alsos [{:keys [see-alsos var user add-see-also] :as app} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [new-ch ac-ch delete-ch]}]
      (sab/html
        [:div
         [:h5 "See Also"]
         (if (empty? see-alsos)
           [:div.null-state
            "No see-alsos for " [:code (:ns var) "/" (:name var)]]
           (->> see-alsos
                (map #(do
                        (om/build $see-also % {:init-state {:delete-ch delete-ch}})))
                (partition-all 2)
                (map (fn [cs] [:div.row cs]))))
         (if user
           (om/build $add add-see-also {:init-state {:new-ch new-ch
                                                     :ac-ch ac-ch
                                                     :delete-ch delete-ch}})
           [:div.login-required-message
            "Log in to add a see-also"])]))))
