(ns clojuredocs.schemas
  (:require [schema.core :as s]))

;; Validations

(defn validate [ent comp context-name]
  (let [validations (-> comp :contexts context-name :validations)]
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

(defn logged-in-create [{:keys [author] :as ex}]
  (when-not author
    {:error-message "You must be logged in to create an example"}))

(defn logged-in-edit [{:keys [author]}]
  (when-not author
    {:error-message "You must be logged in to edit an example"}))

#_(def ExampleComp
  {:name :example
   :api-root "/examples"
   :mongo-coll :examples
   :field-docs {:body "Example body text"
                :var "Var for this example"}
   :contexts [{:name :create
               :req-schema {:body s/Str
                            :var Var}
               :resp-schemas {200 {:body s/Str}
                              422 {(s/optional-key :error-message) s/Str}}
               :http-method :post
               :validations [body-not-empty has-valid-var logged-in-create]
               :require-auth? true}
              {:name :update
               :validations [body-not-empty has-valid-var logged-in-edit]
               :http-method :put}
              {:name :delete
               :http-method :delete
               :validations [(fn [{:keys [_id]}]
                               (when-not _id
                                 {:error-message "Please provide the id of"}))]
               :identity #(select-keys % [:_id])
               :data-op :delete}]})


(def CreateExample {:body s/Str})

(def Example {:body s/Str})

(def User {:login s/Str
           :avatar-url s/Str
           :account-source (s/enum "github" "clojuredocs")})

(def Var {:ns s/Str
          :name s/Str
          :library-url s/Str})

(def ExampleReq
  {(s/optional-key :_id) #+clj org.bson.types.ObjectId #+cljs s/Str
   :body s/Str
   :var Var
   (s/optional-key :author) User
   (s/optional-key :editors) [User]
   (s/optional-key :created-at) s/Int
   (s/optional-key :updated-at) s/Int})

(def ExampleResp
  (assoc ExampleReq (s/optional-key :_id) s/Str))

(def ExampleList {:data [ExampleResp]
                  :offset s/Int
                  :limit s/Int
                  :count s/Int
                  :total s/Int})

(def Pagination {(s/optional-key :offset) s/Int
                 (s/optional-key :limit) s/Int})

(def ValidationError
  {:field s/Keyword
   :message s/Str})

(def ValidationErrors
  {(s/optional-key :message) s/Str
   (s/optional-key :errors) [ValidationError]})

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

(def get-examples-endpoint
  {:method :get
   :path "/api/examples"
   :schemas {:req {:edn-body ExampleReq
                   (s/optional-key :params) {(s/optional-key :limit) s/Int
                                                   (s/optional-key :offset) s/Int}}
             :resp {200 {:offset s/Int
                         :limit s/Int
                         :data [ExampleResp]}}}})
