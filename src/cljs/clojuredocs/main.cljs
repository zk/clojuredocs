(ns clojuredocs.main
  (:require [dommy.utils :as utils]
            [dommy.core :as dom]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.sticky :as sticky])
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
  (node [:a {:href "#"}
         [:tr.ac-result
          [:td.name
           (str name)
           [:div.ac-metadata
            [:a {:href (str "/v/" ns)} (str ns)]]]
          [:td.docstring
           [:a {:href (str "/v/" ns "/" name)} (ellipsis (str doc) 200)]]]]))

(defn prevent [e]
  (.preventDefault e))

(defn url-encode
  [string]
  (some-> string str (js/encodeURIComponent) (.replace "+" "%20")))

(def raf
  (or (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      (.-mozRequestAnimationFrame js/window)
      (.-oRequestAnimationFrame js/window)
      (.-msRequestAnimationFrame js/window)
      (fn [f] (.setTimeout js/window f (/ 1000 60.0)))))

(defn offset-top [$el]
  (loop [y 0
         $el $el]
    (let [parent (.-offsetParent $el)]
      (if-not parent
        y
        (recur
          (+ y (.-offsetTop $el))
          parent)))))

(defn scroll-to [$el]
  (let [scroll (.-pageYOffset js/window)
        top (offset-top $el)]
    (clog (- top scroll))
    (.scrollBy js/window 0 (- (- top scroll) 20))))

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
                                 #_(scroll-to $el)
                                 (->> resp
                                      :body
                                      (map $ac-result)
                                      (dom/append! $ac))))})))))

  [:.search :form]
  (fn [$el]
    (dom/listen! $el :submit
      (fn [e]
        (prevent e))))

  "[data-sticky-offset]" sticky/init)
