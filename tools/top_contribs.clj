(ns tools.top-contribs
  (:require [somnium.congomongo :as mon]
            [clojure.pprint :refer (pprint)]))


(def scores (atom {}))

(time (doseq [{:keys [history]} (mon/fetch :examples)]
        (let [history (reverse history)
              first-user (-> history first :user)]
          (swap! scores update-in [first-user] #(+ 4 (or % 0)))
          (doseq [user (->> history rest (map :user))]
            (swap! scores update-in [user] #(inc (or % 0)))))))


(->> @scores
     (sort-by second)
     reverse
     (take 24)
     (map #(assoc (first %) :score (second %)))
     pprint)
