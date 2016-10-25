(ns clojuredocs.see-alsos
  (:require [dommy.core :as dommy :refer-macros [sel1]]
            [nsfw.ops :as ops]
            [reagent.core :as rea]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.anim :as anim]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [clojuredocs.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn $see-also [opts !sa bus]
  (let [{:keys [from-var to-var
                created-at
                doc
                author
                can-delete?
                delete-state
                _id] :as sa} @!sa]
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
                          :on-click (fn [e]
                                      (.preventDefault e)
                                      (swap! !sa assoc :delete-state :none)
                                      nil)}
                      "cancel"]
                     " | "
                     [:a {:href "#"
                          :on-click (fn [e]
                                      (.preventDefault e)
                                      (ops/send bus ::delete-see-also sa)
                                      nil)}
                      "confirm delete"]]

           [:span
            {:class (when (= :error delete-state) "error-deleting bg-danger")}
            [:a {:href "#"
                 :on-click (fn [e]
                             (.preventDefault e)
                             (swap! !sa assoc :delete-state :confirm)
                             nil)}
             "delete"]])])]]))

(defn $add [!app bus]
  (let [{:keys [expanded? loading? completing? error ac-results ac-text] :as app} @!app]
    [:div.add-see-also
     [:div.toggle-controls
      (if true
        [:a.toggle-link {:href "#"
                         :on-click (fn [e]
                                     (.preventDefault e)
                                     (swap! !app update-in [:expanded?] not)
                                     nil)}
         (if-not expanded?
           "Add See Also"
           "Collapse")]
        [:span.muted "log in to add a see also"])]
     [:div.add-see-also-content {:class (when-not expanded? "hidden")}
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (ops/send bus ::create (or ac-text ""))
                           nil)
              :autoComplete "off"}
       [:div.input-group
        [:input.form-control
         {:class (when (or loading? completing?) "loading")
          :name "see-also-name"
          :ref "input"
          :placeholder "Var Name"
          :disabled (when loading? "disabled")
          :value ac-text
          :on-change (fn [e]
                       (let [text (.. e -target -value)]
                         (ops/send bus ::ac-text (or text ""))
                         (swap! !app assoc :ac-text text)))}]
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
          [:li ns "/" name])]]]]))

(defn $see-alsos [!app bus]
  (let [{:keys [see-alsos var user add-see-also] :as app} @!app]
    [:div
     [:h5 "See Also"]
     (if (empty? see-alsos)
       [:div.null-state
        "No see-alsos for " [:code (:ns var) "/" (:name var)]]
       (->> see-alsos
            (map-indexed
              (fn [i sa]
                [$see-also {:key i} (rea/cursor !app [:see-alsos i]) bus]))
            (partition-all 2)
            (map-indexed (fn [i cs] [:div.row
                                     {:key i}
                                     cs]))))
     (if user
       [$add (rea/cursor !app [:add-see-also]) bus]
       [:div.login-required-message
        "Log in to add a see-also"])]))
