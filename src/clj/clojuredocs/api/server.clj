(ns clojuredocs.api.server
  (:require [clojuredocs.api.examples :as examples]
            [clojuredocs.api.notes :as notes]
            [clojuredocs.api.see-alsos :as see-alsos]
            [compojure.core :refer [DELETE GET PATCH POST defroutes]]
            [compojure.route :refer [not-found]]
            [fogus.unk :as unk]
            [nsfw.util :as nu]
            [slingshot.slingshot :refer [throw+ try+]]
            [somnium.congomongo :as mon])
  (:import [org.bson.types ObjectId]))

(defn all-see-alsos-relations-map []
  (->> (mon/fetch
         :see-alsos)
       (group-by #(select-keys (:from-var %) [:ns :name]))
       (map (fn [[{:keys [ns name]} to-vars]]
              [(str ns "/" name)
               (->> to-vars
                    (map (fn [{:keys [to-var]}]
                           (str (:ns to-var) "/" (:name to-var))))
                    vec)]))
       (into {})
       nu/pp-str))

(def memo-all-see-alsos-relations-map
  (unk/memo-ttl all-see-alsos-relations-map
    (* 1000 60 60 1)))

(defn see-alsos-relations-handler [r]
  {:body (memo-all-see-alsos-relations-map)
   :headers {"Content-Type" "application/edn"}})

(defroutes _routes
  (POST "/examples" [] examples/post-example-handler)
  (DELETE "/examples/:id" [id] (examples/delete-example-handler id))
  (PATCH "/examples/:id" [id] (examples/patch-example-handler id))

  (POST "/see-alsos" [] see-alsos/post-see-also-handler)
  (DELETE "/see-alsos/:id" [id] (see-alsos/delete-see-also-handler id))

  (POST "/notes" [] notes/post-note-handler)
  (PATCH "/notes/:id" [id] (notes/patch-note-handler id))
  (DELETE "/notes/:id" [id] (notes/delete-note-handler id))


  (GET "/exports/see-alsos-relations" [] see-alsos-relations-handler)

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
      (if (and _id (instance? ObjectId _id))
        (update-in res [:body :_id] str)
        res))))

(defn wrap-str->mongo-id [h]
  (fn [r]
    (let [{:keys [_id] :as res} (h r)]
      (if (and _id (string? _id))
        (try
          (assoc res :_id (ObjectId. (str _id)))
          (catch IllegalArgumentException e
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
