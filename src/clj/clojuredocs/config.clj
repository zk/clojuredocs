(ns clojuredocs.config
  (:require [clojuredocs.env :as env]))

(def gh-creds {:client-id (env/str :gh-client-id)
               :client-secret (env/str :gh-client-secret)})

(def base-url (env/str :base-url))

(def staging? (env/bool :staging false))

(defn url [& s]
  (apply str base-url s))
