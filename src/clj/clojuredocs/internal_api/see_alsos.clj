(ns clojuredocs.internal-api.see-alsos
  (:require [slingshot.slingshot :refer [throw+]]
            [somnium.congomongo :as mon]
            [schema.core :as s]
            [clojuredocs.util :as util]
            [clojuredocs.internal-api.common :as c]
            [clojuredocs.search :as search]))

;; Schemas

(def User
  {:login s/Str
   :account-source s/Str
   :avatar-url s/Str})

(def Var
  {:ns s/Str
   :name s/Str
   :library-url s/Str})

(def SeeAlso
  {:from-var Var
   :to-var Var
   :author User
   :created-at s/Int
   :_id org.bson.types.ObjectId})

(defn prevent-duplicates [{:keys [from-var to-var]}]
  (when-let [existing (mon/fetch-one :see-alsos :where {:from-var from-var :to-var to-var})]
    {:message "Sorry, that see also already exists."}))

(defn prevent-same [{:keys [from-var to-var]}]
  (when (= from-var to-var)
    {:message "Sorry, a var can't see-also itself."}))

(defn post-see-also-handler [{:keys [edn-body user]}]
  (c/require-login! user)
  (let [ns-name (:fq-to-var-name edn-body)
        from-var (:from-var edn-body)
        to-var (search/lookup ns-name)
        new-see-also {:from-var (select-keys from-var
                                  [:ns :name :library-url])
                      :to-var (select-keys to-var
                                  [:ns :name :library-url])
                      :author user
                      :created-at (util/now)
                      :_id (org.bson.types.ObjectId.)}]
    (when-not to-var
      (throw+
        {:status 404
         :body {:message (str "Couldn't find var \"" ns-name "\". Don't forget to fully qualify the var name.")} }))
    (c/validate! new-see-also [prevent-duplicates prevent-same])
    (c/validate-schema! new-see-also SeeAlso)
    (mon/insert! :see-alsos new-see-also)
    {:status 200 :body (assoc new-see-also :doc (:doc to-var) :can-delete? true)}))

(defn is-author? [user]
  (fn [m]
    (when-not (= user (:author m))
      {:message "Sorry, you can't delete that see also."})))

(defn delete-see-also-handler [id]
  (fn [{:keys [edn-body user]}]
    (c/require-login! user)
    (let [_id (org.bson.types.ObjectId. id)
          sa (mon/fetch-one :see-alsos :where {:_id _id})]
      (c/validate! sa [(is-author? user)])
      #_(mon/update! :see-alsos
        {:_id _id}
        (assoc sa :deleted-at (util/now)))
      (mon/destroy! :see-alsos {:_id _id})
      {:status 200
       :body sa})))
