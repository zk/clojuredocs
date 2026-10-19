(ns clojuredocs.pages.gh-auth
  (:require [clojuredocs.config :as config]
            [clojuredocs.data :as data]
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
            gh-user (gh/user token)
            user (gh-user->user gh-user)]
        (data/upsert-user! user)
        (-> (redirect (if (empty? path) "/" path))
            (assoc :session {:user user})))
      (catch Exception e
        (prn e)
        (-> (redirect "/gh-auth")
            (assoc :session nil))))))
