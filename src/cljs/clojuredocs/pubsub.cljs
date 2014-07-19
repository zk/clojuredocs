(ns clojuredocs.pubsub
  (:require [nsfw.util :as util]))

(defn mk-bus [] (atom {}))

(defn pub [bus [msg & rest :as payload]]
  (doseq [f (concat (get @bus msg))]
    (f payload))
  (doseq [f (concat (get @bus ::all))]
    (f payload)))

(defn sub
  ([bus f]
     (sub bus nil f))
  ([bus msg f]
      (swap! bus update-in [(or msg ::all)] concat [f])))
