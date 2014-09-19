(ns clojuredocs.anim
  (:require [dommy.core :as dom])
  (:require-macros [dommy.macros :refer [node sel1]]))

(defn offset-parents
  "a lazy seq of offset parents of `node`"
  [elem]
  (->> elem
       (iterate #(.-offsetParent %))
       (take-while identity)))

(defn offset-top [el]
  (->> el
       offset-parents
       (map #(.-offsetTop %))
       (reduce +)))

(defn tween [el opts]
  (js/morpheus el (clj->js opts)))

(defn scroll-to
    [elem & [{:keys [pad]}]]
    (let [body (sel1 :body)
          start (.-scrollTop body)
          end (- (offset-top elem) pad)]
      (.tween js/morpheus
        250
        (fn [pos]
          (aset body "scrollTop" pos))
        nil
        nil
        start
        end)))

(defn scroll-to-top []
  (scroll-to (sel1 :body)))

(defn scroll-into-view
  [elem & [opts]]
  (let [elem (node elem)
        {:keys [top bottom]} (dom/bounding-client-rect elem)]
    (when (or (< js/window.innerHeight
                (+ top (.-offsetHeight elem)))
              (< top 0))
      (scroll-to elem opts))))
