(ns clojuredocs.search
  (:require [clucy.core :as clucy]
            [clojure.string :as str]))

(def search-index (clucy/memory-index))

(def special-forms
  [{:name 'def
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
    :doc "Assignment special form. When the first operand is a field member access form, the assignment is to the corresponding field. If it is an instance field, the instance expr will be evaluated, then the expr. In all cases the value of expr is returned. Note - you cannot assign to function params or local bindings. Only Java fields, Vars, Refs and Agents are mutable in Clojure. See http://clojure.org/special_forms for more information."}])

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
    clojure.zip
    clojure.core.async])

(def searchable-vars
  (do
    (doseq [ns-sym clojure-namespaces]
      (require ns-sym))
    (->> clojure-namespaces
         (mapcat ns-publics)
         (map second)
         (map meta)
         (map #(update-in % [:ns] str))
         (map #(update-in % [:name] str))
         (map #(select-keys % [:ns :arglists :file :name
                               :column :added :static :doc :line]))
         (concat special-forms)
         (map #(-> %
                   (update-in [:ns] str)
                   (update-in [:name] str))))))

(binding [clucy/*analyzer* (org.apache.lucene.analysis.core.WhitespaceAnalyzer. clucy/*version*)]
  (doseq [nm searchable-vars]
    (clucy/add search-index nm)))

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
    (->> (clucy/search search-index (str (lucene-escape (str/trim q)) "*") 1000)
         (map #(assoc % :edit-distance (levenshtein-distance (str (:name %)) q)))
         (sort-by :edit-distance)
         (take 5))))
