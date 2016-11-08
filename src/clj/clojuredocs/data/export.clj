(ns clojuredocs.data.export
  "Read and export edn representing a clojure library"
  (:require [codox.reader.clojure :as cljr]
            [codox.reader.clojurescript :as cljsr]
            [nsfw.util :as nu]
            [clojuredocs.search :as search]))

;; Inputs


;; Lib info

:source-base-url
:library-url

(def nss (reader/read-namespaces "/Users/zk/code/clojure"))

(let [source-base-url "https://github.com/clojure/clojure/blob/clojure-1.8.0/src/clj/"
      library-url "https://github.com/clojure/clojure"]
  (->> nss
       (mapcat
         (fn [{:keys [doc author name publics]}]
           (->> publics
                (map #(assoc % :ns name))
                (map #(search/format-for-cd library-url %))
                (map #(assoc % :source-url (str source-base-url
                                                (:file %)
                                                "#L"
                                                (:line %)))))))
       last
       nu/pp))





(keys (first vars))

(:name (first vars))

;; Top Level Info

;; source-base
