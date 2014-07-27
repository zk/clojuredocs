(ns clojuredocs.util
  (:require [goog.crypt :as gcrypt]
            [goog.crypt.Md5 :as Md5]
            [goog.crypt.Sha1 :as Sha1]
            [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]))

(defn pluralize [n single plural]
  (str n " " (if (= 1 n) single plural)))

(defn string->bytes [s]
  (gcrypt/stringToUtf8ByteArray s))  ;; must be utf8 byte array

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

(defn md5-hex [string]
  "convert utf8 string to md5 hex string"
  (bytes->hex (md5-bytes string)))

(defn cd-decode [s]
  (cond
    (= "_dot" s) "."
    (= "_." s) "."
    (= "_.." s) ".."
    :else (-> s
              (str/replace #"_fs" "/")
              (str/replace #"_bs" "\\")
              (str/replace #"_q" "?")

              ;; legacy
              (str/replace #"_dot" "."))))

(defn cd-encode [s]
  (cond
    (= "." s) "_."
    (= ".." s) "_.."
    :else (-> s
              (str/replace #"/" "_fs")
              (str/replace #"\\" "_bs")
              (str/replace #"\?" "_q"))))

(defn $var-link [ns name & contents]
  (vec (concat
         [:a {:href (str "/" ns "/" (cd-encode name))}]
         contents)))

(defn url-encode
  [string]
  (some-> string
    str
    (js/encodeURIComponent)
    (.replace "+" "%20")))

(defn navigate-to [url]
  (aset (.-location js/window) "href" url))

(defn $avatar [{:keys [email login avatar-url] :as user}]
  [:a {:href (str "/u/" login)}
   [:img.avatar
    {:src (or avatar-url
              (str "https://www.gravatar.com/avatar/"
                   (md5-hex email)
                   "?r=PG&s=32&default=identicon")) }]])

(defn now []
  (.now js/Date))

(defn timeago [millis]
  (when millis
    (let [ms (- (now) millis)
          s (/ ms 1000)
          m (/ s 60)
          h (/ m 60)
          d (/ h 24)
          y (/ d 365.0)]
      (cond
        (< s 60) "less than a minute"
        (< m 2) "1 minute"
        (< h 1) (str (int m) " minutes")
        (< h 2) "1 hour"
        (< d 1) (str (int h) " hours")
        (< d 2) "1 day"
        (< y 1) (str (int d) " days")
        :else (str (gstring/format "%.1f" y) " years")))))
