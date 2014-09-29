(ns clojuredocs.main
  (:require [clojuredocs.util :as util]
            [dommy.core :as dommy]
            [clojure.string :as str]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.sticky :as sticky]
            [clojuredocs.mods.search :as search]
            [clojuredocs.see-alsos :as see-alsos]
            [clojuredocs.examples :as examples]
            [clojuredocs.notes :as notes]
            [clojuredocs.mods.var-page :as var-page]
            [clojuredocs.mods.styleguide :as styleguide]
            [om.core :as om :include-macros true]
            [clojuredocs.anim :as anim]
            [clojuredocs.canary :as canary]
            [cljs.reader :as reader]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            [clojuredocs.syntax :as syntax]
            #_[clj-fuzzy.metrics1 :as fuzzy])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [dommy.macros :refer [node sel sel1]]))

(enable-console-print!)

(doseq [$el (sel :pre.clojure)]
  (let [contents (dommy/text $el)
        highlighted (syntax/syntaxify contents :stringify-style? true)]
    (dommy/replace! $el (node highlighted))))

(defn log [& args]
  (.log js/console args))

(defn clog [& args]
  (.log js/console (pr-str args)))

(defn init [& pairs]
  (doseq [[selector f] (partition 2 pairs)]
    (doseq [$el (sel selector)]
      (f $el))))

(def app-state
  (atom (or (reader/read-string (aget js/window "PAGE_DATA")) {})))

(defn on-el [& pairs]
  (doseq [[selector f] (partition 2 pairs)]
    (doseq [$el (sel selector)]
      (f $el app-state))))

(defn on-el-om [& ws]
  (doseq [[sel widget-fn root-overrides] ws]
    (on-el sel
      (fn [$el app-state]
        (om/root
          widget-fn
          app-state
          (merge
            {:target $el}
            (if (fn? root-overrides)
              (root-overrides $el app-state)
              root-overrides)))))))

(defn ellipsis [s n]
  (cond
    (<= (count s) 3) s
    (> n (count s))  s
    :else (str (->> s
                    (take n)
                    (apply str))
               "...")))

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

(defn animated-scroll-init [$el]
  (let [href (dommy/attr $el :href)
        $target (if (= "#" href)
                  (sel1 :body)
                  (-> $el
                      (dommy/attr :href)
                      sel1))
        buffer (-> $el
                   (dommy/attr :data-animate-buffer))
        buffer (if (and buffer (string? buffer))
                 (js/parseInt buffer)
                 10)]
    (dommy/listen! $el :click
      (fn [e]
        (anim/scroll-to $target {:pad buffer})
        (.preventDefault e)))))

(def text-chan (chan))

(defn mk-delete-example [app-state]
  (fn [ex-to-del]
    (swap! app-state update-in [:examples]
      (fn [exs]
        (vec (remove #(= ex-to-del %) exs))))))

(def new-example-ch (chan))

(go-loop []
  (when-let [ex (<! new-example-ch)]
    (prn ex)
    (swap! app-state assoc :create-success? true)
    (recur)))

;; Styleguide

(def push-sels [:.mobile-push-wrapper :.mobile-nav-menu :.mobile-nav-bar])

(def ex-ac-results
  {:ac-results [{:type "function"
                 :ns "clojure.core"
                 :name "map"
                 :doc "Returns a lazy sequence consisting of the result of applying f to the set of first items of each coll, followed by applying f to the set
of second items in each coll, until any one of the colls is
exhausted. Any remaining items in other colls are ignored. Function
f should accept number-of-colls arguments."}
                {:type "namespace"
                 :name "clojure.core"
                 :doc "Fundamental library of the Clojure language"}
                {:type "page"
                 :name "Getting Started"
                 :desc "Where to go to get started with Clojure. Provides a host of information  con the language, core concepts, tutorials, books, and videos to help you learn Clojure."}]})

(on-el
  :.btn.mobile-menu
  (fn [$el]
    (dommy/listen! $el :click
      (fn [e]
        (doseq [s push-sels]
          (dommy/toggle-class! (sel1 s) :mobile-push))
        (.stopPropagation e))))

  :body
  (fn [$el]
    (let [h (fn [_]
              (doseq [s push-sels]
                (dommy/remove-class! (sel1 s) :mobile-push)))]
      (dommy/listen! $el :click h)))

  :.mobile-nav-menu
  (fn [$el]
    (let [h (fn [e] (.stopPropagation e))]
      #_(dommy/listen! $el
          :click h
          :touchstart h
          :touchend h))

    (doseq [$a (sel $el :a)]
      (dommy/listen! $a :click #(doseq [s push-sels]
                                  (dommy/remove-class! (sel1 s) :mobile-push)))))

  :.sg-quick-lookup
  (fn [$el]

    (om/root
      search/$quick-lookup
      {}
      {:target $el}))

  :.sg-quick-lookup-autocomplete
  (fn [$el]
    (om/root
      search/$quick-lookup
      ex-ac-results
      {:target $el}))

  :.sg-quick-lookup-null-state
  (fn [$el]
    (om/root
      search/$quick-lookup
      {:results-empty? true}
      {:target $el :init-state {:text "foo bar"}}))

  :.sg-quick-lookup-loading
  (fn [$el]
    (om/root
      search/$quick-lookup
      {:search-loading? true}
      {:target $el}))

  :.sg-see-alsos-null-state
  (fn [$el]
    (om/root
      see-alsos/$see-alsos
      {:var {:ns "foo" :name "bar"}}
      {:target $el}))

  :.sg-see-alsos-populated
  (fn [$el]
    (om/root
      see-alsos/$see-alsos
      {:var {:ns "foo" :name "bar"}
       :see-alsos [{:_id "", :user {:login "mmwaikar"}, :created-at #inst "2011-10-14T13:29:04.000-00:00", :name "map-indexed", :ns "clojure.core", :doc "Returns a lazy sequence consisting of the result of applying f to 0\nand the first item of coll, followed by applying f to 1 and the second\nitem in coll, etc, until coll is exhausted. Thus function f should\naccept 2 arguments, index and item."} {:_id "", :user {:login "gstamp"}, :created-at #inst "2012-09-06T11:28:04.000-00:00", :name "pmap", :ns "clojure.core", :doc "Like map, except f is applied in parallel. Semi-lazy in that the\nparallel computation stays ahead of the consumption, but doesn't\nrealize the entire result unless required. Only useful for\ncomputationally intensive functions where the time of f dominates\nthe coordination overhead."} {:_id "", :user {:login "gstamp"}, :created-at #inst "2012-09-06T11:28:33.000-00:00", :name "amap", :ns "clojure.core", :doc "Maps an expression across an array a, using an index named idx, and\nreturn value named ret, initialized to a clone of a, then setting \neach element of ret to the evaluation of expr, returning the new \narray ret."} {:_id "", :user {:login "adereth"}, :created-at #inst "2013-06-21T19:20:53.000-00:00", :name "mapcat", :ns "clojure.core", :doc "Returns the result of applying concat to the result of applying map\nto f and colls.  Thus function f should return a collection."}]}
      {:target $el}))

  :.canary-tests-container
  (fn [$el]
    (canary/init $el))

  :body.var-page var-page/init
  :body.styleguide-page styleguide/init
  :body search/init)

(dommy/listen! (sel1 :body) :keydown
  (fn [e]
    ;; ctrl-s to focus search input
    (when (and (.-ctrlKey e) (= 83 (.-keyCode e)))
      (doseq [$el (sel ".search input[name='query']")]
        (.focus $el)))))

(def tog (atom false))

#_(js/setInterval
  (fn []
    (if (swap! tog not)
      (dommy/add-class! (sel1 :body) :mobile-push)
      (dommy/remove-class! (sel1 :body) :mobile-push)))
  1000)


(.attach js/FastClick js/document.body)

(init
  "[data-sticky-offset]" sticky/init
  "[data-animate-scroll]" animated-scroll-init)

;; Hack to fix mobile safari ios 8 scrolling issue
(doseq [$el (sel :.mobile-nav-menu)]
  (let [f (fn [$el]
            (dommy/set-style! $el
              :height (str (.-innerHeight js/window) "px")))]
    (dommy/listen! js/window :resize
      (fn [_]
        (f $el)))
    (f $el)))
