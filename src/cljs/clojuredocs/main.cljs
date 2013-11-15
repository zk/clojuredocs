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

(defn ellipsis [s n]
  (cond
    (<= (count s) 3) s
    (> n (count s))  s
    :else (str (->> s
                    (take n)
                    (apply str))
               "...")))

(defn $ac-result [{:keys [name ns doc]}]
  (node [:tr.ac-result
         [:td.name
          (str name)
          [:div.ac-metadata
           "1 ex, 2 sa"]]
         [:td.docstring (str doc)]]))

(defn prevent [e]
  (.preventDefault e))

(defn url-encode
  [string]
  (some-> string str (js/encodeURIComponent) (.replace "+" "%20")))

(init
  [:.search :form :input]
  (fn [$el]
    (let [$input (sel1 [$el :input])]
      (dom/listen! $el
        :input (fn [e]
                 (prevent e)
                 (ajax
                   {:method :get
                    :path (str "/search?query=" (-> $input dom/value url-encode))
                    :success (fn [resp]
                               (let [$ac (sel1 [:table.ac-results])]
                                 (dom/clear! $ac)
                                 (->> resp
                                      :body
                                      (map $ac-result)
                                      (dom/append! $ac))))})))))

  [:.search :form]
  (fn [$el]
    (dom/listen! $el :submit
      (fn [e]
        (prevent e)))))
