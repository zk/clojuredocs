(ns clojuredocs.search.static)

(def clojure-namespaces
  '[clojure.core
    clojure.core.async
    clojure.core.logic
    clojure.core.logic.fd
    clojure.core.logic.pldb
    clojure.core.reducers
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

(def special-forms
  (->> [{:name 'def
         :ns "clojure.core"
         :doc "Creates and interns or locates a global var with the name of symbol and a
namespace of the value of the current namespace (*ns*). See
http://clojure.org/special_forms for more information."}
        {:name 'if
         :ns "clojure.core"
         :doc "Evaluates test."}
        {:name 'do
         :ns "clojure.core"
         :doc "Evaluates the expressions in order and returns the value of the last. If no
expressions are supplied, returns nil. See http://clojure.org/special_forms
for more information."}
        {:name 'quote
         :ns "clojure.core"
         :doc "Yields the unevaluated form. See http://clojure.org/special_forms for more
information."}
        {:name 'var
         :ns "clojure.core"
         :doc "The symbol must resolve to a var, and the Var object itself (not its value)
is returned. The reader macro #'x expands to (var x). See
http://clojure.org/special_forms for more information."}
        {:name 'recur
         :ns "clojure.core"
         :doc "Evaluates the exprs in order, then, in parallel, rebinds the bindings of
the recursion point to the values of the exprs. See
http://clojure.org/special_forms for more information."}
        {:name 'throw
         :ns "clojure.core"
         :doc "The expr is evaluated and thrown, therefore it should yield an instance of
some derivee of Throwable. Please see http://clojure.org/special_forms#throw"}
        {:name 'try
         :ns "clojure.core"
         :doc "The exprs are evaluated and, if no exceptions occur, the value of the last
is returned. If an exception occurs and catch clauses are provided, each is
examined in turn and the first for which the thrown exception is an instance
of the named class is considered a matching catch clause. If there is a
matching catch clause, its exprs are evaluated in a context in which name is
bound to the thrown exception, and the value of the last is the return value
of the function. If there is no matching catch clause, the exception
propagates out of the function. Before returning, normally or abnormally,
any finally exprs will be evaluated for their side effects. See
http://clojure.org/special_forms for more information."}
        {:name 'catch
         :ns "clojure.core"
         :doc "The exprs are evaluated and, if no exceptions occur, the value of the last
is returned. If an exception occurs and catch clauses are provided, each is
examined in turn and the first for which the thrown exception is an instance
of the named class is considered a matching catch clause. If there is a
matching catch clause, its exprs are evaluated in a context in which name is
bound to the thrown exception, and the value of the last is the return value
of the function. If there is no matching catch clause, the exception
propagates out of the function. Before returning, normally or abnormally,
any finally exprs will be evaluated for their side effects. See
http://clojure.org/special_forms for more information."}
        {:name 'finally
         :ns "clojure.core"
         :doc "The exprs are evaluated and, if no exceptions occur, the value of the last
is returned. If an exception occurs and catch clauses are provided, each is
examined in turn and the first for which the thrown exception is an instance
of the named class is considered a matching catch clause. If there is a
matching catch clause, its exprs are evaluated in a context in which name is
bound to the thrown exception, and the value of the last is the return value
of the function. If there is no matching catch clause, the exception
propagates out of the function. Before returning, normally or abnormally,
any finally exprs will be evaluated for their side effects. See
http://clojure.org/special_forms for more information."}
        {:name '.
         :ns "clojure.core"
         :doc "The '.' special form is the basis for access to Java. It can be considered
a member-access operator, and/or read as 'in the scope of'. See
http://clojure.org/special_forms for more information."}
        {:name 'set!
         :ns "clojure.core"
         :doc "Assignment special form. When the first operand is a field member access
form, the assignment is to the corresponding field. If it is an instance
field, the instance expr will be evaluated, then the expr. In all cases
the value of expr is returned. Note - you cannot assign to function params
or local bindings. Only Java fields, Vars, Refs and Agents are mutable in
Clojure. See http://clojure.org/special_forms for more information."}
        {:name 'monitor-enter
         :ns "clojure.core"
         :doc "A synchronization primitive that should be avoided in user code. Use the
locking macro. See http://clojure.org/special_forms for more information."}
        {:name 'monitor-exit
         :ns "clojure.core"
         :doc "A synchronization primitive that should be avoided in user code. Use the
locking macro. See http://clojure.org/special_forms for more information."}
        {:name 'new
         :ns "clojure.core"
         :doc "Instantiate a class. See http://clojure.org/java_interop#new for
more information."}]
       (map #(assoc % :type "special-form"))))

(def concept-pages
  [{:name "Destructuring"
    :keywords "destructuring destructure destruct"
    :href "/concepts/destructuring"
    :desc "Destructuring allows you to assign names to values based on the structure of a parameter."}
   {:name "Functional Programming"
    :keywords "functional programming"
    :href "/concepts/functional-programming"
    :desc "Rooted in lambda calculus, functional programming is a the style of building programs in a declarative way favoring composition of first-class, pure, and higher-order functions, immutable data structures, laziness, and the elimination of side effects. "}])

(def searchable-pages
  (->> [{:name "Quick Reference"
         :keywords "help, getting started, quickref, quick reference"
         :href "/quickref"
         :desc "Clojure functions broken down by conceptual area (string manipulation, collections, etc)."}
        {:name "Laziness in Clojure"
         :keywords "lazy laziness lazyness sequences seq lazy evaluation"
         :href "/concepts/lazyness"
         :desc "Laziness is the deferred or delayed execution of some bit of code, opposite of eager or immediate evaluation. Laziness is used Clojure to enable execution composition and solutions to problems that involve infinite sequences. FIX THIS"}
        {:name "Read-Eval-Print Loop (REPL)"
         :keywords "repl read eval print loop"
         :href "/concepts/repl"
         :desc "A read–eval–print loop (REPL), also known as an interactive toplevel or language shell, is a simple, interactive computer programming environment that takes single user inputs (i.e. single expressions), evaluates them, and returns the result to the user; a program written in a REPL environment is executed piecewise. The term is most usually used to refer to programming interfaces similar to the classic Lisp interactive environment. Common examples include command line shells and similar environments for programming languages."}
        {:name "Thrush Operators (->, ->>)"
         :keywords "thrush operators -> ->> as->"
         :href "/concepts/thrush"
         :desc "http://thecomputersarewinning.com/post/Clojure-Thrush-Operator/"}
        {:name "Recursion"
         :keywords "recursion loop recur trampoline"
         :href "https://www.google.com/search?q=recursion"
         :desc "Recursion is the process of repeating items in a self-similar way. For instance, when the surfaces of two mirrors are exactly parallel with each other the nested images that occur are a form of infinite recursion."}]
       (concat concept-pages)
       (map #(assoc % :type "page"))))
