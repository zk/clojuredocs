(ns clojuredocs.schemas
  (:require [schema.core :as s]))

;; prismatic schemas

(def Comp {:name s/Keyword
           :mongo-coll s/Keyword
           :api-root s/Str
           (s/optional-key :contexts) s/Any
           (s/optional-key :local-refs) s/Any})

(def Var
  ^{:docs {:ns "Namespace -- ex. `clojure.core`"}
    :name "Var"}
  {:ns s/Str :name s/Str :library-url s/Str})

(def VarDocs )

(def User
  ^{:name "User"}
  {:login s/Str
   :avatar-url s/Str
   :account-source (s/enum "github" "bitbucket" "clojuredocs")})

(def GetExample
  {:_id s/Str
   :updated-at s/Int
   :created-at s/Int
   :body s/Str
   :var Var
   :author User})

(def ExampleHistory
  {:created-at s/Int
   :body s/Str
   :author User})

(def CreateExample
  {(s/optional-key :_id) #+clj org.bson.types.ObjectId #+cljs s/Str
   :var Var
   :author User
   :body s/Str
   :updated-at s/Int
   :created-at s/Int
   :editors [User]})

(def UpdateExample
  (merge
    CreateExample))

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

(def ExampleComp
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

(def CreateExampleComp
  {:name :create-example
   :path "/examples"
   :mongo-coll :examples
   :http-method :post
   :request-validations [body-not-empty has-valid-var]})

(def UpdateNote {:author User
                 :var Var
                 :created-at s/Int
                 :updated-at s/Int
                 :body s/Str})

(def NoteComp
  {:name :note
   :api-root "/notes"
   :mongo-coll :notes
   :contexts {:update {:schema UpdateNote}}})

(def SeeAlsoVar
  {:author User
   :created-at s/Int
   :ns s/Str
   :name s/Str
   :library-url s/Str})

(def UpdateSeeAlso
  {(s/optional-key :_id) s/Str
   :var Var
   :refs [SeeAlsoVar]})

(def SeeAlsoComp
  {:name :see-also
   :api-root "/see-alsos"
   :mongo-coll :see-alsos
   :contexts {:update {:schema UpdateSeeAlso}}})

;;;

#_(doseq [ps [ExampleComp NoteComp SeeAlsoComp]]
  (s/validate Comp ps))
