(ns clojuredocs.external-api
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE context)]
            [cheshire.core :as cheshire]
            [cheshire.generate :as cheshire-gen]
            [clojuredocs.data :as data]
            [clojuredocs.config :as config]
            [clojuredocs.util :as util]))

(cheshire-gen/add-encoder org.bson.types.ObjectId cheshire-gen/encode-str)

(def library-url "https://github.com/clojure/clojure")

(def to-json cheshire/generate-string)
(def from-json cheshire/parse-string)

(defn json-response [resp]
  (-> resp
      (assoc-in [:headers "Content-Type"] "application/json;charset=utf-8")
      (update-in [:body] to-json)))

(defn parse-page-params [{:keys [limit offset]}]
  {:offset (try
             (Integer/parseInt offset)
             (catch NumberFormatException e
               0))
   :limit (try
            (Integer/parseInt limit)
            (catch NumberFormatException e
              20))})

(defn v1-examples-handler [ns name]
  (fn [r] (json-response {:body "todo"})))

(defn v2-examples-handler [ns name]
  (fn [{:keys [params]}]
    (prn params)
    (let [v {:ns ns
             :name name
             :library-url library-url}
          {:keys [offset limit]} (parse-page-params params)
          examples (data/find-examples-for v :skip offset :limit limit)]
      (json-response
        {:body
         {:html-url (config/url "/" ns "/" (util/cd-encode name))
          :offset offset
          :limit limit
          :count (count examples)
          :total (data/count-examples-for v)
          :examples (->> examples
                         (map #(dissoc % :_id))
                         (map #(update-in % [:editors] distinct))
                         (map #(update-in % [:editors]
                                 (fn [editors]
                                   (remove (fn [ed] (= ed (:author %))) editors)))))}}))))

(defroutes v1-routes
  (GET "/examples/:ns/:name" [ns name] (v1-examples-handler ns name))
  (GET "/examples/:version/:ns/:name" [ns name] (v1-examples-handler ns name)))

(defroutes v2-routes
  (GET "/examples/:ns/:name" [ns name] (v2-examples-handler ns name)))

(defroutes _routes
  (context "/v1" [] v1-routes)
  (context "/v2" [] v2-routes))

(def routes _routes)
