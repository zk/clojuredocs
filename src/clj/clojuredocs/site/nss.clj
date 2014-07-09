(ns clojuredocs.site.nss
  (:require [somnium.congomongo :as mon]
            [clojuredocs.site.common :as common]
            [clojure.string :as str]))

(defn library-for [ns]
  (mon/fetch-one :libraries :where {:namespaces ns}))

(defn namespace-for [ns]
  (mon/fetch-one :namespaces :where {:name ns}))

(defn vars-for [ns]
  (mon/fetch :vars :where {:ns ns} :sort {:name 1}))

(defn index [ns-str]
  (fn [r]
    (let [lib (library-for ns-str)
          ns (namespace-for ns-str)
          vars (sort-by #(-> % :name str/lower-case) (vars-for ns-str))]
      (common/$main
        {:body-class "ns-page"
         :content [:div
                   [:div.row
                    [:div.col-sm-2
                     (common/$recent (-> r :session :recent))
                     (common/$library-nav lib)]
                    [:div.col-sm-10
                     [:h1 ns-str]
                     (when (:doc ns)
                       [:pre.doc (:doc ns)])
                     [:table {:class "ns-table"}
                      (for [{:keys [name doc]} vars]
                        [:tr
                         [:td.name [:span [:a {:href (str "/" ns-str "/" name)} name]]]
                         [:td [:div.doc doc]]])]]]]}))))
