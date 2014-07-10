(ns clojuredocs.main
  (:require [dommy.utils :as utils]
            [dommy.core :as dom]
            [clojure.string :as str]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.sticky :as sticky]
            [clojuredocs.widgets :as widgets]
            [clojuredocs.styleguide :as styleguide]
            [highlight])
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

(defn scroll-to [$el]
  (let [scroll (.-pageYOffset js/window)
        top (offset-top $el)]
    (clog (- top scroll))
    (.scrollBy js/window 0 (- (- top scroll) 20))))

(defn key-code [e]
  (.-keyCode e))

(defn ctrl? [e]
  (.-ctrlKey e))

(defn navigate-to [url]
  (aset (.-location js/window) "href" url))

(apply init widgets/init)
(apply init styleguide/init)

#_(init
  #_[:form.search :input]
  #_(fn [$el]
    (let [$input (sel1 [$el :input])
          $ac (sel1 [:table.ac-results])
          results (atom [])]
      (dom/listen! $el :keydown
        (fn [e]
          (condp = (key-code e)
            ;; esc
            27 (do (dom/set-value! $el "")
                   (dom/clear! $ac))
            13 (navigate-to (path-for-var (first @results)))
            (clog (key-code e)))))
      (dom/listen! $el :input
        (fn [e]
          (prevent e)
          (ajax
            {:method :get
             :path (str "/search?query=" (-> $input dom/value url-encode))
             :success (fn [{:keys [body]}]
                        (dom/clear! $ac)
                        (reset! results body)
                        (->> body
                             (map $ac-result)
                             (dom/append! $ac)))})))))
  #_[:form.search]
  #_(fn [$el]
    (dom/listen! $el :submit
      (fn [e]
        (prevent e))))

  "[data-sticky-offset]" sticky/init

  #_:body #_(fn [$el]
          (dom/listen! $el :keydown
            (fn [e]
              (when (and (ctrl? e)
                         ;; s
                         (= 83 (key-code e)))
                (.focus (sel1 [:form.search :input])))))))


(aset (aget js/SyntaxHighlighter "defaults") "toolbar" false)
(aset (aget js/SyntaxHighlighter "defaults") "gutter" false)
(.all js/SyntaxHighlighter)

(dom/listen! (sel1 :body) :keydown
  (fn [e]
    (when (and (.-ctrlKey e) (= 83 (.-keyCode e)))
      (.focus (sel1 ".search input[name='query']")))))
