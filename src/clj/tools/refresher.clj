(ns tools.refresher
  (:require [clojure.java.shell :as sh]
            [watchtower.core :as wt]))

(defn slurp-safe [path]
  (try
    (slurp path)
    (catch java.io.FileNotFoundException e
      nil)))

(defn refresh-safari []
  (->> ["osascript"
        "tell application \"Safari\" to tell its first document"
        "set its URL to (get its URL)"
        "end tell"]
       (interpose "-e")
       (apply sh/sh)))

(defn refresh-chrome
  "Refreshes first tab of chrome"
  []
  (->> ["osascript"
        "tell application \"Google Chrome\" to tell the first tab of its first window"
        "reload"
        "end tell"]
       (interpose "-e")
       (apply sh/sh)))

(defn refresh-on-change [refresh-fn paths]
  (wt/watcher
   paths
   (wt/rate 100)
   (wt/on-change (fn [args]
                   (println (->> args
                                 (map #(.getPath %))
                                 (interpose ", ")
                                 (apply str))
                            "changed, refreshing browser")
                   (refresh-fn))))
  nil)

(defn -main [& args]
  (let [browser (or (first args) "chrome")
        paths (or (rest args)
                  ["src/clj" "resources"])]
    (condp = browser
      "safari" (refresh-on-change refresh-safari paths)
      "chrome" (refresh-on-change refresh-chrome paths))))
