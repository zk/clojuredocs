(ns clojuredocs.sticky
  (:require [dommy.utils :as utils]
            [dommy.core :as dom])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(defn clog [& args]
  (.log js/console (pr-str args)))

(defn parse-int [s & [default]]
  (try
    (js/parseInt s)
    (catch js/Error e
      (if default
        default
        (throw e)))))

(defn init [$el]
  (let [px-offset (-> $el
                      (dom/attr :data-sticky-offset)
                      (parse-int 100))]
    (dom/listen! js/window :scroll
      (fn [_]
        (if (> (.-pageYOffset js/window) px-offset)
          (dom/add-class! $el :sticky)
          (dom/remove-class! $el :sticky))))))
