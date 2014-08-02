(ns clojuredocs.api
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE ANY routes)]
            [clojuredocs.util :as util]
            [somnium.congomongo :as mon]
            [clout.core :as clout]))

(defn validate [ent schema context-name]
  (let [validations (-> schema :contexts context-name :validations)]
    (->> validations
         (reduce #(merge %1 (%2 ent)) {}))))

(def field-types
  [{:name :email
    :type :email}
   {:name :_id
    :type :mongo-id}
   {:name :created-at
    :type :unix-timestamp}
   {:name :avatar-url
    :type :string}])

(defn body-not-empty [{:keys [body]}]
  (when (empty? body)
    {:error-message "Looks like your example is blank."}))

(defn has-valid-var [ex]
  (let [{:keys [ns name library-url] :as v} (:var ex)]
    (when-not (and ns name library-url)
      {:error-message "Please provide the library url, namespace, and name of the var."})))

(def example-schema
  {:name :example
   :api-root "/examples"
   :contexts {:create {:required [:user]
                       :http-method :put
                       :validations [body-not-empty has-valid-var]}}
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

(defroutes _routes
  (POST "/examples" []
    (fn [{:keys [edn-body user]}]
      (if-not user
        {:body (pr-str {:error-message "Sorry, you must be logged in to post an example."})
         :headers {"Content-Type" "application/edn"}
         :status 422}
        (let [entity (-> edn-body
                         (assoc :user (select-keys user [:login :avatar-url])))
              errors (validate entity example-schema :create)]
          (if-not (empty? errors)
            {:body (pr-str errors)
             :headers {"Content-Type" "application/edn"}
             :status 422}
            (do
              (mon/insert! :examples entity)
              {:body "ok"
               :status 200
               :headers {"Content-Type" "text/plain"}})))))))

#_(use 'clojure.pprint)

#_(->> (mon/fetch :examples)
     (take 10)
     pprint)
