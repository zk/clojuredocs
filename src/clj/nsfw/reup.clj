(ns nsfw.reup
  "Utilities for supporting a clojure.tools.namespace reloading dev
  lifecycle.

  Add the following to your project.clj

  `:repl-options {:init (load-file \"reup.clj\")}`"
  (:require [clojure.tools.namespace.repl :as repl]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.java.classpath :as cp]
            [clojure.string :as str]))

(defn exception? [e]
  (isa? (type e) Exception))

(defn ns-for-sym [sym]
  (when (.contains (str sym) "/")
    (-> sym
        str
        (str/split #"/")
        first
        symbol)))

(defn setup
  "Helper for initializing a clojure.tools.namespace dev
  lifecycle. See
  http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded
  for more info.

  This will return a function that, when called, will stop the
  current environment, reload all namespaces, and start a new
  environment.

  Params:
  * `start-app-sym` -- FQ symbol of a no-arg function which
  starts the environment
  * `stop-app-sym` -- FQ symbol of a 1-arg
  function which stops the environment. The result of calling the
  start app function is passed in as it's first parameter
  * `tests-regex` -- Run tests after reload for all namespaces matching"

  [{:keys [start-app-sym stop-app-sym tests-regex]}]
  (when start-app-sym
    (when-not (resolve 'user/reup-app)
      (intern 'user 'reup-app nil))
    (when-not (resolve 'user/after-reup)
      (intern 'user 'after-reup
              (fn []
                (when start-app-sym
                  (binding [*ns* (find-ns 'user)]
                    (alter-var-root (resolve 'user/reup-app)
                                    (constantly (when-let [a (resolve start-app-sym)]
                                                  (let [f (deref a)]
                                                    (f))))))))))

    (require (ns-for-sym start-app-sym) :reload)
    (require (ns-for-sym stop-app-sym) :reload)

    (when-not (resolve start-app-sym)
      (throw (Exception. (str "Can't resolve start-app-sym: " start-app-sym))))

    (when-not (resolve stop-app-sym)
      (throw (Exception. (str "Can't resolve stop-app-sym: " stop-app-sym)))))

  (fn []
    (time
     (do
       (when start-app-sym
         (binding [*ns* (find-ns 'user)]
           (do
             (try
               (@(resolve stop-app-sym) @(resolve 'user/reup-app))
               (catch Exception e
                 (println "Exception stopping app:" e)))))
         (alter-var-root (resolve 'user/reup-app) (constantly nil)))
       (let [res (if start-app-sym
                   (repl/refresh :after 'user/after-reup)
                   (repl/refresh))]
         (when (exception? res)
           (throw res)))

       (when tests-regex
         (doseq [ns-sym (->> (cp/classpath-directories)
                             ns-find/find-namespaces
                             (filter #(re-find tests-regex (str %))))]
           (require ns-sym))
         (clojure.test/run-all-tests tests-regex))))))
