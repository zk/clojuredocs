(ns clojuredocs.search
  (:require [clucy.core :as clucy]
            [clojure.string :as str]
            [clojuredocs.util :as util]
            [clojure.pprint :refer [pprint]]
            [clojuredocs.search.static :as static]))

(def search-index (clucy/memory-index))

(defn tokenize-name [s]
  (when s
    (str
      s
      " "
      (->> (str/split s #"-")
           (interpose " ")
           (apply str)))))

(def var-keys
    [:ns
     :name
     :file
     :column
     :line
     :added
     :arglists
     :doc
     :static
     :tag ; convert to string
     :macro
     :dynamic
     :special-form
     :forms ; -> list of strings
     :deprecated
     :url
     :no-doc])

(defn cond-update-in [m keys & rest]
  (if (get-in m keys)
    (apply update-in m keys rest)
    m))

(defn type-of [{:keys [type macro arglists special-form]}]
  (cond
    type type
    macro "macro"
    (> (count arglists) 0) "function"
    special-form "special-form"
    :else "var"))

(defn transform-var-meta [m]
  (-> m
      (select-keys var-keys)
      (cond-update-in [:tag] #(if (class? %)
                                (.getName %)
                                (str %)))
      (cond-update-in [:forms] #(map str %))
      (update-in [:ns] str)
      (update-in [:arglists] #(map
                                (fn [arg-list-coll]
                                  (->> arg-list-coll
                                       (map str)
                                       (interpose " ")
                                       (apply str)))
                                %))
      (update-in [:name] str)))

(defn gather-var [ns-obj]
  (->> ns-obj
       ns-publics
       (map second)
       (map meta)))

(defn gather-vars [{:keys [namespaces library-url] :as lib}]
  (assoc lib :vars (->> namespaces
                        (map :name)
                        (map symbol)
                        (map find-ns)
                        (mapcat gather-var)
                        (concat static/special-forms)
                        (map transform-var-meta)
                        (map #(assoc % :library-url library-url))
                        (map #(assoc % :type (type-of %)))
                        (map #(assoc % :href (str "/" (:ns %) "/" (util/cd-encode (:name %))))))))

(defn gather-namespace [ns-name]
  (require (symbol ns-name))
  (let [sym (symbol ns-name)
        namespace (find-ns sym)
        meta (meta namespace)]
    (merge
      (select-keys meta [:doc :no-doc :added])
      {:name (str ns-name)})))

(defn gather-namespaces [{:keys [namespaces] :as lib}]
  (assoc lib
    :namespaces
    (->> namespaces
         (map gather-namespace)
         (remove :no-doc))))

(def clojure-lib
  (-> {:library-url "https://github.com/clojure/clojure"
       :version "1.10.1"
       :source-base-url "https://github.com/clojure/clojure/1.10.1/blob"
       :gh-tag-url "https://github.com/clojure/clojure/tree/clojure-1.10.1"
       :namespaces static/clojure-namespaces}
      gather-namespaces
      gather-vars))

(def searchable-vars
  (->> clojure-lib
       :vars
       (map #(assoc % :keywords (tokenize-name (:name %))))))

(def searchable-nss
  (->> static/clojure-namespaces
       (map (fn [sym]
              {:name (str sym)
               :keywords (str
                           (str sym)
                           " "
                           (->> (str/split (str sym) #"\.")
                                (interpose " ")
                                (apply str)))
               :type "namespace"}))
       (map (fn [{:keys [name] :as ns}]
              (assoc ns :href (str "/" name))))))

(binding [clucy/*analyzer* (org.apache.lucene.analysis.core.WhitespaceAnalyzer. clucy/*version*)]
  (doseq [nm searchable-vars]
    (clucy/add search-index nm))
  (doseq [ns searchable-nss]
    (clucy/add search-index ns))
  (doseq [page static/searchable-pages]
    (clucy/add search-index page)))

(def lookup-vars
  (->> searchable-vars
       (reduce #(assoc %1 (str (:ns %2) "/" (:name %2)) %2) {})))

(defn lookup [ns-name]
  (get lookup-vars ns-name))

(defn lucene-escape [s]
  (str/replace s #"[\+\-\!\(\)\{\}\[\]\^\"\~\*\?\:\\\/]" "\\\\$0"))

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

(defn drop-leading-stars [q]
  (when q
    (let [stripped (if (.startsWith q "*")
                     (->> (str/replace q #"\**" "")
                          (apply str))
                     q)]
      (when-not (empty? stripped)
        stripped))))

(defn escape-query [q]
  (when-not (empty? q)
    (org.apache.lucene.queryparser.classic.QueryParser/escape q)))

(defn format-query [q]
  (some-> q
    str/trim
    drop-leading-stars
    lucene-escape
    (str "*")))

(defn query [q]
  (cond
    (= "*" q) [(lookup "clojure.core/*")]

    :else
    (when-let [q (format-query q)]
      (->> (clucy/search search-index q 1000 :default-field "keywords")
           (map #(assoc % :edit-distance (levenshtein-distance (str (:name %)) q)))
           (sort-by :edit-distance)))))
