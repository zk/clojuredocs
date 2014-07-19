(ns clojuredocs.api
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE ANY routes)]
            [clojuredocs.util :as util]
            [clout.core :as clout]))

(def field-types
  [{:name :email
    :type :email}
   {:name :_id
    :type :mongo-id}
   {:name :created-at
    :type :unix-timestamp}
   {:name :avatar-url
    :type :string}])

(def example-schema
  {:name :example
   :api-root "/examples"
   :contexts {:create {:required [:user]
                       :http-method :put}}
   :local-refs {:name :user :schema :user}})

(def user-schema
  {:name :user
   :api-root "/users"
   :contexts {:create {:required [:login :email :avatar-url]
                       :http-method :put}}
   :remote-refs [:example]})

(def schemas [example-schema user-schema])

(defn endpoint [{:keys [name api-root] :as schema}
                    context-name
                    handler]
  (assert api-root (str "No api-root for schema " name))
  (let [ctx (-> schema :contexts context-name)
        root-path api-root
        ctx-path (-> ctx :path)
        method (-> ctx :http-method)
        path (str root-path ctx-path)
        path (.replaceFirst path "/api" "")
        route (clout/route-compile path)]
    (assert method (str "No http-method for " name " -> " context-name))
    (fn [req]
      (when (= method (:request-method req))
        (when-let [match (clout/route-matches route req)]
          (handler (assoc req :route-params match)))))))

(defn make-handlers [handler-parts]
  (let [hs (partition 3 handler-parts)]
    (->> hs
         (map #(apply endpoint %))
         (apply routes))))

(defn create-example [{:keys []}])

(defroutes _routes
  (endpoint example-schema :create create-example)
  (ANY "/render-markdown" []
    (fn [r]
      (let [body (util/response-body r)]
        {:body (when body (util/markdown body))
         :headers {"Content-Type" "text/html;charset=utf-8"}}))))
