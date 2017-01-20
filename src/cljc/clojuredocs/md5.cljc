(ns clojuredocs.md5
  #?(:clj
     (:require [clojure.string :as str])
     :cljs
     (:require [clojure.string :as str]
               [goog.crypt :as gcrypt]
               [goog.crypt.Md5 :as Md5]
               [goog.crypt.Sha1 :as Sha1]
               [goog.string :as gstring]
               [goog.string.format])))



#?(:cljs
   (do
     (defn string->bytes [s]
       (gcrypt/stringToUtf8ByteArray s)) ;; must be utf8 byte array

     (defn bytes->hex
       "convert bytes to hex"
       [bytes-in]
       (gcrypt/byteArrayToHex bytes-in))

     (defn hash-bytes [digester bytes-in]
       (do
         (.update digester bytes-in)
         (.digest digester)))

     (defn md5-
       "convert bytes to md5 bytes"
       [bytes-in]
       (hash-bytes (goog.crypt.Md5.) bytes-in))

     (defn md5-bytes
       "convert utf8 string to md5 byte array"
       [string]
       (md5- (string->bytes string)))

     (defn md5-hex
       "convert utf8 string to md5 hex string"
       [string]
       (when string
         (bytes->hex (md5-bytes string))))))



#? (:clj
    (defn md5-hex
      "Compute the hex MD5 sum of a string."
      [#^String str]
      (when str
        (let [alg (doto (java.security.MessageDigest/getInstance "MD5")
                    (.reset)
                    (.update (.getBytes str)))]
          (try
            (.toString (new BigInteger 1 (.digest alg)) 16)
            (catch java.security.NoSuchAlgorithmException e
              (throw (new RuntimeException e))))))))
