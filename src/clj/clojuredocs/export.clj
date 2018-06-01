(ns clojuredocs.export
  (:require [nsfw.util :as nu]
            [clojuredocs.search :as search]
            [clojuredocs.data :as data]
            [somnium.congomongo :as mon]))

(defn ensure-empty->nil [c]
  (if (and c (empty? c))
    nil
    c))

(defn collect-var [var]
  (merge
    var
    {:examples (->> var
                    data/find-examples-for
                    (map #(dissoc % :var))
                    (map #(update % :_id str))
                    vec
                    ensure-empty->nil)
     :see-alsos (->> var
                     data/find-see-alsos-for
                     (map #(dissoc % :from-var))
                     (map #(update % :_id str))
                     vec
                     ensure-empty->nil)
     :notes (->> var
                 data/find-notes-for
                 (map #(dissoc % :var))
                 (map #(update % :_id str))
                 vec
                 ensure-empty->nil)}))

(defn run-export [output-path]
  (spit
    output-path
    {:created-at (nu/now)
     :description "ClojureDocs Data Export"
     :vars (->> search/clojure-lib
                :vars
                (map collect-var)
                vec)}))
