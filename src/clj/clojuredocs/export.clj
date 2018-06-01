(ns clojuredocs.export
  (:require [nsfw.util :as nu]
            [clojuredocs.search :as search]
            [clojuredocs.data :as data]
            [clojuredocs.env :as env]
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
    (nu/to-json
      {:created-at (nu/now)
       :description "ClojureDocs Data Export"
       :vars (->> search/clojure-lib
                  :vars
                  (map collect-var)
                  vec)})))


(defn -main []
  (mon/set-connection!
    (mon/make-connection
      (env/str :mongo-url)))
  (run-export "resources/public/clojuredocs-export.json"))
