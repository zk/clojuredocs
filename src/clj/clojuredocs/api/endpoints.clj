(ns clojuredocs.api.endpoints
  (:require [slingshot.slingshot :refer [try+ throw+]]
            [clojuredocs.data :as data]
            [clojuredocs.util :as util]
            [somnium.congomongo :as mon]
            [plumbing.core :as p]
            [schema.core :as s]
            [schema.coerce :as c]
            [clojuredocs.api.schemas :as schemas]))

(defn validate! [payload {:keys [validations]}]
  (let [errors (reduce #(merge %1 (%2 payload)) {} validations)]
    (when-not (empty? errors)
      (throw+ {:status 422
               :headers {"Content-Type" "application/edn"}
               :body (pr-str errors)})))
  payload)

(defn insert! [payload]
  (mon/insert! :examples payload)
  payload)

(defn edn-response [payload]
  {:body payload
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

(defn require-login! [user]
  (when-not user
    (throw+
      (-> (edn-response {:error "You must be logged in to edit an example."})
          (assoc :status 401)))))

(def update-example-endpoint
  [schemas/UpdateExample
   (fn [{:keys [route-params edn-body user]}]
     (require-login! user)
     (let [example-update (merge route-params edn-body)
           example (mon/fetch-one :examples :where {:_id (:_id example-update)})
           new-example (-> example
                           (assoc :body (:body example-update))
                           (update-in [:editors] (add-editor user)))
           example-history (create-example-history
                             example
                             user
                             (:body example-update))]
       (when-not example
         (throw+
           (-> (edn-response {:error "Couldn't find the example you're trying to update."})
               (assoc :status 422))))
       (mon/update! :examples
         {:_id (:_id example)}
         new-example)
       (mon/insert! :example-histories example-history)
       {:status 200
        :headers {"Content-Type" "application/edn"}
        :body new-example}))])

(def create-example-endpoint
  [schemas/CreateExample
   (fn [{:keys [edn-body user]}]
     (require-login! user)
     (-> edn-body
         (assoc :author user)
         update-timestamps
         (assoc :_id (org.bson.types.ObjectId.))
         insert!
         edn-response))])

(def get-examples-endpoint
  [schemas/GetExamples
   (fn [{:keys [edn-body params]}]
     {:status 200
      :body {:data (mon/fetch :examples :limit 2)
             :limit 10
             :offset 2}})])

(defn is-author? [user example]
  (= (select-keys user [:login :account-source])
     (select-keys (:author example) [:login :account-source])))

(def delete-example-endpoint
  [schemas/DeleteExample
   (fn [{:keys [route-params user]}]
     (require-login! user)
     (let [_id (:_id route-params)
           example (mon/fetch-one :examples :where {:_id _id})]
       (when-not example
         (throw+ {:status 404
                  :body {:message "Not Found"}
                  :headers {"Content-Type" "application/edn"}}))
       (when-not (is-author? user example)
         (throw+ {:status 401
                  :body {:message "Not authorized to delete that example"}
                  :headers {"Content-Type" "application/edn"}}))
       (mon/update! :examples example (assoc example :deleted-at (util/now)))
       {:status 200 :body example}))])
