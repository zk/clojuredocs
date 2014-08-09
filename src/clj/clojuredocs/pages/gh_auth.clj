(ns clojuredocs.pages.gh-auth
  (:require [clojuredocs.config :as config]
            [clojuredocs.env :as env]
            [clojuredocs.github :as gh]
            [ring.util.response :refer (redirect)]
            [compojure.core :refer (defroutes GET)]))

(defn gh-user->user [{:keys [avatar_url id login]}]
  {:avatar-url avatar_url
   :account-source "github"
   :login login})

(defn callback-handler [path]
  (fn [{:keys [params]}]
    (try
      (let [token (:access_token (gh/exchange-code config/gh-creds (:code params)))
            user (gh/user token)]
        (-> (redirect (if (empty? path) "/" path))
            (assoc :session {:user (gh-user->user user)})))
      (catch Exception e
        (prn e)
        (-> (redirect "/gh-auth")
            (assoc :session nil))))))
