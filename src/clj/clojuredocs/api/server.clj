(ns clojuredocs.api.server
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE ANY) :as cc]
            [clojuredocs.util :as util]
            [clojuredocs.api.schemas :as schemas]
            [clojuredocs.data :as data]
            [somnium.congomongo :as mon]
            [schema.core :as ps]
            [schema.coerce :as coerce]
            [clout.core :as clout]
            [slingshot.slingshot :refer [try+ throw+]]
            [clojuredocs.api.endpoints :as e]
            [schema.core :as s]
            [schema.coerce :as c]
            [schema.utils]))

(def RequestSchema
  {s/Any s/Any})

(def ResponseSchema
  {s/Any s/Any})

(defn mongo-id-coercion [schema]
  (s/start-walker
   (fn [s]
     (let [walk (s/walker s)]
       (fn [x]
         (walk
           (cond
             (and (= s org.bson.types.ObjectId) (string? x)) (org.bson.types.ObjectId. x)
             (and (= s s/Str) (= org.bson.types.ObjectId (class x))) (str x)
             :else x)))))
   schema))

(defn validate-request [req schema]
  (prn (type req))
  (let [coercion ((mongo-id-coercion (merge RequestSchema schema)) req)]
    (when (schema.utils/error? coercion)
      (throw+
        {:status 422
         :headers {"Content-Type" "application/edn"}
         :body (pr-str
                 {:errors (:error coercion)
                  :schema schema})}))
    coercion))

(defn validate-response-body [{:keys [status body] :as resp} schemas]
  (let [status (or status 200)
        schema (get schemas status)
        coercion ((mongo-id-coercion (merge ResponseSchema schema)) body)]
    (when (schema.utils/error? coercion)
      (throw+
        {:status 500
         :headers {"Content-Type" "application/edn"}
         :body (pr-str {:message "Error validating response"
                        :errors (pr-str coercion)})}))
    (assoc resp :body coercion)))

(defn mount-endpoint [[{:keys [name path method schemas]} handler]]
  (let [path path #_(.replaceFirst path "/api" "")
        route (clout/route-compile path)]
    (fn [req]
      (when (= method (:request-method req))
        (when-let [match (clout/route-matches route req)]
          (try+
            (-> req
                (assoc :route-params match)
                (validate-request (:req schemas))
                handler
                (validate-response-body (:resp schemas))
                (update-in [:body] pr-str))
            (catch map? resp          ; Should have a record for this?
              resp)
            (catch Exception e
              (.printStackTrace e)
              {:status 500
               :headers {"Content-Type" "application/edn"}
               :body (pr-str (str e))})))))))

(defroutes _routes
  (->> [e/get-examples-endpoint
        e/update-example-endpoint
        e/create-example-endpoint
        e/delete-example-endpoint]
       (map mount-endpoint)
       (apply cc/routes)))

(defn string-body? [r]
  (string? (:body r)))

(defn edn-response? [{:keys [headers]}]
  (get #{"application/edn"}
    (or (get headers "Content-Type")
        (get headers "content-type"))))

(defn wrap-format-edn-body [h]
  (fn [r]
    (let [res (h r)]
      (if (and (not (string-body? res))
               (edn-response? res))
        (update-in res [:body] pr-str)
        res))))

(def routes
  (->> _routes
       wrap-format-edn-body))

#_(routes {:uri "/api/examples/54027f6130049c43a3100cf0"
         :request-method :delete
         :user {:login "zkim" :account-source "clojuredocs"}})
