(ns clojuredocs.api.notes
  (:require [clojuredocs.api.common :as c]
            [clojuredocs.data :as data]
            [schema.core :as s]
            [slingshot.slingshot :refer [throw+]]))

(defn body-not-empty [m]
  (when (empty? (:body m))
    {:message "Whoops, looks like your note is empty."}))

(def Note
  {:body s/Str
   :var c/Var
   :author c/User
   :created-at s/Int
   :updated-at s/Int
   :_id org.bson.types.ObjectId})

(defn post-note-handler [{:keys [edn-body user]}]
  (c/require-login! user)
  (let [new-note (-> edn-body
                     (assoc :_id (org.bson.types.ObjectId.))
                     c/update-timestamps
                     (assoc :author user))]
    (c/validate! new-note [body-not-empty])
    (c/validate-schema! new-note Note)
    (data/insert-note! new-note)
    {:status 200
     :body (assoc new-note :can-edit? true :can-delete? true)}))

(defn is-author [user]
  (fn [m]
    (when-not (= (select-keys user [:login :account-source])
                 (select-keys (:author m) [:login :account-source]))
      {:message "You must be the author of this note."})))

(defn patch-note-handler [id]
  (fn [{:keys [edn-body user]}]
    (c/require-login! user)
    (let [_id (c/parse-mongo-id! id)
          note (data/find-note-by-id _id)
          new-note (-> note
                       (assoc :body (:body edn-body))
                       c/update-timestamps)]
      (when-not note
        (throw+
          {:status 404
           :body {:message (str "Note with id " id " not found.")}}))
      (c/validate! note [(is-author user)])
      (c/validate! new-note [body-not-empty])
      (c/validate-schema! new-note Note)
      (data/update-note! (:_id note) new-note)
      {:status 200
       :body (assoc new-note :can-edit? true :can-delete? true)})))

(defn delete-note-handler [id]
  (fn [{:keys [user]}]
    (c/require-login! user)
    (let [_id (c/parse-mongo-id! id)
          note (data/find-note-by-id _id)]
      (when-not note
        (throw+
          {:status 404
           :body {:message (str "Note with id " id " not found.")}}))
      (c/validate! note [(is-author user)])
      (data/delete-note! _id)
      {:status 200
       :body {:message "Note deleted"}})))
