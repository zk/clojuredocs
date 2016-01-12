(ns clojuredocs.data.import
  (:require [clojure.pprint :refer [pprint]]
            [somnium.congomongo :as mon]
            [clojure.edn :as edn]))
(comment
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
         (map meta)
         (map transform-var-meta)))

  (defn gather-vars [{:keys [namespaces library-url] :as lib}]
    (assoc lib :vars (->> namespaces
                          (map :name)
                          (map symbol)
                          (map find-ns)
                          (mapcat gather-var)
                          (map #(assoc % :library-url library-url)))))

  (defn gather-namespace [ns-name]
    (require (symbol ns-name))
    (let [sym (symbol ns-name)
          namespace (find-ns sym)
          meta (meta namespace)]
      (merge
        (select-keys meta [:doc :no-doc :added])
        {:name ns-name})))

  (defn gather-namespaces [{:keys [namespaces] :as lib}]
    (assoc lib
      :namespaces
      (->> namespaces
           (map gather-namespace)
           (remove :no-doc))))

  ["clojure.core"
   "clojure.data"
   "clojure.edn"
   "clojure.inspector"
   "clojure.instant"
   "clojure.java.browse"
   "clojure.java.io"
   "clojure.java.javadoc"
   "clojure.java.shell"
   "clojure.main"
   "clojure.pprint"
   "clojure.reflect"
   "clojure.repl"
   "clojure.set"
   "clojure.stacktrace"
   "clojure.string"
   "clojure.template"
   "clojure.test"
   "clojure.walk"
   "clojure.xml"
   "clojure.zip"]

  (defn import-clojure []
    (->> {:library-url "https://github.com/clojure/clojure"
          :version "1.6.0"
          :source-base-url "https://github.com/clojure/clojure/1.6.0/blob"
          :namespaces ["clojure.data"
                       "clojure.edn"
                       "clojure.zip"]}
         gather-namespaces
         gather-vars))

  (pprint (import-clojure))

  #_(-> {:library-url "https://github.com/clojure/clojure"
         :version "1.6.0"
         :source-base-url "https://github.com/clojure/clojure/1.6.0/blob"
         :namespaces ["clojure.core"
                      "clojure.data"
                      "clojure.edn"
                      "clojure.inspector"
                      "clojure.instant"
                      "clojure.java.browse"
                      "clojure.java.io"
                      "clojure.java.javadoc"
                      "clojure.java.shell"
                      "clojure.main"
                      "clojure.pprint"
                      "clojure.reflect"
                      "clojure.repl"
                      "clojure.set"
                      "clojure.stacktrace"
                      "clojure.string"
                      "clojure.template"
                      "clojure.test"
                      "clojure.walk"
                      "clojure.xml"
                      "clojure.zip"]}
        (update-in [:namespaces] gather-namespaces)
        (update-in [:vars] gather-vars)
        pprint)

  (declare special-forms)

  (mon/fetch-one :vars :where {:name "if-not"})

  (->> (ns-publics 'clojure.core)
       (map second)
       (map str)
       (filter #(re-find #"if" %)))

  (meta #'if-not)

  (pprint (meta #'map))

  (meta #'*print-level*)
  (meta #'all-ns)

  (first searchable-vars)

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

  (defn get-vars []
    (doseq [ns-sym clojure-namespaces]
      (require ns-sym))
    (->> clojure-namespaces
         (mapcat ns-publics)
         (map second)
         (map meta)))

  (defn get-nss []
    (doseq [ns-sym clojure-namespaces]
      (require ns-sym))
    (->> clojure-namespaces
         (map find-ns)
         (map meta)))

  (->> (get-vars)
       (mapcat keys)
       distinct
       pprint)

  (->> (get-vars)
       (filter :see-also)
       first
       pprint)

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
                                 :column :added :static :doc :line
                                 :added :static :tag :forms :deprecated]))
           (concat special-forms)
           (map #(-> %
                     (update-in [:ns] str)
                     (update-in [:name] str)
                     (assoc :type (cond
                                    (:type %) (:type %)
                                    (:macro %) "macro"
                                    (> (count (:arglists %)) 0) "function"
                                    (:special-form %) "special-form"
                                    :else "var")))))))


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
           :doc "The expr is evaluated and thrown, therefore it should yield an instance of
some derivee of Throwable. Please see http://clojure.org/special_forms#throw"}
          {:name
           'try
           :ns "clojure.core"
           :doc "The exprs are evaluated and, if no exceptions occur, the value of the last is returned. If an exception occurs and catch clauses are provided, each is examined in turn and the first for which the thrown exception is an instance of the named class is considered a matching catch clause. If there is a matching catch clause, its exprs are evaluated in a context in which name is bound to the thrown exception, and the value of the last is the return value of the function. If there is no matching catch clause, the exception propagates out of the function. Before returning, normally or abnormally, any finally exprs will be evaluated for their side effects. See http://clojure.org/special_forms for more information."}
          {:name '.
           :ns "clojure.core"
           :doc "The '.' special form is the basis for access to Java. It can be considered a member-access operator, and/or read as 'in the scope of'. See http://clojure.org/special_forms for more information."}
          {:name 'set!
           :ns "clojure.core"
           :doc "Assignment special form. When the first operand is a field member access form, the assignment is to the corresponding field. If it is an instance field, the instance expr will be evaluated, then the expr. In all cases the value of expr is returned. Note - you cannot assign to function params or local bindings. Only Java fields, Vars, Refs and Agents are mutable in Clojure. See http://clojure.org/special_forms for more information."}]
         (map #(assoc % :type "special-form"))))

  )
