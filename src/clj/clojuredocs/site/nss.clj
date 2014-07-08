(ns clojuredocs.site.nss
  (:require [somnium.congomongo :as mon]
            [clojuredocs.site.common :as common]))

(defn library-for [ns]
  (mon/fetch-one :libraries :where {:namespaces ns}))

(defn index [ns]
  (fn [r]
    (let [lib (library-for ns)]
      (common/$main
        {:content [:div
                   [:div.row
                    [:div.col-sm-2
                     (common/$library-nav lib)]
                    [:div.col-sm-10
                     [:h1 ns]]]]}))))
