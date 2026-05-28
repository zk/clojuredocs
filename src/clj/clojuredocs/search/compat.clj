(ns clojuredocs.search.compat
  "Loads dialect-compat.edn at startup and provides lookup."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def dialect-compat
  (-> (io/resource "dialect-compat.edn")
      slurp
      edn/read-string))

(defn dialects-for
  "Returns the set of dialect keywords for a qualified var name,
   e.g. #{:clj :cljs :bb}, or nil if not found."
  [ns-str name-str]
  (get-in dialect-compat [:vars (str ns-str "/" name-str)]))

(comment
  ;; === Dialect compatibility verification ===
  ;; Evaluate these forms to verify the compat data is loaded
  ;; and lookups return expected results.

  ;; Data loaded?
  (some? dialect-compat)       ;; => true
  (contains? dialect-compat :vars)    ;; => true
  (contains? dialect-compat :versions) ;; => true

  ;; Total var count — should be > 700
  (count (:vars dialect-compat)) ;; => 715

  ;; All-three dialects (most common case)
  (dialects-for "clojure.core" "map")    ;; => #{:clj :cljs :bb}
  (dialects-for "clojure.core" "reduce") ;; => #{:clj :cljs :bb}

  ;; JVM-only (no CLJS or bb equivalent)
  (dialects-for "clojure.core" "gen-class")   ;; => #{:clj}
  (dialects-for "clojure.core" "proxy-name")  ;; => #{:clj}

  ;; JVM + babashka (no CLJS equivalent)
  (dialects-for "clojure.core" "agent")          ;; => #{:clj :bb}
  (dialects-for "clojure.core" "shutdown-agents") ;; => #{:clj :bb}

  ;; Cross-namespace
  (dialects-for "clojure.string" "split") ;; => #{:clj :cljs :bb}
  (dialects-for "clojure.set" "union")    ;; => #{:clj :cljs :bb}

  ;; Unknown var returns nil
  (dialects-for "clojure.core" "not-a-real-var") ;; => nil

  ;; Versions recorded
  (:versions dialect-compat))
  ;; => {:clj "1.12.4", :cljs "1.12.134", :bb "babashka v1.12.215"}
