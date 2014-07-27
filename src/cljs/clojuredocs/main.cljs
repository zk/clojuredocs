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
             :refer [<! >! chan close! sliding-buffer put! alts! timeout pipe mult tap]])
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

(defn wire-search [text-chan app-state]
  (go
    (while true
      (let [ac-text (<! text-chan)
            ac-response (<! (ajax-chan {:method :get
                                        :path (str "/search?query=" (util/url-encode ac-text))
                                        :data-type :edn}))
            data (-> ac-response :res :body)]
        (when (:success ac-response)
          (swap! app-state
            assoc
            :highlighted-index 0
            :loading? false
            :ac-results data))))))

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

(on-el
  :.btn.mobile-menu
  (fn [$el]
    (dommy/listen! $el :click
      (fn [e]
        (dommy/toggle-class! (sel1 :body) :mobile-push)
        (.stopPropagation e))))

  :body
  (fn [$el]
    (let [h (fn [_] (dommy/remove-class! (sel1 :body) :mobile-push))]
      (dommy/listen! $el :click h :touchstart h)))

  #_:.navbar-nav
  #_(fn [$el]
    (let [h (fn [e] (.stopPropagation e))]
      (dommy/listen! $el
        :click h
        :touchstart h
        :scroll h)))

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

  :.sg-quick-lookup-loading
  (fn [$el]
    (om/root
      search/$quick-lookup
      {:loading? true}
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
       :init-state {:expanded? true}})))

(dommy/listen! (sel1 :body) :keydown
  (fn [e]
    ;; ctrl-s to focus search input
    (when (and (.-ctrlKey e) (= 83 (.-keyCode e)))
      (.focus (sel1 ".search input[name='query']")))))


(def tog (atom false))

#_(js/setInterval
  (fn []
    (if (swap! tog not)
      (dommy/add-class! (sel1 :body) :mobile-push)
      (dommy/remove-class! (sel1 :body) :mobile-push)))
  1000)
