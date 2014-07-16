(ns tools.old-import
  (:require [clojure.java.jdbc :as j]
            #_[clojure.java.jdbc.sql :as s]
            [clojure.pprint :refer (pprint)]
            [clojure.string :as str]
            [somnium.congomongo :as mon]))



(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/clojuredocs"
               :user "root"
               :password ""})

(defn clojure-core-lib []
  (first (j/query mysql-db ["SELECT * FROM libraries WHERE id=3"])))

(defn core-nss []
  (j/query mysql-db ["SELECT * FROM namespaces WHERE library_id=3"]))

(defn all-libs []
  (j/query mysql-db ["SELECT * FROM libraries"]))

(defn all-comments []
  (j/query mysql-db ["SELECT * FROM comments"]))

(defn lookup-user [user-id]
  (select-keys (first (j/query mysql-db ["SELECT * FROM users WHERE id=?" user-id])) [:email :login]))

(defn lookup-ns [nsid]
  {:ns (->> (j/query mysql-db ["SELECT * FROM namespaces WHERE id=?" nsid])
            first
            :name)})

(defn lookup-function [fid]
  (let [func (first (j/query mysql-db ["SELECT * FROM functions WHERE id=?" fid]))
        ns (lookup-ns (:namespace_id func))]
    {:name (:name func)
     :ns (:ns ns)
     :library-url "https://github.com/clojure/clojure"}))

(defn history-for [example-id]
  (->> (j/query mysql-db ["SELECT * FROM example_versions WHERE example_id=?" example-id])
       (map (fn [{:keys [updated_at body user_id]}]
              (when user_id ; some example versions don't have a user associated
                (let [user (lookup-user user_id)]
                  {:created-at updated_at
                   :body body
                   :user user}))))
       (filter identity)
       (sort-by :created-at)
       #_reverse))


(mon/mongo! :db :clojuredocs)

(defn insert-or-update-example [e]
  (mon/update! :examples (select-keys e [:library-url :ns :name :body]) e))

;; examples
#_(time
  (let [examples (->> (core-nss)
                      (map (fn [{:keys [library_id name id]}]
                             {:library-id library_id
                              :namespace-id id
                              :namespace name}))
                      (mapcat (fn [{:keys [namespace namespace-id]}]
                                (let [res (j/query mysql-db ["SELECT * FROM functions WHERE namespace_id=?" namespace-id])]
                                  (->> res
                                       (map (fn [{:keys [name id]}]
                                              {:ns namespace
                                               :name name
                                               :function-id id}))))))
                      (mapcat (fn [m]
                                (let [res (j/query mysql-db ["SELECT * FROM examples WHERE function_id=?" (:function-id m)])]
                                  (->> res
                                       (map (fn [{:keys [body user_id id created_at updated_at]}]
                                              (assoc m
                                                :body body
                                                :user-id user_id
                                                :example-id id
                                                :created-at created_at
                                                :updated-at updated_at)))))))
                      (map (fn [{:keys [user-id example-id] :as m}]
                             (let [user (lookup-user user-id)]
                               (assoc m
                                 :user user
                                 :history (history-for example-id)))))
                      (map #(assoc % :library-url "https://github.com/clojure/clojure"))
                      (map #(dissoc % :user-id :example-id :function-id)))]
    (doseq [e examples]
      (insert-or-update-example e))
    (println (mon/fetch-count :examples))))

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
    clojure.zip])

(defn format-arglist [a]
  (->> a
       (map pr-str)
       (map #(str/replace % #"," ""))
       (interpose " ")
       (apply str)))

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
                   (update-in [:name] str)))
         (map #(assoc % :library-url "https://github.com/clojure/clojure"))
         (map #(assoc % :arglists (map format-arglist (:arglists %))))
         (map #(assoc % :runtimes ["clj"])))))

(def version "1.6.0")

(defn insert-or-update-var [v]
  (mon/update! :vars (select-keys v [:library-url :ns :name]) v))

;; Vars
(doseq [v searchable-vars]
  (insert-or-update-var v))


;; See Alsos

(defn insert-or-update-sa [v]
  (mon/update! :see-alsos (select-keys v [:library-url :ns :name]) v))

(defn pull-ns-name [id]
  (let [v (first (j/query mysql-db ["SELECT * FROM functions WHERE id=?" id]))
        ns (first (j/query mysql-db ["SELECT * FROM namespaces WHERE id=?" (:namespace_id v)]))]
    {:name (:name v)
     :ns (:name ns)
     :doc (:doc v)}))

#_(def see-alsos (atom {}))

#_(time
  (doseq [{:keys [from_id to_id user_id created_at]}
          (j/query mysql-db ["SELECT * FROM see_alsos"])]
    (let [from (pull-ns-name from_id)
          to (pull-ns-name to_id)]
      (swap! see-alsos update-in [from] #(-> %
                                             (concat [(assoc to :created-at created_at
                                                             :user (lookup-user user_id))])
                                             distinct)))))

#_(def clj-nss (->> clojure-namespaces
                  (map str)
                  set))

#_(time (doseq [[v sas] @see-alsos]
        (insert-or-update-sa (assoc (select-keys v [:name :ns])
                               :vars (->> sas
                                          (filter #(get clj-nss (:ns %))))
                               :library-url "https://github.com/clojure/clojure"))))

#_(->> @see-alsos
     (filter #(= "map" (:name (first %))))
     (map #(assoc ()))
     first
     insert-or-update-var
     pprint)


#_(mon/fetch-one :see-alsos :where {:name "fnil"})



#_(mon/update! :libraries
  {:name "Clojure"}
  {:name "Clojure"
   :namespaces (->> clojure-namespaces
                    (map str))})


(def libs
  [{:name "Clojure"
    :version "1.6"
    :nss ["clojure.core"
          "clojure.data"
          "clojure.edn"
          "clojure.inspector"
          "clojure.instant"
          "clojure.java.browse"
          "clojure.java.classpath"
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
   {:name "core.async"
    :version "??"
    :nss ["clojure.core.async"]}
   {:name "core.logic"
    :version "0.8.8"}])

#_(doseq [{:keys [name meta]} (->> (all-ns)
                                 (filter #(re-find #"^clojure\.core\.logic" (str %)))
                                 #_(remove #(re-find #"^clojure.tools" (str %)))
                                 (map #(hash-map :name (str %) :meta (meta %)))
                                 (remove #(-> % :meta :skip-wiki))
                                 (sort-by :name))]
  (prn name))

(defn update-ns! [ns-map]
  (mon/update! :namespaces {:name (:name ns-map)} ns-map))


(let [nss ["clojure.core"
           "clojure.data"
           "clojure.edn"
           "clojure.inspector"
           "clojure.instant"
           "clojure.java.browse"
           "clojure.java.classpath"
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
           "clojure.zip"]]
  (->> nss
       (map symbol)
       (map find-ns)
       (map #(merge (meta %) {:name (str %)}))
       (map update-ns!)
       doall))



(->> (mon/fetch :vars)
     (sort-by #(-> % :name count))
     reverse
     first)


(->> (mon/fetch :namespaces)
     (sort-by #(-> % :name count))
     #_reverse
     (map (fn [{:keys [name ns]}]
            {:ns ns
             :name name
             :len (count name)}))
     first)

(->> (mon/fetch :vars)
     (mapcat (fn [{:keys [ns name arglists]}]
               (map #(hash-map :ns ns :name name :arglist % :len (count %)) arglists)))
     (sort-by #(-> % :arglist count))
     (drop-while #(= 0 (-> % :arglist count)))
     reverse
     #_(map (fn [{:keys [name ns]}]
            {:ns ns
             :name name
             :len (count name)}))
     (take 10))

(->> (mon/fetch :vars)
     (sort-by #(-> % :doc count))
     reverse
     first
     )


(defn ins-or-update-comment [cmt]
  (mon/update! :var-comments
    (select-keys cmt [:user :created-at :var])
    cmt))

(defn import-comments []
  (let [comments (->> (all-comments)
                      (map #(assoc % :user (lookup-user (:user_id %))))
                      (map #(assoc % :var (lookup-function (:commentable_id %))))
                      (map #(assoc % :created-at (:created_at %)))
                      (map #(assoc % :updated-at (:updated_at %)))
                      (map #(select-keys % [:updated-at :var :body :created-at :user])))]
    (doseq [c comments]
      (ins-or-update-comment c))))

(import-comments)
