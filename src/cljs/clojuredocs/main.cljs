(ns clojuredocs.main
  (:require [clojuredocs.util :as util]
            [dommy.core :as dommy]
            [clojure.string :as str]
            [clojuredocs.ajax :refer [ajax]]
            [clojuredocs.sticky :as sticky]
            [clojuredocs.search :as search]
            [clojuredocs.see-alsos :as see-alsos]
            [clojuredocs.examples :as examples]
            [clojuredocs.notes :as notes]
            [highlight]
            [om.core :as om :include-macros true]
            [clojuredocs.anim :as anim]
            [cljs.reader :as reader]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]]
            #_[clj-fuzzy.metrics1 :as fuzzy])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel sel1]]))

(enable-console-print!)

(aset (aget js/SyntaxHighlighter "defaults") "toolbar" false)
(aset (aget js/SyntaxHighlighter "defaults") "gutter" false)
(.all js/SyntaxHighlighter)

(defn log [& args]
  (.log js/console args))

(defn clog [& args]
  (.log js/console (pr-str args)))

(defn init [& pairs]
  (doseq [[selector f] (partition 2 pairs)]
    (doseq [$el (sel selector)]
      (f $el))))

(def app-state (atom (reader/read-string (aget js/window "PAGE_DATA"))))

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

(defn ajax-chan [opts]
  (let [c (chan)]
    (ajax
      (merge
        opts
        {:success (fn [res]
                    (put! c {:success true :res res}))
         :error (fn [res]
                  (put! c {:success false :res res}))}))
    c))


;; From http://swannodette.github.io/2013/08/17/comparative/
(defn throttle*
  ([in msecs]
    (throttle* in msecs (chan)))
  ([in msecs out]
    (throttle* in msecs out (chan)))
  ([in msecs out control]
    (go
      (loop [state ::init last nil cs [in control]]
        (let [[_ _ sync] cs]
          (let [[v sc] (alts! cs)]
            (condp = sc
              in (condp = state
                   ::init (do (>! out v)
                            (>! out [::throttle v])
                            (recur ::throttling last
                              (conj cs (timeout msecs))))
                   ::throttling (do (>! out v)
                                  (recur state v cs)))
              sync (if last
                     (do (>! out [::throttle last])
                       (recur state nil
                         (conj (pop cs) (timeout msecs))))
                     (recur ::init last (pop cs)))
              control (recur ::init nil
                        (if (= (count cs) 3)
                          (pop cs)
                          cs)))))))
    out))

(defn throttle
  ([in msecs] (throttle in msecs (chan)))
  ([in msecs out]
    (->> (throttle* in msecs out)
      (filter #(and (vector? %) (= (first %) ::throttle)))
      (map second))))

(defn wire-search [text-chan app-state]
  (let [tin (chan)
        tout (chan)]
    (throttle tin 250 tout)
    (go
      (while true
        (let [ac-text (<! text-chan)]
          (swap! app-state assoc :search-loading? true)
          (when (empty? ac-text)
            (swap! app-state assoc :results-empty? false))
          (>! tin ac-text))))
    (go
      (while true
        (let [ac-text (<! tout)]
          (when (vector? ac-text)
            (let [ac-text (second ac-text)
                  ac-response (<! (ajax-chan {:method :get
                                              :path (str "/search?query=" (util/url-encode ac-text))
                                              :data-type :edn}))
                  data (-> ac-response :res :body)]
              (when (:success ac-response)
                (swap! app-state
                  assoc
                  :highlighted-index 0
                  :search-loading? false
                  :results-empty? (and (empty? data) (not (empty? ac-text)))
                  :ac-results data)))))))))

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

(init
  "[data-sticky-offset]" sticky/init
  "[data-animate-scroll]" animated-scroll-init)

(def text-chan (chan))

(wire-search text-chan app-state)

(on-el-om
  [:.examples-widget examples/$examples]
  [:.see-alsos-widget see-alsos/$see-alsos]
  [:.search-widget search/$quick-lookup {:init-state {:text-chan text-chan}}]
  [:.quick-search-widget search/$quick-search {:init-state {:text-chan text-chan}}]
  [:.ac-results-widget search/$ac-results]
  [:.search-feedback-widget search/$search-feedback
   (fn [$el app]
     {:init-state
      {:query (dommy/attr $el :data-query)}})]
  [:.notes-widget notes/$notes])


;; Styleguide

(def push-sels [:.mobile-push-wrapper :.mobile-nav-menu :.mobile-nav-bar])

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
                     :desc "Where to go to get started with Clojure. Provides a host of information  con the language, core concepts, tutorials, books, and videos to help you learn Clojure."}]}
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

  :.sg-examples-null-state
  (fn [$el]
    (om/root
      examples/$examples
      {:var {:name "bar" :ns "foo"}}
      {:target $el}))

  :.sg-examples-single
  (fn [$el]
    (om/root
      examples/$examples
      {:examples [{:body "user=> (map #(vector (first %) (* 2 (second %)))
            {:a 1 :b 2 :c 3})

([:a 2] [:b 4] [:c 6])

user=> (into {} *1)
{:a 2, :b 4, :c 6}"
                   :user {:email "zachary.kim@gmail.com"}
                   :history [{:user {:email "zachary.kim@gmail.com"}}]}]
       :var {:name "bar" :ns "foo"}}
      {:target $el}))

  :.sg-examples-lengths
  (fn [$el]
    (om/root
      examples/$examples
      {:examples [{:body "user=> (foo)"
                   :user {:email "zachary.kim@gmail.com"}
                   :history [{:user {:email "lee@writequit.org"}}
                             {:user {:email "zachary.kim@gmail.com"}}]}
                  {:body "user=> (map #(vector (first %) (* 2 (second %)))
            {:a 1 :b 2 :c 3})

([:a 2] [:b 4] [:c 6])

user=> (into {} *1)
{:a 2, :b 4, :c 6}"
                   :user {:email "zachary.kim@gmail.com"}
                   :history [{:user {:email "masondesu@gmail.com"}}
                             {:user {:email "lee@writequit.org"}}
                             {:user {:email "zachary.kim@gmail.com"}}]}
                  {:body "user=> (map #(vector (first %) (* 2 (second %)))
            {:a 1 :b 2 :c 3})

([:a 2] [:b 4] [:c 6])

user=> (into {} *1)
{:a 2, :b 4, :c 6}"
                   :user {:email "zachary.kim@gmail.com"}
                   :history [{:user {:email "foo@barrrrrrr.com"}}
                             {:user {:email "foo@barrrrrr.com"}}
                             {:user {:email "foo@barrrrr.com"}}
                             {:user {:email "foo@barrrr.com"}}
                             {:user {:email "foo@barrr.com"}}
                             {:user {:email "foo@barr.com"}}
                             {:user {:email "foo@bar.com"}}
                             {:user {:email "fickamanda@gmail.com"}}
                             {:user {:email "brentdillingham@gmail.com"}}
                             {:user {:email "masondesu@gmail.com"}}
                             {:user {:email "lee@writequit.org"}}
                             {:user {:email "zachary.kim@gmail.com"}}]}]
       :var {:name "bar" :ns "foo"}}
      {:target $el}))

  :.sg-add-example
  (fn [$el]
    (om/root
      examples/$add
      {}
      {:target $el :init-state {:expanded? true}}))

  :.sg-add-example-loading
  (fn [$el]
    (om/root
      examples/$add
      {}
      {:target $el
       :init-state {:expanded? true
                    :loading? true
                    :text "(defn greet [name]\n  (println \"Hello\" name))"}}))

  :.sg-add-example-errors
  (fn [$el]
    (om/root
      examples/$add
      {}
      {:target $el
       :init-state {:expanded? true
                    :error-message "This is where error messages that apply to the whole form go. And here's some other text to show what happens with a very long error message."
                    :text "(defn greet [name]\n  (println \"Hello\" name))"}}))

  :.sg-notes-null-state
  (fn [$el]
    (om/root
      notes/$notes
      {}
      {:target $el}))

  :.sg-notes-populated
  (fn [$el]
    (om/root
      notes/$notes
      {:notes [{:body "# Hello World\n\nThe quick brown fox **jumps** over the *lazy* dog.<pre>(heres some \"clojure\")</pre>"
                :user {:login "zk"
                       :avatar-url "https://avatars.githubusercontent.com/u/7194?"}
                :created-at (- (util/now) 10000000)}]}
      {:target $el}))

  :.sg-add-note
  (fn [$el]
    (om/root
      notes/$add
      {}
      {:target $el
       :init-state {:expanded? true}}))

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

  :.sg-add-see-also
  (fn [$el]
    (om/root
      see-alsos/$add
      {:var {:ns "foo" :name "bar"}}
      {:target $el
       :init-state {:expanded? true}})))


(dommy/listen! (sel1 :body) :keydown
  (fn [e]
    ;; ctrl-s to focus search input
    (when (and (.-ctrlKey e) (= 83 (.-keyCode e)))
      (doseq [$el (sel ".search input[name='query']")]
        (prn $el)
        (.focus $el)))))

(def tog (atom false))

#_(js/setInterval
  (fn []
    (if (swap! tog not)
      (dommy/add-class! (sel1 :body) :mobile-push)
      (dommy/remove-class! (sel1 :body) :mobile-push)))
  1000)


(.attach js/FastClick js/document.body)
