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
          html (sel1 :html)
          start (max  (.-scrollTop body) (.-scrollTop html))
          end (- (offset-top elem) pad)]
      (.tween js/morpheus
        250
        (fn [pos]
          (aset body "scrollTop" pos)
          (aset html "scrollTop" pos))
        nil
        nil
        start
        end)))

(defn scroll-to-top []
  (scroll-to (sel1 :body)))

;; From dommy.attrs
(defn bounding-client-rect
  "Returns a map of the bounding client rect of `elem`
   as a map with [:top :left :right :bottom :width :height]"
  [elem]
  (let [r (.getBoundingClientRect (node elem))]
    {:top (.-top r)
     :bottom (.-bottom r)
     :left (.-left r)
     :right (.-right r)
     :width (.-width r)
     :height (.-height r)}))

(defn scroll-into-view
  [elem & [opts]]
  (let [elem (node elem)
        {:keys [top bottom] :as res} (bounding-client-rect elem)]
    (when (or (< js/window.innerHeight
                (+ top (.-offsetHeight elem)))
              (< top 0))
      (scroll-to elem opts))))
