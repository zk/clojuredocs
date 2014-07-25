(ns clojuredocs.search
  (:require [clucy.core :as clucy]
            [clojure.string :as str]
            [clojuredocs.util :as util]
            [clojure.pprint :refer [pprint]]))

(def search-index (clucy/memory-index))

(def special-forms
  (->> [{:name 'def
         :ns "clojure.core"
         :doc "Creates and interns or locates a global var with the name of symbol and a namespace of the value of the current namespace (*ns*). See http://clojure.org/special_forms for more information."}
        {:name 'if
         :ns "clojure.core"
         :doc "Evaluates test."}
        {:name 'do
         :ns "clojure.core"
         :doc "Evaluates the expressions in order and returns the value of the last. If no expressions are supplied, returns nil. See http://clojure.org/special_forms for more information."}
        {:name 'quote
         :ns "clojure.core"
         :doc "Yields the unevaluated form. See http://clojure.org/special_forms for more information."}
        {:name 'var
         :ns "clojure.core"
         :doc "The symbol must resolve to a var, and the Var object itself (not its value) is returned. The reader macro #'x expands to (var x). See http://clojure.org/special_forms for more information."}
        {:name 'recur
         :ns "clojure.core"
         :doc "Evaluates the exprs in order, then, in parallel, rebinds the bindings of the recursion point to the values of the exprs. See http://clojure.org/special_forms for more information."}
        {:name 'throw
         :ns "clojure.core"
         :doc "Evaluates the exprs in order, then, in parallel, rebinds the bindings of the recursion point to the values of the exprs. See http://clojure.org/special_forms for more information."}
        {:name 'try
         :ns "clojure.core"
         :doc "The exprs are evaluated and, if no exceptions occur, the value of the last is returned. If an exception occurs and catch clauses are provided, each is examined in turn and the first for which the thrown exception is an instance of the named class is considered a matching catch clause. If there is a matching catch clause, its exprs are evaluated in a context in which name is bound to the thrown exception, and the value of the last is the return value of the function. If there is no matching catch clause, the exception propagates out of the function. Before returning, normally or abnormally, any finally exprs will be evaluated for their side effects. See http://clojure.org/special_forms for more information."}
        {:name '.
         :ns "clojure.core"
         :doc "The '.' special form is the basis for access to Java. It can be considered a member-access operator, and/or read as 'in the scope of'. See http://clojure.org/special_forms for more information."}
        {:name 'set!
         :ns "clojure.core"
         :doc "Assignment special form. When the first operand is a field member access form, the assignment is to the corresponding field. If it is an instance field, the instance expr will be evaluated, then the expr. In all cases the value of expr is returned. Note - you cannot assign to function params or local bindings. Only Java fields, Vars, Refs and Agents are mutable in Clojure. See http://clojure.org/special_forms for more information."}]
       (map #(assoc % :type "special-form"))))

(def clojure-namespaces
  '[clojure.core
    clojure.data
    clojure.edn
    clojure.inspector
    clojure.instant
    clojure.java.browse
    clojure.java.io
    clojure.java.javadoc
    clojure.java.shell
    clojure.main
    clojure.pprint
    clojure.reflect
    clojure.repl
    clojure.set
    clojure.stacktrace
    clojure.string
    clojure.template
    clojure.test
    clojure.walk
    clojure.xml
    clojure.zip])

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
                        (map #(assoc % :library-url library-url))
                        (concat special-forms)
                        (map transform-var-meta)
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
       :version "1.6.0"
       :source-base-url "https://github.com/clojure/clojure/1.6.0/blob"
       :namespaces clojure-namespaces}
      gather-namespaces
      gather-vars))

(def searchable-vars
  (->> clojure-lib
       :vars
       (map #(assoc % :keywords (tokenize-name (:name %))))))

(def additional-ns-data
  {"clojure.zip" {:desc "Functional tree navigation and manipulation"}
   "clojure.main" {:desc "Environmental- and repl-related utility functions"}})

(def searchable-nss
  (->> clojure-namespaces
       (map (fn [sym]
              (merge
                {:name (str sym)
                 :keywords (str
                             (str sym)
                             " "
                             (->> (str/split (str sym) #"\.")
                                  (interpose " ")
                                  (apply str)))
                 :type "namespace"}
                (get additional-ns-data (str sym)))))
       (map (fn [{:keys [name] :as ns}]
              (assoc ns :href (str "/" name))))))

(def searchable-pages
  [{:name "Quick Reference"
    :keywords "help, getting started, quickref, quick reference"
    :href "/quickref"
    :desc "Clojure functions broken down by conceptual area (string manipulation, collections, etc)."
    :type "page"}])

(binding [clucy/*analyzer* (org.apache.lucene.analysis.core.WhitespaceAnalyzer. clucy/*version*)]
  (doseq [nm searchable-vars]
    (clucy/add search-index nm))
  (doseq [ns searchable-nss]
    (clucy/add search-index ns))
  (doseq [page searchable-pages]
    (clucy/add search-index page)))

(def lookup-vars
  (->> searchable-vars
       (reduce #(assoc %1 (str (:ns %2) "/" (:name %2)) %2))))

(defn lookup [ns-name]
  (get lookup-vars ns-name))

(doseq [m special-forms]
  (clucy/add search-index m))

(defn lucene-escape [s]
  (-> s
      (str/replace #"[\+\-\!\(\)\{\}\[\]\^\"\~\*\?\:\\\/]" "\\\\$0")))

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

(defn query [q]
  (when-not (empty? q)
    (->> (clucy/search search-index (str (lucene-escape (str/trim q)) "*") 1000 :default-field "keywords")
         (map #(assoc % :edit-distance (levenshtein-distance (str (:name %)) q)))
         (sort-by :edit-distance)
         (take 5))))
