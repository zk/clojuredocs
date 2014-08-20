(ns clojuredocs.data
  (:require [somnium.congomongo :as mon]
            [schema.core :as sc]))

(defn validate-payload [comp context-name payload]
  (sc/validate (-> comp :contexts context-name :schema) payload))

(defn insert! [comp payload]
  (let [context-name :create
        coll (:mongo-coll comp)]
    (validate-payload comp context-name payload)
    (mon/insert! coll payload)
    payload))

(defn update-where! [comp where-fn payload]
  (let [context-name :update
        coll (:mongo-coll comp)]
    (validate-payload comp context-name payload)
    (mon/update! coll (where-fn payload) payload)
    payload))

(defn update-by-id! [schema payload]
  (update-where! schema #(select-keys % [:_id]) payload))

(defn find-by-id [{:keys [mongo-coll]} _id]
  (mon/fetch-one mongo-coll :where {:_id _id}))


;; Examples

(defn find-examples-for [{:keys [ns name library-url]}]
  (mon/fetch :examples
    :where {:var.name name
            :var.ns ns
            :var.library-url library-url}
    :sort {:created-at 1}))

;; Notes

(defn find-notes-for [{:keys [ns name library-url]}]
  (mon/fetch :notes
    :where {:var.ns ns :var.name name :var.library-url library-url}
    :sort {:created-at 1}))


;; See Alsos

(defn find-see-alsos-for [{:keys [ns name library-url]}]
  (->> (mon/fetch-one :see-alsos :where {:var.name name
                                         :var.ns ns
                                         :var.library-url "https://github.com/clojure/clojure"})
       :refs))
