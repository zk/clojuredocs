(ns clojuredocs.util
  (:require [clojure.string :as str]
            [clojuredocs.md5 :as md5]
            #+clj  [cheshire.core :as json]
            #+clj  [clojure.pprint :refer [pprint]]
            #+cljs [goog.string :as gstring]
            #+cljs [cljs.reader :as reader])
  #+clj
  (:import [org.pegdown PegDownProcessor]
           [org.pegdown Parser]
           [org.pegdown Extensions]))

#+clj
(defn url-encode [s]
  (when s
    (java.net.URLEncoder/encode s)))

#+cljs
(defn url-encode
  [string]
  (some-> string
    str
    (js/encodeURIComponent)
    (.replace "+" "%20")))

#+clj
(defn url-encode [s]
  (when s
    (java.net.URLEncoder/encode s)))

(defn cd-decode [s]
  (when s
    (cond
      (= "_dot" s) "."
      (= "_." s) "."
      (= "_.." s) ".."
      :else (-> s
                (str/replace #"_fs" "/")
                (str/replace #"_bs" "\\")
                (str/replace #"_q" "?")

                ;; legacy
                (str/replace #"_dot" ".")))))

(defn cd-encode [s]
  (when s
    (cond
      (= "." s) "_."
      (= ".." s) "_.."
      :else (-> s
                (str/replace #"/" "_fs")
                (str/replace #"\\" "_bs")
                (str/replace #"\?" "_q")))))

(defn var-path [ns name]
  (str "/" ns "/" (cd-encode name)))

(defn $var-link [ns name & contents]
  (vec
    (concat
      [:a {:href (var-path ns name)}]
      contents)))

#+cljs
(defn navigate-to [url]
  (aset (.-location js/window) "href" url))


(def md5 md5/md5-hex)

#+cljs
(defn markdown [s]
  (when s
    (js/marked s)))

#+clj
(defn markdown [s]
  (when s
    (let [pd (PegDownProcessor. (int (bit-or
                                       Extensions/AUTOLINKS
                                       Extensions/FENCED_CODE_BLOCKS
                                       Extensions/TABLES)))]
      (.markdownToHtml pd s))))

(defn pluralize [n single plural]
  (str n " " (if (= 1 n) single plural)))


(defn now []
  #+clj  (System/currentTimeMillis)
  #+cljs (.now js/Date))

(defn $avatar [{:keys [email login avatar-url account-source] :as user} & [{:keys [size]}]]
  (let [size (str (or size 32))]
    [:a {:href (str (if (= "github" account-source)
                      "/u/"
                      "/uc/")
                    login)}
     [:img.avatar
      {:src (or (str avatar-url "&s=" size)
                (str "https://www.gravatar.com/avatar/"
                     (md5 email)
                     "?r=PG&s=" size "&default=identicon")) }]]))

(defn sformat [& args]
  #+cljs
  (apply gstring/format args)
  #+clj
  (apply format args))

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
        :else (str (sformat "%.1f" y) " years")))))

(defn to-json [o]
  #+clj  (json/generate-string o)
  #+cljs (.stringify js/JSON o))

(defn from-json [s]
  #+clj  (json/parse-string s true)
  #+cljs (.parse js/JSON s))


#+clj
(defn bson-id
  ([]
     (org.bson.types.ObjectId.))
  ([id-or-str]
     (org.bson.types.ObjectId/massageToObjectId id-or-str)))

#+clj
(defn uuid []
  (-> (java.util.UUID/randomUUID)
      str
      (str/replace #"-" "")))

#+clj
(defn pp-str [o]
  (let [w (java.io.StringWriter.)]
    (pprint o w)
    (str/trim (.toString w))))

#+cljs
(defn page-data! []
  (reader/read-string (aget js/window "PAGE_DATA")))


(defn is-author? [user o]
  (= (select-keys user [:login :account-source])
     (select-keys (:author o) [:login :account-source])))
