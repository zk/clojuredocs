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
            [clojuredocs.api.endpoints :as e]))

(defn mount-context [{:keys [name api-root] :as comp}
                     context-name
                     handler]
  (let [ctx (->> comp :contexts (filter #(= context-name (:name %))) first)
        root-path api-root
        ctx-path (-> ctx :path)
        method (-> ctx :http-method)
        path (str root-path ctx-path)
        path (.replaceFirst path "/api" "")
        route (clout/route-compile path)]
    (assert method (str "No http method for " name " -> " context-name))
    (fn [req]
      (when (= method (:request-method req))
        (when-let [match (clout/route-matches route req)]
          (try+
            (handler (assoc req :route-params match) comp (-> comp :contexts context-name))
            (catch map? resp ; Should have a record for this?
              resp)
            (catch Exception e
              (.printStackTrace e)
              {:status 500
               :headers {"Content-Type" "application/edn"}
               :body (pr-str (str e))})))))))

(defn mount-comp [comp & rest]
  (let [pairs (partition 2 rest)]
    (->> pairs
         (map (fn [[context-name handler]]
                (mount-context comp context-name handler))))))

(defroutes routes
  (apply cc/routes
    (mount-comp schemas/ExampleComp
      :create e/create-example-handler
      :delete e/delete-example-handler
      :update e/update-example-handler)))
