(ns clojuredocs.env
  "Shell environment helpers."
  (:require [clojure.string :as str])
  (:refer-clojure :exclude (int str)))

(defn clj->env [sym-or-str]
  (-> sym-or-str
      name
      (str/replace #"-" "_")
      (str/upper-case)))

(defn env
  "Retrieve environment variables by clojure keyword style.
   ex. (env :user) ;=> \"zk\""
  [sym & [default]]
  (or (System/getenv (clj->env sym))
      default))

(defn int
  "Retrieve and parse int env var."
  [sym & [default]]
  (if-let [env-var (env sym)]
    (Integer/parseInt env-var)
    default))

(defn str
  "Retrieve and parse string env var."
  [sym & [default]]
  (env sym default))

(defn bool
  [sym & [default]]
  (if-let [env-var (env sym)]
    (Boolean/parseBoolean env-var)
    default))
