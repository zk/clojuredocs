(ns clojuredocs.data
  (:require [somnium.congomongo :as mon]))

;; Examples

(defn find-examples-for [{:keys [ns name library-url]}]
  (mon/fetch :examples
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url
            :deleted-at nil}
    :sort {:created-at 1}))

;; Notes

(defn find-notes-for [{:keys [ns name library-url]}]
  (mon/fetch :notes
    :where {:var.ns ns
            :var.name name
            :var.library-url library-url}
    :sort {:created-at 1}))


;; See Alsos

(defn find-see-alsos-for [{:keys [ns name library-url]}]
  (->> (mon/fetch-one :see-alsos :where {:var.name name
                                         :var.ns ns
                                         :var.library-url "https://github.com/clojure/clojure"})
       :refs))
