(ns clojuredocs.api.endpoints
  (:require [slingshot.slingshot :refer [try+ throw+]]
            [clojuredocs.data :as data]
            [clojuredocs.util :as util]
            [somnium.congomongo :as mon]
            [plumbing.core :as p]
            [schema.core :as s]
            [schema.coerce :as c]))

(defn validate! [payload {:keys [validations]}]
  (let [errors (reduce #(merge %1 (%2 payload)) {} validations)]
    (when-not (empty? errors)
      (throw+ {:status 422
               :headers {"Content-Type" "application/edn"}
               :body (pr-str errors)})))
  payload)

(defn insert! [payload comp]
  ;; :|
  (data/insert! comp payload)
  payload)

(defn edn-response [payload]
  {:body (pr-str payload)
   :headers {"Content-Type" "application/edn"}})

(defn update-timestamps [{:keys [created-at updated-at] :as m}]
  (let [now (util/now)
        m (if created-at m (assoc m :created-at now))]
    (assoc m :updated-at now)))

(defn parse-mongo-id [{:keys [_id] :as m}]
  (if _id
    (assoc m :_id (org.bson.types.ObjectId. _id))
    m))

(defn format-mongo-id [{:keys [_id] :as m}]
  (if _id
    (assoc m :_id (str _id))
    m))

(defn create-example-handler [{:keys [edn-body user]} comp context]
  (-> edn-body
      (assoc :author user)
      (validate! context)
      update-timestamps
      (assoc :_id (org.bson.types.ObjectId.))
      (insert! comp)
      format-mongo-id
      edn-response))

(defn require-author-of! [{:keys [author] :as payload} user]
  (when-not (= (select-keys author [:login :account-source])
               (select-keys user [:login :account-source]))
    (throw+ {:status 422
             :headers {"Content-Type" "application/edn"}
             :body (pr-str {:error-message "You must be the original author of an example to delete it."})}))
  payload)

(defn find-by-id [{:keys [_id]} {:keys [mongo-coll]}]
  (mon/fetch-one mongo-coll :where {:_id _id}))

(defn delete! [{:keys [_id] :as payload} {:keys [mongo-coll]}]
  (mon/destroy! mongo-coll {:_id _id})
  payload)

(defn delete-example-handler [{:keys [edn-body user]} comp context]
  (-> edn-body
      parse-mongo-id
      (find-by-id comp)
      (require-author-of! user)
      (delete! comp)
      format-mongo-id
      edn-response))

(defn example-by-id [{:keys [_id]}]
  (data/find-by-id ))

(defn update-example-handler [{:keys [edn-body user]} comp context]
  (let [{:keys [_id body] :as example-update} (parse-mongo-id edn-body)
        example (find-by-id example-update comp)]
    (when-not example
      (throw+
        (-> (edn-response {:error-message "Couldn't find the example you're trying to update."})
            (assoc :status 422))))
    ))



(defn create-example-handler [{:keys [edn-body user]} comp context]
  (-> edn-body
      (assoc :author user)
      (validate! context)
      update-timestamps
      (assoc :_id (org.bson.types.ObjectId.))
      (insert! comp)
      format-mongo-id
      edn-response))

(defn get-examples-handler [{:keys [edn-body params]}]
  {:status 200
   :body {:data (mon/fetch :examples :limit 2)
          :limit 10
          :offset 2}})
