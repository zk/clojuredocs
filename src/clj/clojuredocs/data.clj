(ns clojuredocs.data
  (:require [somnium.congomongo :as mon]))

;; Examples

(defn find-examples-for [{:keys [ns name library-url]} & opts]
  (let [opts (apply hash-map opts)
        query (merge
                {:where {:var.name name
                         :var.ns ns
                         :var.library-url library-url
                         :deleted-at nil}
                 :sort {:created-at 1}}
                opts)]
    (prn query)
    (apply mon/fetch (apply concat [:examples] query))))

(defn count-examples-for [{:keys [ns name library-url]}]
  (mon/fetch-count :examples
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url
            :deleted-at nil}))

;; Notes

(defn find-notes-for [{:keys [ns name library-url]}]
  (mon/fetch :notes
    :where {:var.ns ns
            :var.name name
            :var.library-url library-url}
    :sort {:created-at 1}))


;; See Alsos

(defn find-see-alsos-for [{:keys [ns name library-url]}]
  (mon/fetch-one :see-alsos
    :where {:var.name name
            :var.ns ns
            :var.library-url "https://github.com/clojure/clojure"}))
