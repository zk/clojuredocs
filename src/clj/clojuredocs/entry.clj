(ns clojuredocs.entry
  (:use [ring.middleware
         file
         file-info
         session
         params
         nested-params
         multipart-params
         keyword-params]
        [ring.middleware.session.cookie :only (cookie-store)])
  (:require [compojure.core :refer (defroutes GET POST PUT DELETE)]
            [clojure.string :as str]
            [clojuredocs.env :as env]
            [clojuredocs.layout :as layout]
            [clojure.pprint :refer (pprint)]
            [clucy.core :as clucy]))

(defn html-resp [body]
  {:headers {"Content-Type" "text/html;charset=utf-8"}
   :body body})

(def $index
  [:div
   [:div.row
    [:div.col-md-12
     [:section
      [:h1 "ClojureDocs is a community-powered documentation and examples repository for the " [:a {:href "http://clojure.org"} "Clojure programming language"] "."]]
     [:section.search
      [:form {:method :get :action "/search" :autocomplete "off"}
       [:input.form-control {:type "text"
                             :name "query"
                             :placeholder "What do you need help with?"
                             :autofocus "autofocus"
                             :autocomplete "off"}]]
      [:table.ac-results]]]]
   [:div.row
    [:div.col-md-6
     [:section
      [:h3 "Getting started with ClojureDocs"]
      [:p "Finding the right tool for the job can be tough, so we've outlined a few ways to go about your search below."]
      [:ul
       [:li [:i.icon-search] "Use the search box above to find what you're looking for."]
       [:li [:i.icon-map-marker] "Take a look at the Clojure Core quickref, which displays Clojure vars grouped by category."]
       [:li [:i.icon-book] "Browse an alphabetical list of vars defined in Clojure Core or Contrib."]]]
     [:section
      [:h3 "Contribute to ClojureDocs"]
      [:p "We need your help to make ClojureDocs a great community resource. Here are a couple of ways you can contribute."]
      [:ul
       [:li
        [:h4 [:i.icon-comment-alt] "Give Feedback"]
        [:p "Please " [:a {:href "https://github.com/zk/clojuredocs/issues"} "open a ticket"] " if you have an idea of how we can improve ClojureDocs."]]
       [:li
        [:h4 [:i.icon-indent-right] "Add an Example"]
        [:p "Sharing your knowledge with fellow Clojurists is easy:"]
        [:p "First, take a look at the examples style guide, and then add an example for your favorite var (or pick one from the list)."]
        [:p "In addition to examples, you also have the ability to add 'see also' references between vars."]]]]]
    [:div.col-md-5.col-md-offset-1
     [:h3 "Top Contributors"]]]])

(def search-index (clucy/memory-index))

(binding [clucy/*analyzer* (org.apache.lucene.analysis.core.WhitespaceAnalyzer. clucy/*version*)]
  (doseq [nm (->> ['clojure.core 'clojure.zip]
                  (mapcat ns-publics)
                  (map second)
                  (map meta)
                  (map #(update-in % [:ns] str))
                  (map #(update-in % [:name] str))
                  (map #(select-keys % [:ns :arglists :file :name
                                        :column :added :static :doc :line])))]
    (clucy/add search-index nm)))

;; https://gist.github.com/ck/960716
(defn- compute-next-row
  "computes the next row using the prev-row current-element and the other seq"
  [prev-row current-element other-seq pred]
  (reduce
    (fn [row [diagonal above other-element]]
      (let [update-val
	     (if (pred other-element current-element)
	       diagonal
	       (inc (min diagonal above (peek row)))
	       )]
	(conj row update-val)))
    [(inc (first prev-row))]
    (map vector prev-row (next prev-row) other-seq)))

(defn levenshtein-distance
  "Levenshtein Distance - http://en.wikipedia.org/wiki/Levenshtein_distance
In information theory and computer science, the Levenshtein distance is a metric for measuring the amount of difference between two sequences. This is a functional implementation of the levenshtein edit
distance with as little mutability as possible.

Still maintains the O(n*m) guarantee.
"
  [a b & {p :predicate  :or {p =}}]
  (peek
    (reduce
      (fn [prev-row current-element]
	(compute-next-row prev-row current-element b p))
      (map #(identity %2) (cons nil b) (range))
      a)))

(defn lucene-escape [s]
  (-> s
      (str/replace #"[\+\-\!\(\)\{\}\[\]\^\"\~\*\?\:\\]" "\\\\$0")))

(defn search [query]
  (when-not (empty? query)
    (->> (clucy/search search-index (str (lucene-escape query) "*") 1000)
         (map #(assoc % :edit-distance (levenshtein-distance (str (:name %)) query)))
         (sort-by :edit-distance)
         (take 10))))

(defroutes _routes
  (GET "/" []
    (fn [r]
      (-> {:content $index}
          layout/main
          html-resp)))
  (GET "/search" []
    (fn [{:keys [params]}]
      {:headers {"Content-Type" "application/edn"}
       :body (pr-str (search (:query params)))})))

(def session-store
  (cookie-store
    {:key (env/str :session-key "abcdefg")
     :domain ".clojuredocs.org"}))

(def routes
  (-> _routes
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      (wrap-session {:store session-store})
      (wrap-file "resources/public" {:allow-symlinks? true})
      wrap-file-info))
