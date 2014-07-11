(ns clojuredocs.anim
  (:require [dommy.core :as dom]
            [morpheus])
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

(defn scroll-into-view
  [elem & [opts]]
  (dom/scroll-into-view elem false)
  #_(let [elem (node elem)
        {:keys [top bottom]} (dom/bounding-client-rect elem)]
    (when (or (< js/window.innerHeight
                (+ top (.-offsetHeight elem)))
              (< top 0))
      (scroll-to elem opts))))

(comment

  (defn tween [el opts]
    (.tween js/JSTween el (clj->js opts))
    (.play js/JSTween))



  )
