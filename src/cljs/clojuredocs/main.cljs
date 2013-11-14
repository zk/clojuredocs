(ns clojuredocs.main
  (:require [dommy.utils :as utils]
            [dommy.core :as dom]
            [clojuredocs.ajax :refer [ajax]])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(defn log [& args]
  (.log js/console args))

(defn clog [& args]
  (.log js/console (pr-str args)))

(defn init [& pairs]
  (clog "hi")
  (doseq [[selector f] (partition 2 pairs)]
    (doseq [$el (sel selector)]
      (f $el))))

(def !subs (atom {}))

(defn pub [msg & [payload]]
  (when-let [handlers (get @!subs msg)]
    (doseq [h handlers]
      (if payload
        (h payload)
        (h)))))

(defn sub [msg handler]
  (swap! !subs update-in [msg] #(concat % [handler])))

(init
  [:form.search :input]
  (fn [$el]
    (let [$input (sel1 [$el :input])]
      (dom/listen! $el
        :input (fn [e]
                 (ajax
                   {:method :get
                    :path (str "/search?query=" (dom/value $input))
                    :success (fn [resp]
                               (clog resp))}))))))
