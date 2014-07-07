(ns user
  (:require [clojure.tools.namespace.repl
             :refer (refresh refresh-all)]
            [clojuredocs.main]
            [clojure.java.shell :as sh]))

(defn refresh-chrome
  "Refreshes first tab of chrome"
  []
  (->> ["osascript"
        "tell application \"Google Chrome\" to tell the first tab of its first window"
        "reload"
        "end tell"]
       (interpose "-e")
       (apply sh/sh)))

(defonce system nil)

(defn init []
  (alter-var-root #'system
    (constantly (clojuredocs.main/create-app))))

(defn start []
  (alter-var-root #'system clojuredocs.main/start))

(defn stop []
  (alter-var-root #'system
    (fn [s]
      (when s
        (clojuredocs.main/stop s)))))

(defn go []
  (init)
  (start))

(defn restart []
  (stop)
  (refresh :after 'user/go))
