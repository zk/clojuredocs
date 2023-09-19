(ns clojuredocs.main
  (:require [clojuredocs.anim :as anim]
            [clojuredocs.mods.search :as search]
            [clojuredocs.mods.styleguide :as styleguide]
            [clojuredocs.mods.var-page :as var-page]
            [clojuredocs.see-alsos :as see-alsos]
            [clojuredocs.sticky :as sticky]
            [clojuredocs.syntax :as syntax]
            [dommy.core :as dommy :refer-macros [sel sel1]]
            [nsfw.page :as page]
   #_[clj-fuzzy.metrics1 :as fuzzy]))

(enable-console-print!)

(doseq [$el (sel :pre.clojure)]
  (let [contents (dommy/text $el)
        highlighted (syntax/syntaxify contents :stringify-style? true)]
    (dommy/replace! $el highlighted)))

(defn log [& args]
  (.log js/console args))

(defn clog [& args]
  (.log js/console (pr-str args)))

(defn on-el
  [init-state & pairs]
  (let [unmount-fns
        (->> (partition 2 pairs)
             (mapcat
               (fn [[selector f]]
                 (->> (sel selector)
                      (map (fn [$el]
                             (f $el init-state)))
                      doall)))
             doall)]
    (fn []
      (doseq [f unmount-fns]
        (if (ifn? f)
          (f))))))

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

(defn mk-delete-example [app-state]
  (fn [ex-to-del]
    (swap! app-state update-in [:examples]
      (fn [exs]
        (vec (remove #(= ex-to-del %) exs))))))

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

(.attach js/FastClick js/document.body)

(defn bind-resize []
  (let [unbinds (doall
                  (for [$el (sel :.mobile-nav-menu)]
                    (do
                      (let [f (fn [$el]
                                (dommy/set-style! $el
                                  :height (str (.-innerHeight js/window) "px")))
                            handler (fn [_]
                                      (f $el))]
                        (dommy/listen! js/window :resize handler)
                        (f $el)
                        (fn []
                          (dommy/unlisten! js/window :resize handler))))))]
    (fn []
      (doseq [unbind unbinds]
        (unbind)))))

(defn init []
  (let [shutdown-els
        (on-el
          {}
          :.btn.mobile-menu
          (fn [$el]
            (let [f (fn [e]
                      (doseq [s push-sels]
                        (dommy/toggle-class! (sel1 s) :mobile-push))
                      (.stopPropagation e))]
              (dommy/listen! $el :click f)
              (fn []
                (dommy/unlisten! $el :click f))))

          :body
          (fn [$el]
            (let [h (fn [_]
                      (doseq [s push-sels]
                        (dommy/remove-class! (sel1 s) :mobile-push)))]
              (dommy/listen! $el :click h)
              (fn []
                (dommy/unlisten! $el :click h))))

          :.mobile-nav-menu
          (fn [$el]
            (let [h (fn [e] (.stopPropagation e))]
              #_(dommy/listen! $el
                  :click h
                  :touchstart h
                  :touchend h))

            (let [f #(doseq [s push-sels]
                       (dommy/remove-class! (sel1 s) :mobile-push))]
              (doseq [$a (sel $el :a)]
                (dommy/listen! $a :click f))
              (fn []
                (doseq [$a (sel $el :a)]
                  (dommy/unlisten! $a :click f)))))


          ;; Styleguide
          :.sg-quick-lookup
          (fn [$el]
            #_(om/root
                search/$quick-lookup
                {}
                {:target $el}))

          #_ :.sg-quick-lookup-autocomplete
          #_(fn [$el]
              (om/root
                search/$quick-lookup
                ex-ac-results
                {:target $el}))

          #_ :.sg-quick-lookup-null-state
          #_ (fn [$el]
               (om/root
                 search/$quick-lookup
                 {:results-empty? true}
                 {:target $el :init-state {:text "foo bar"}}))

          #_ :.sg-quick-lookup-loading
          #_ (fn [$el]
               (om/root
                 search/$quick-lookup
                 {:search-loading? true}
                 {:target $el}))

          #_ :.sg-see-alsos-null-state
          #_ (fn [$el]
               (om/root
                 see-alsos/$see-alsos
                 {:var {:ns "foo" :name "bar"}}
                 {:target $el}))

          #_ :.sg-see-alsos-populated
          #_ (fn [$el]
               (om/root
                 see-alsos/$see-alsos
                 {:var {:ns "foo" :name "bar"}
                  :see-alsos [{:_id "", :user {:login "mmwaikar"}, :created-at #inst "2011-10-14T13:29:04.000-00:00", :name "map-indexed", :ns "clojure.core", :doc "Returns a lazy sequence consisting of the result of applying f to 0\nand the first item of coll, followed by applying f to 1 and the second\nitem in coll, etc, until coll is exhausted. Thus function f should\naccept 2 arguments, index and item."} {:_id "", :user {:login "gstamp"}, :created-at #inst "2012-09-06T11:28:04.000-00:00", :name "pmap", :ns "clojure.core", :doc "Like map, except f is applied in parallel. Semi-lazy in that the\nparallel computation stays ahead of the consumption, but doesn't\nrealize the entire result unless required. Only useful for\ncomputationally intensive functions where the time of f dominates\nthe coordination overhead."} {:_id "", :user {:login "gstamp"}, :created-at #inst "2012-09-06T11:28:33.000-00:00", :name "amap", :ns "clojure.core", :doc "Maps an expression across an array a, using an index named idx, and\nreturn value named ret, initialized to a clone of a, then setting \neach element of ret to the evaluation of expr, returning the new \narray ret."} {:_id "", :user {:login "adereth"}, :created-at #inst "2013-06-21T19:20:53.000-00:00", :name "mapcat", :ns "clojure.core", :doc "Returns the result of applying concat to the result of applying map\nto f and colls.  Thus function f should return a collection."}]}
                 {:target $el}))

          :body.var-page var-page/init
          :body.styleguide-page styleguide/init
          :body search/init
          "[data-sticky-offset]" sticky/init
          "[data-animate-scroll]" animated-scroll-init)

        shutdown-search-listener
        (let [body (sel1 :body)
              f (fn [e]
                  ;; ctrl-s to focus search input
                  (when (and (.-ctrlKey e) (= 83 (.-keyCode e)))
                    (doseq [$el (sel ".search input.query")]
                      (.focus $el))
                    ;; prevent save dialog
                    (.preventDefault e)))]
          (dommy/listen! (sel1 :body) :keydown f)
          (fn []
            (dommy/unlisten! (sel1 :body) :keydown f)))

        ;; Hack to fix mobile safari ios 8 scrolling issue
        shutdown-resize (bind-resize)]
    (fn []
      (shutdown-els)
      (shutdown-search-listener)
      (shutdown-resize))))

;; redirection to enable reloading of init code
(defn reload [] (init))

;; project.clj entry
(defonce reload-hook (page/hook-reload-fn reload))
