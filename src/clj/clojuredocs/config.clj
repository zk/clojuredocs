(ns clojuredocs.config
  (:require [clojuredocs.env :as env]))

(def gh-creds {:client-id (env/str :gh-client-id)
               :client-secret (env/str :gh-client-secret)})

(def base-url (env/str :base-url))

(def staging? (env/bool :staging false))

(def ga-tracking-id (env/str :ga-tracking-id "UA-17348828-3"))

(def cljs-dev? (env/bool :cljs-dev false))

(defn url [& s] (apply str base-url s))
