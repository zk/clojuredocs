(ns clojuredocs.external-api
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE context)]
            [cheshire.core :as cheshire]
            [cheshire.generate :as cheshire-gen]
            [clojuredocs.data :as data]
            [clojuredocs.config :as config]
            [clojuredocs.util :as util]
            [clojure.pprint :refer [pprint]]))

(cheshire-gen/add-encoder org.bson.types.ObjectId cheshire-gen/encode-str)

(def library-url "https://github.com/clojure/clojure")

(def to-json cheshire/generate-string)
(def from-json cheshire/parse-string)

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
  (fn [r] {:body "todo"}))

(defn handle-endpoint [things]
  (fn [{:keys [params]}]
    (when-let [{:keys [response-key
                       query-fn
                       transform-fn
                       total-fn]} (get things (:thing params))]
      (let [{:keys [ns name]} params
            v {:ns ns
               :name name
               :library-url library-url}
            {:keys [offset limit]
             :as query-opts} (parse-page-params params)
             results-only (query-fn v query-opts)
             tx-results ((or transform-fn identity) results-only)
             total (total-fn v)]
        {:body
         {:html-url (config/url
                      (->> ["" ns name]
                           (remove nil?)
                           (interpose "/")
                           (apply str)))
          :offset offset
          :limit limit
          :count (count tx-results)
          :total total
          :data tx-results}}))))

(def things
  {"examples" {:query-fn data/find-examples-for
               :all-fn data/examples
               :transform-fn (fn [examples]
                               (->> examples
                                    (map #(dissoc % :_id))
                                    (map #(update-in % [:editors] distinct))
                                    (map #(update-in % [:editors]
                                            (fn [editors]
                                              (remove (fn [ed] (= ed (:author %))) editors))))))
               :total-fn data/count-examples-for}
   "notes" {:query-fn data/find-notes-for
            :total-fn data/count-notes-for}
   "see-alsos" {:query-fn data/find-see-alsos-for
                :total-fn data/count-see-alsos-for}})


(defroutes _routes
  (GET "/:ns/:name/:thing" req (handle-endpoint things)))

(defn json-response [resp]
  (-> resp
      (assoc-in [:headers "Content-Type"] "application/json;charset=utf-8")
      (update-in [:body] to-json)))

(defn wrap-render-json [h]
  (fn [r]
    (json-response (h r))))

(def routes
  (-> _routes
      wrap-render-json))
