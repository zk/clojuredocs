(ns clojuredocs.util
  (:require [cheshire.core :as json]
            [clojure.string :as str]))

(defn url-encode [s]
  (when s
    (java.net.URLEncoder/encode s)))

(defn to-json [o]
  (json/generate-string o))

(defn from-json [s]
  (json/parse-string s true))

(defn uuid []
  (-> (java.util.UUID/randomUUID)
      str
      (str/replace #"-" "")))

(defn md5
  "Compute the hex MD5 sum of a string."
  [#^String str]
  (when str
    (let [alg (doto (java.security.MessageDigest/getInstance "MD5")
                (.reset)
                (.update (.getBytes str)))]
      (try
        (.toString (new BigInteger 1 (.digest alg)) 16)
        (catch java.security.NoSuchAlgorithmException e
          (throw (new RuntimeException e)))))))

(defn bson-id
  ([]
     (org.bson.types.ObjectId.))
  ([id-or-str]
     (org.bson.types.ObjectId/massageToObjectId id-or-str)))
