(ns clojuredocs.util
  (require [cheshire.core :as json]))

(defn url-encode [s]
  (when s
    (java.net.URLEncoder/encode s)))

(defn to-json [o]
  (json/generate-string o))

(defn from-json [s]
  (json/parse-string s true))

(defn uuid []
  (java.util.UUID/randomUUID))
