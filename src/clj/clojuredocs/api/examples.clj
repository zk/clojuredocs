(ns clojuredocs.api.examples
  (:require [slingshot.slingshot :refer [throw+]]
            [somnium.congomongo :as mon]
            [schema.core :as s]
            [clojuredocs.util :as util]))

;; Schemas

(def User
  {:login s/Str
   :account-source s/Str
   :avatar-url s/Str})

(def Var
  {:ns s/Str
   :name s/Str
   :library-url s/Str})

(defn validate-schema! [payload schema]
  (let [res (s/check schema payload)]
    (when res
      (throw+
        {:status 421
         :body {:failures res
                :schema schema}})))
  payload)

;; Utils

(defn update-timestamps [{:keys [created-at updated-at] :as m}]
  (let [now (util/now)
        m (if created-at m (assoc m :created-at now))]
    (assoc m :updated-at now)))

(defn edn-response [payload]
  {:body payload
   :status 200})

(defn insert! [payload]
  (mon/insert! :examples payload)
  payload)

(defn is-author? [user example]
  (= (select-keys user [:login :account-source])
     (select-keys (:author example) [:login :account-source])))

(defn parse-mongo-id! [id]
  (try
    (org.bson.types.ObjectId. id)
    (catch java.lang.IllegalArgumentException e
      (throw+
        {:status 400
         :body {:message "Couldn't parse mongo id"}}))))

(defn create-example-history [example user new-body]
  {:_id (org.bson.types.ObjectId.)
   :editor user
   :body new-body
   :created-at (util/now)
   :example-id (:_id example)})

(defn add-editor [user]
  (fn [editors]
    (let [eds (map #(select-keys % [:login :account-source]) editors)]
      (if (get (set eds) (select-keys user [:login :account-source]))
        editors
        (concat editors [user])))))


;; Error States

(defn require-login! [user]
  (when-not user
    (throw+
      {:body {:error "You must be logged in to edit an example."}
       :status 401})))


;; Validations

(defn run-validations [payload validations]
  (->> validations
       (map #(% payload))
       (reduce merge)))

(defn validate! [payload validations]
  (let [res (run-validations payload validations)]
    (when-not (empty? res)
      (throw+
        {:status 422
         :body res}))))

(defn body-not-empty [{:keys [body]}]
  (when (empty? body)
    {:message "Whoops, looks like your example is blank."}))

(defn var-required [{:keys [var]}]
  (when (empty? var)
    {:message "Please provide the var that your example is about"}))


;; Handlers

(def InsertExample
  {:author User
   :body s/Str
   :created-at s/Int
   :updated-at s/Int
   :var Var
   :_id org.bson.types.ObjectId})

(defn post-example-handler [{:keys [edn-body user]}]
  (require-login! user)
  (validate! edn-body [body-not-empty var-required])
  (-> edn-body
      (assoc :author user)
      update-timestamps
      (assoc :_id (org.bson.types.ObjectId.))
      (validate-schema! InsertExample)
      insert!
      edn-response))

(def UpdateExample
  (merge
    InsertExample
    {:editors [User]}))

(def InsertExampleHistory
  {:_id org.bson.types.ObjectId
   :example-id org.bson.types.ObjectId
   :created-at s/Int
   :body s/Str
   :editor User})

(defn patch-example-handler [id]
  (fn [{:keys [edn-body user]}]
    (require-login! user)
    (let [_id (parse-mongo-id! id)
          example-update (assoc edn-body :_id _id)
          example (mon/fetch-one :examples :where {:_id (:_id example-update)})
          new-example (-> example
                          (assoc :body (:body example-update))
                          (update-in [:editors] (add-editor user))
                          update-timestamps)
          example-history (create-example-history
                            example
                            user
                            (:body example-update))]
      (when-not example
        (throw+
          (-> (edn-response {:error "Couldn't find the example you're trying to update."})
              (assoc :status 422))))
      (validate! new-example [body-not-empty])
      (validate-schema! new-example UpdateExample)
      (validate-schema! example-history InsertExampleHistory)
      (mon/update! :examples
        {:_id (:_id example)}
        new-example)
      (mon/insert! :example-histories example-history)
      {:status 200
       :headers {"Content-Type" "application/edn"}
       :body new-example})))

(defn delete-example-handler [id]
  (fn [{:keys [user]}]
    (require-login! user)
    (let [_id (parse-mongo-id! id)
          example (mon/fetch-one :examples :where {:_id _id})]
      (when-not example
        (throw+ {:status 404
                 :body {:message "Not Found"}}))
      (when-not (is-author? user example)
        (throw+ {:status 401
                 :body {:message "Not authorized to delete that example"}}))
      (mon/update! :examples example (assoc example :deleted-at (util/now)))
      {:status 200 :body example})))
