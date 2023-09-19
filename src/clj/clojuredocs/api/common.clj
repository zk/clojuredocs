(ns clojuredocs.api.common
  (:require [slingshot.slingshot :refer [throw+]]
            [schema.core :as s]
            [clojuredocs.util :as util])
  (:import [org.bson.types ObjectId]))

;; Schemas

(def User
  {:login s/Str
   :account-source s/Str
   :avatar-url s/Str})

(def Var
  {:ns s/Str
   :name s/Str
   :library-url s/Str})



(defn require-login! [user]
  (when-not user
    (throw+
      {:body {:error "You must be logged in to edit an example."}
       :status 401})))

(defn validate-schema! [payload schema]
  (let [res (s/check schema payload)]
    (when res
      (throw+
        {:status 421
         :body {:failures res
                :schema schema}})))
  payload)


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

(defn parse-mongo-id! [id]
  (try
    (ObjectId. (str id))
    (catch IllegalArgumentException e
      (throw+
        {:status 400
         :body {:message "Couldn't parse mongo id"}}))))

(defn update-timestamps [{:keys [created-at updated-at] :as m}]
  (let [now (util/now)
        m (if created-at m (assoc m :created-at now))]
    (assoc m :updated-at now)))
