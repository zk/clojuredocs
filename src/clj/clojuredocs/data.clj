(ns clojuredocs.data
  (:require [somnium.congomongo :as mon]))

;; Examples

(defn examples [{:keys [limit offset]}]
  (mon/fetch :examples :limit limit :skip offset
    :sort {:created-at 1}))

(defn find-examples-for
  ([find-params]
   (find-examples-for find-params {}))
  ([{:keys [ns name library-url]}
    {:keys [offset limit]}]
   (let [query (merge
                 {:where {:var.name name
                          :var.ns ns
                          :var.library-url library-url
                          :deleted-at nil}
                  :sort {:created-at 1}}
                 {:limit limit :skip offset})]
     (apply mon/fetch (apply concat [:examples] query)))))

(defn count-examples-for [{:keys [ns name library-url]}]
  (mon/fetch-count :examples
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url
            :deleted-at nil}))

;; Notes

(defn find-notes-for
  ([find-params]
   (find-notes-for find-params {}))
  ([{:keys [ns name library-url]}
    {:keys [offset limit]}]
   (mon/fetch :notes
     :where {:var.ns ns
             :var.name name
             :var.library-url library-url}
     :sort {:created-at 1}
     :skip offset
     :limit limit)))

(defn count-notes-for [{:keys [ns name library-url]}]
  (mon/fetch-count :notes
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url
            :deleted-at nil}))


;; See Alsos

(defn find-see-alsos-for
  ([find-params]
   (find-see-alsos-for find-params {}))
  ([{:keys [ns name library-url]}
    {:keys [offset limit]}]
   (mon/fetch :see-alsos
     :where {:from-var.name name
             :from-var.ns ns
             :from-var.library-url "https://github.com/clojure/clojure"})))

#_(mon/fetch-one :see-alsos
    :where {:from-var.name "map"
            :from-var.ns "clojure.core"
            :from-var.library-url "https://github.com/clojure/clojure"})

(defn count-see-alsos-for [{:keys [ns name library-url]}]
  (mon/fetch-count :see-alsos
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url
            :deleted-at nil}))
