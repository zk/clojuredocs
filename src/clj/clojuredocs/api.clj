(ns clojuredocs.api
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE ANY) :as cc]
            [clojuredocs.util :as util]
            [clojuredocs.schemas :as schemas]
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
                        :errors coercion})}))
    (assoc resp :body coercion)))

(defn mount-endpoint [[{:keys [name path method schemas]} handler]]
  (let [path path #_(.replaceFirst path "/api" "")
        route (clout/route-compile path)]
    (fn [req]
      (when (= method (:request-method req))
        (when-let [match (clout/route-matches route req)]
          (try+
            (-> req
                (validate-request (:req schemas))
                (assoc :route-params match)
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

(def endpoints
  [schemas/get-examples-endpoint e/get-examples-handler])

(defroutes routes
  (apply cc/routes
    (->> endpoints
         (partition 2)
         (map mount-endpoint))))

#_(routes
    {:uri "/api/examples"
     :request-method :get
     :edn-body {:var {:ns "clojure.core"
                      :name "map"
                      :library-url "foo"}
                :body "hello world"}})
