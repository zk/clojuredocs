(ns clojuredocs.api.server
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE ANY PATCH) :as cc]
            [compojure.route :refer (not-found)]
            [somnium.congomongo :as mon]
            [clout.core :as clout]
            [slingshot.slingshot :refer [try+ throw+]]
            [clojuredocs.util :as util]
            [clojuredocs.api.examples :as examples]
            [clojuredocs.api.see-alsos :as see-alsos]
            [clojuredocs.api.notes :as notes]))

(defroutes _routes
  (POST "/examples" [] examples/post-example-handler)
  (DELETE "/examples/:id" [id] (examples/delete-example-handler id))
  (PATCH "/examples/:id" [id] (examples/patch-example-handler id))

  (POST "/see-alsos" [] see-alsos/post-see-also-handler)
  (DELETE "/see-alsos/:id" [id] (see-alsos/delete-see-also-handler id))

  (POST "/notes" [] notes/post-note-handler)
  (PATCH "/notes/:id" [id] (notes/patch-note-handler id))
  (DELETE "/notes/:id" [id] (notes/delete-note-handler id))

  (not-found
    {:status 404
     :body {:message "Route not found"}}))

(defn string-body? [r]
  (string? (:body r)))

(defn edn-response? [{:keys [headers]}]
  (re-find #"application/edn"
    (or (get headers "Content-Type")
        (get headers "content-type"))))

(defn wrap-format-edn-body [h]
  (fn [r]
    (let [res (h r)]
      (if (and (not (string-body? res))
               (edn-response? res))
        (update-in res [:body] pr-str)
        res))))

(defn wrap-mongo-id->str [h]
  (fn [r]
    (let [{:keys [body] :as res} (h r)
          _id (:_id body)]
      (if (and _id (instance? org.bson.types.ObjectId _id))
        (update-in res [:body :_id] str)
        res))))

(defn wrap-str->mongo-id [h]
  (fn [r]
    (let [{:keys [_id] :as res} (h r)]
      (if (and _id (string? _id))
        (try
          (assoc res :_id (org.bson.types.ObjectId. _id))
          (catch java.lang.IllegalArgumentException e
            (throw+
              {:status 400
               :body {:message (str "Error parsing Mongo ID: " _id)}})))
        res))))

(defn wrap-render-errors [h]
  (fn [r]
    (try+
      (h r)
      (catch map? m m)
      (catch Exception e
        (.printStackTrace e)
        {:body {:message "Unknown server error"}
         :status 500}))))

(defn wrap-force-edn [h]
  (fn [r]
    (-> (h r)
        (assoc-in [:headers "Content-Type"] "application/edn;charset=utf-8"))))

(def routes
  (-> _routes
      wrap-mongo-id->str
      wrap-str->mongo-id
      wrap-render-errors
      wrap-force-edn
      wrap-format-edn-body))
