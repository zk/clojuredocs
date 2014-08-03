(ns clojuredocs.schemas
  (:require [schema.core :as s]))

;; Field Types

(def field-types
  [{:name :email
    :type :email}
   {:name :_id
    :type :mongo-id}
   {:name :created-at
    :type :unix-timestamp}
   {:name :avatar-url
    :type :string}])

;; Validations

(defn validate [ent schema context-name]
  (let [validations (-> schema :contexts context-name :validations)]
    (->> validations
         (reduce #(merge %1 (%2 ent)) {}))))

(defn body-not-empty [{:keys [body]}]
  (when (empty? body)
    {:error-message "Looks like your example is blank."}))

(defn field-not-empty [k msg]
  (fn [m]
    (when (empty? (get m k))
      {:error-message msg})))

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


;;;

(def Var {:ns s/Str :name s/Str})

(def User {:login s/Str
           :avatar-url s/Str
           :account-source (s/enum :github :bitbucket)})

(def GetExample
  {:_id s/Str
   :updated-at s/Int
   :created-at s/Int
   :body s/Str
   :var Var
   :history []
   :author User})

(def CreateExample
  {:var Var
   :author User
   :body s/Str})
