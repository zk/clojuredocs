(ns clojuredocs.api.common
  (:require [slingshot.slingshot :refer [throw+]]
            [schema.core :as s]))

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
