(ns clojuredocs.main
  (:require [dommy.utils :as utils]
            [dommy.core :as dom]
            [clojure.string :as str]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.sticky :as sticky]
            [clojuredocs.widgets :as widgets]
            [clojuredocs.styleguide :as styleguide]
            [highlight]
            [clojuredocs.anim :as anim])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

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

(defn munge-name [s]
  (-> s
      str
      (str/replace #"\." "_dot")
      (str/replace #"/" "_div")
      (str/replace #"\?" "_qm")))

(defn path-for-var [{:keys [ns name]}]
  (str "/" ns "/" (munge-name name)))

(defn $ac-result [{:keys [name ns doc] :as v}]
  (node [:a.ac-result-link {:href (path-for-var v)}
         [:tr.ac-result
          [:td.name
           (str name)
           [:div.ac-metadata
            [:a {:href (str "/" ns)} (str ns)]]]
          [:td.docstring
           (ellipsis (str doc) 200)]]]))

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

(defn key-code [e]
  (.-keyCode e))

(defn ctrl? [e]
  (.-ctrlKey e))

(defn navigate-to [url]
  (aset (.-location js/window) "href" url))

(apply init widgets/init)
(apply init styleguide/init)

(defn animated-scroll-init [$el]
  (let [$target (-> $el
                    (dom/attr :href)
                    sel1)
        buffer (-> $el
                   (dom/attr :data-animate-buffer))
        buffer (if (and buffer (string? buffer))
                 (js/parseInt buffer)
                 10)]
    (dom/listen! $el :click #(anim/scroll-to $target {:pad buffer}))))

(init
  "[data-sticky-offset]" sticky/init
  "[data-animate-scroll]" animated-scroll-init)


(aset (aget js/SyntaxHighlighter "defaults") "toolbar" false)
(aset (aget js/SyntaxHighlighter "defaults") "gutter" false)
(.all js/SyntaxHighlighter)

(dom/listen! (sel1 :body) :keydown
  (fn [e]
    (when (and (.-ctrlKey e) (= 83 (.-keyCode e)))
      (.focus (sel1 ".search input[name='query']")))))
