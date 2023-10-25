(ns nsfw.util
  #?(:clj
     (:require [clojure.string :as str]
               [cheshire.custom :as json]
               [clojure.pprint :as pprint]
               [camel-snake-kebab.core :as csk]
               [cognitect.transit :as transit]
               [camel-snake-kebab.core :as csk])
     :cljs
     (:require [clojure.string :as str]
               [cognitect.transit :as transit]
               [cljs.pprint :as pprint]
               [camel-snake-kebab.core :as csk]))

  #?(:clj
     (:import
              [java.io ByteArrayInputStream ByteArrayOutputStream StringWriter]
              [java.security SecureRandom]
              [java.util Date UUID]
              [java.text SimpleDateFormat]
              [java.net URLDecoder URLEncoder]
              [org.bson.types ObjectId]
              [org.joda.time DateTime]
              [org.joda.time.format ISODateTimeFormat]))
  #?(:cljs
     (:import [goog.string StringBuffer]))

  (:refer-clojure :exclude [uuid]))


(defn now []
  #?(:clj
     (System/currentTimeMillis)
     :cljs
     (.now js/Date)))

(defn pp-str [o]
  #?(:clj
     (let [w (StringWriter.)]
       (pprint/pprint o w)
       (.toString w))
     :cljs
     (let [sb  (StringBuffer.)
           sbw (StringBufferWriter. sb)]
       (pprint/pprint o sbw)
       (str sb))))

#?(:clj (json/add-encoder ObjectId json/encode-str))

(defn to-json [o & [opts-or-replacer space]]
  #?(:clj
     (json/generate-string o opts-or-replacer)
     :cljs
     (.stringify js/JSON (clj->js o) opts-or-replacer space)))


#?(:clj
   (defn to-transit [o & [opts]]
     (let [bs (ByteArrayOutputStream.)]
       (transit/write
        (transit/writer bs :json opts)
        o)
       (.toString bs)))
   :cljs
   (defn to-transit [o & [opts]]
     (transit/write
      (transit/writer :json opts)
      o)))

#?(:clj
   (defn from-transit [s & [opts]]
     (when s
       (transit/read
        (transit/reader
         (if (string? s)
           (ByteArrayInputStream. (.getBytes s "UTF-8"))
           s)
         :json
         opts))))
   :cljs
   (defn from-transit [s & [opts]]
     (when s
       (transit/read
        (transit/reader :json opts)
        s))))


#?(:clj
   (defn uuid []
     (-> (UUID/randomUUID)
         str
         (str/replace #"-" ""))))

#?(:cljs
   (defn uuid []
     (let [d        (now)
           uuid-str "xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx"]
       (str/replace uuid-str
                    #"[xy]"
                    (fn [c]
                      (let [r (bit-or (mod (+ d (* (.random js/Math) 16)) 16) 0)
                            d (.floor js/Math (/ d 16.0))]
                        (.toString
                         (if (= "x" c)
                           r
                           (bit-or
                            (bit-and 0x3 r)
                            0x8))
                         16)
                        ))))))

(defn env-val [o]
  (csk/->SCREAMING_SNAKE_CASE o))

(defn env-case [o]
  (env-val o))

(defn lookup-map [key coll]
  (->> coll
       (map (fn [o]
              [(get o key)
               o]))
       (into {})))

(defn throw-str [& args]
  #?(:clj
     (throw (Exception. (str (apply str args))))
     :cljs
     (throw (js/Error. (apply str args)))))
