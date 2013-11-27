(ns clojuredocs.site.gh-auth
  (:require [clojuredocs.config :as config]
            [clojuredocs.env :as env]
            [clojuredocs.github :as gh]
            [ring.util.response :refer (redirect)]
            [compojure.core :refer (defroutes GET)]))

(defn gh-user->user [{:keys [avatar_url id login]}]
  {:avatar-url avatar_url
   :id id
   :login login})

(defroutes routes
  (GET "/gh-callback" []
    (fn [{:keys [params]}]
      (try
        (let [token (:access_token (gh/exchange-code config/gh-creds (:code params)))
              user (gh/user token)]
          (-> (redirect "/")
              (assoc :session {:user (gh-user->user user)})))
        (catch Exception e
          (println e)
          (-> (redirect "/gh-auth")
              (assoc :session nil)))))))
