(ns clojuredocs.api.examples
  (:require [slingshot.slingshot :refer [throw+]]
            [somnium.congomongo :as mon]
            [schema.core :as s]
            [clojuredocs.util :as util]
            [clojuredocs.api.common :as c]))
;; Utils

(defn edn-response [payload]
  {:body payload
   :status 200})

(defn insert! [payload]
  (mon/insert! :examples payload)
  payload)

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


(defn body-not-empty [{:keys [body]}]
  (when (empty? body)
    {:message "Whoops, looks like your example is blank."}))

(defn var-required [{:keys [var]}]
  (when (empty? var)
    {:message "Please provide the var that your example is about"}))


;; Handlers

(def InsertExample
  {:author c/User
   :body s/Str
   :created-at s/Int
   :updated-at s/Int
   :var c/Var
   :_id org.bson.types.ObjectId})

(defn post-example-handler [{:keys [edn-body user]}]
  (c/require-login! user)
  (c/validate! edn-body [body-not-empty var-required])
  (-> edn-body
      (assoc :author user)
      c/update-timestamps
      (assoc :_id (org.bson.types.ObjectId.))
      (c/validate-schema! InsertExample)
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
    (c/require-login! user)
    (let [_id (c/parse-mongo-id! id)
          example-update (assoc edn-body :_id _id)
          example (mon/fetch-one :examples :where {:_id (:_id example-update)})
          new-example (-> example
                          (assoc :body (:body example-update))
                          (update-in [:editors] (add-editor user))
                          c/update-timestamps)
          example-history (create-example-history
                            example
                            user
                            (:body example-update))]
      (when-not example
        (throw+
          (-> (edn-response {:error "Couldn't find the example you're trying to update."})
              (assoc :status 422))))
      (c/validate! new-example [body-not-empty])
      (c/validate-schema! new-example UpdateExample)
      (c/validate-schema! example-history InsertExampleHistory)
      (mon/update! :examples
        {:_id (:_id example)}
        new-example)
      (mon/insert! :example-histories example-history)
      {:status 200
       :headers {"Content-Type" "application/edn"}
       :body new-example})))

(defn delete-example-handler [id]
  (fn [{:keys [user]}]
    (c/require-login! user)
    (let [_id (c/parse-mongo-id! id)
          example (mon/fetch-one :examples :where {:_id _id})]
      (when-not example
        (throw+ {:status 404
                 :body {:message "Not Found"}}))
      (when-not (util/is-author? user example)
        (throw+ {:status 401
                 :body {:message "Not authorized to delete that example"}}))
      (mon/update! :examples example (assoc example :deleted-at (util/now)))
      {:status 200 :body example})))
