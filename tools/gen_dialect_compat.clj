(ns tools.gen-dialect-compat
  "Generates resources/dialect-compat.edn — the static compatibility index
   mapping qualified var names to their supported dialects.

   Run from a lein repl:
     (load-file \"tools/gen_dialect_compat.clj\")
     (tools.gen-dialect-compat/generate!)"
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [cljs.analyzer.api :as ana-api]))

;;; --- Configuration ---

(def clj-version "1.12.4")
(def cljs-version "1.12.134")

(def output-path "resources/dialect-compat.edn")

;;; --- Special forms (hardcoded per decision 2026-04-28) ---

(def special-forms
  "The 15 special forms tracked by ClojureDocs in search/static.clj."
  '[def if do quote var recur throw try catch finally . set! monitor-enter monitor-exit new])

(def cljs-special-forms
  "13 of 15 — monitor-enter and monitor-exit are JVM threading primitives."
  (disj (set special-forms) 'monitor-enter 'monitor-exit))

(def bb-special-forms
  "All 15 — bb runs on JVM via SCI."
  (set special-forms))

;;; --- JVM vars ---

(defn jvm-vars
  "Returns a set of qualified strings for all vars in the given namespace."
  [ns-sym]
  (->> (ns-publics ns-sym)
       keys
       (map #(str ns-sym "/" %))
       set))

;;; --- ClojureScript vars ---

(defn cljs-core-vars
  "Extract cljs.core var names via the compiler analyzer.
   Returns both :defs and :macros to capture the full set."
  []
  (let [state (ana-api/empty-state)
        _ (ana-api/analyze-file state "cljs/core.cljs" {})
        ns-info (get-in @state [:cljs.analyzer/namespaces 'cljs.core])
        defs (set (keys (:defs ns-info)))
        macros (set (keys (:macros ns-info)))]
    (clojure.set/union defs macros)))

(defn cljs-string-vars
  "Extract clojure.string var names from CLJS."
  []
  (let [state (ana-api/empty-state)
        _ (ana-api/analyze-file state "cljs/core.cljs" {})
        _ (ana-api/analyze-file state "clojure/string.cljs" {})
        ns-info (get-in @state [:cljs.analyzer/namespaces 'clojure.string])
        defs (set (keys (:defs ns-info)))]
    defs))

;;; --- babashka vars ---

(defn bb-version
  "Get the installed bb version."
  []
  (let [{:keys [exit out]} (shell/sh "bb" "--version")]
    (when (zero? exit)
      (str/trim out))))

(defn bb-ns-publics
  "Query bb for the public vars in a namespace."
  [ns-sym]
  (let [expr (str "(keys (ns-publics '" ns-sym "))")
        {:keys [exit out err]} (shell/sh "bb" "-e" expr)]
    (if (zero? exit)
      (->> (edn/read-string out)
           (map symbol)
           set)
      (throw (ex-info (str "bb query failed for " ns-sym ": " err)
                      {:ns ns-sym :exit exit})))))

;;; --- Assembly ---

(defn build-compat-map
  "Build the full compatibility map: qualified-string -> #{:clj :cljs :bb}"
  []
  (println "Gathering JVM vars...")
  (let [jvm-core (jvm-vars 'clojure.core)
        jvm-string (jvm-vars 'clojure.string)
        jvm-all (clojure.set/union jvm-core jvm-string)

        _ (println "Analyzing ClojureScript compiler...")
        cljs-core (cljs-core-vars)
        cljs-str (cljs-string-vars)

        _ (println "Querying babashka...")
        bb-core (bb-ns-publics 'clojure.core)
        bb-str (bb-ns-publics 'clojure.string)

        _ (println "Building compatibility map...")
        compat (into {}
                     (for [qualified-name jvm-all]
                       (let [[ns-str name-str] (str/split qualified-name #"/" 2)
                              name-sym (symbol name-str)
                              dialects (cond-> #{:clj}
                                         (and (= ns-str "clojure.core")
                                              (contains? cljs-core name-sym))
                                         (conj :cljs)

                                         (and (= ns-str "clojure.string")
                                              (contains? cljs-str name-sym))
                                         (conj :cljs)

                                         (and (= ns-str "clojure.core")
                                              (contains? bb-core name-sym))
                                         (conj :bb)

                                         (and (= ns-str "clojure.string")
                                              (contains? bb-str name-sym))
                                         (conj :bb))]
                         [qualified-name dialects])))

        ;; Add special forms
        sf-entries (into {}
                        (for [sf special-forms]
                          (let [k (str "clojure.core/" sf)
                                dialects (cond-> #{:clj}
                                           (contains? cljs-special-forms sf)
                                           (conj :cljs)
                                           (contains? bb-special-forms sf)
                                           (conj :bb))]
                            [k dialects])))]
    (merge compat sf-entries)))

(defn generate!
  "Generate resources/dialect-compat.edn with version-pinned data."
  []
  (let [bb-ver (bb-version)
        _ (println (str "Versions: clj " clj-version
                        ", cljs " cljs-version
                        ", bb " bb-ver))
        compat-map (build-compat-map)
        data {:versions {:clj clj-version
                         :cljs cljs-version
                         :bb bb-ver}
              :vars compat-map}
        sorted-vars (into (sorted-map) (:vars data))
        data (assoc data :vars sorted-vars)]
    (spit output-path (pr-str data))
    (println (str "Wrote " (count sorted-vars) " entries to " output-path))
    (println (str "  All 3: " (count (filter #(= #{:clj :cljs :bb} (val %)) sorted-vars))))
    (println (str "  JVM+bb: " (count (filter #(= #{:clj :bb} (val %)) sorted-vars))))
    (println (str "  JVM+CLJS: " (count (filter #(= #{:clj :cljs} (val %)) sorted-vars))))
    (println (str "  JVM only: " (count (filter #(= #{:clj} (val %)) sorted-vars))))))
