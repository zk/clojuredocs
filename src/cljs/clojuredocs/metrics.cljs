(ns clojuredocs.metrics)

(defn ga-event [category action label value]
  (js/ga "send" "event" category action label value))

(defn track-search [query]
  (ga-event "search" query "count" 1))

(defn track-search-choose [query choice]
  (ga-event "search-choose" query choice 1))
