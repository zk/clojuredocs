(ns clojuredocs.quickref
  (:require [clojure.string :as str]
            [clojuredocs.site.common :as common]
            [clojuredocs.search :as search]))

(declare quickref-data)

(defn munge-name [s]
  (-> s
      str
      (str/replace #"\." "_dot")
      (str/replace #"\/" "_div")))

(defn title->id [k]
  (-> k
      name
      str/lower-case
      (str/replace #"[^a-z0-9]" "-")
      (str/replace #"-+" "-")))

(defn $group [{:keys [title syms]}]
  [:div.group
   [:div.quickref-header.clearfix
    [:h5 title]
    [:h5.header-reference "Simple Values > Numbers"]]
   [:dl.dl-horizontal
    (mapcat #(vector
               [:div.dl-row
                [:dt [:a {:href (str "/clojure.core/" (munge-name %))} (str %)]]
                [:dd (->> (str "clojure.core/" %)
                          search/lookup-vars
                          :doc
                          (take 50)
                          (apply str))
                 [:span.examples-count.pull-right
                  "1 ex."]]])
      syms)]])

(defn $category [{:keys [title groups]}]
  [:div.category
   [:div.category-header.clearfix
    [:h4 {:id (title->id title)} title]
    [:h4.header-reference "Simple Values"]]
   (map $group groups)])

(defn $sphere [{:keys [title categories]}]
  [:div.sphere
   [:div.sphere-header
    [:h3 {:id (title->id title)} title]]
   (map $category categories)])

(defn $toc-category [{:keys [title]}]
  [:li [:a {:href (str "#" (title->id title))} title]])

(defn $toc-sphere [{:keys [title categories]}]
  [:div.toc-sphere
   [:h3 [:a {:href (str "#" (title->id title))} title]]
   [:ul
    (map $toc-category categories)]])

(defn $toc [quickref-data]
  [:div.toc
   {:data-sticky-offset "70"}
   [:h2 "Table of Contents"]
   (map $toc-sphere quickref-data)])

(defn index [{:keys [user]}]
  (common/$main
    {:body-class "quickref-page"
     :user user
     :content
     [:div.row
      [:div.col-md-3
       ($toc quickref-data)]
      [:div.col-md-9
       [:h1 "Quickref for Clojure Core"]
       (map $sphere quickref-data)]]}))

(def quickref-data '({:title "Simple Values",
                      :categories
                      ({:title "Numbers",
                        :groups
                        ({:syms (+ - * / quot rem mod inc dec max min with-precision),
                          :title "Arithmetic"}
                         {:syms (= == not= < > <= >=), :title "Compare"}
                         {:syms
                          (bit-and
                            bit-or
                            bit-xor
                            bit-flip
                            bit-not
                            bit-clear
                            bit-set
                            bit-shift-left
                            bit-shift-right
                            bit-test),
                          :title "Bitwise Operations"}
                         {:syms
                          (byte short int long float double bigint bigdec num rationalize),
                          :title "Cast"}
                         {:syms (nil? identical? zero? pos? neg? even? odd?),
                          :title "Test"})}
                       {:title "Symbols / Keywords",
                        :groups
                        ({:syms (keyword symbol), :title "Create"}
                         {:syms (name intern namespace), :title "Use"}
                         {:syms (keyword? symbol?), :title "Test"})}
                       {:title "Strings / Characters",
                        :groups
                        ({:syms (str print-str println-str pr-str prn-str with-out-str),
                          :title "Create"}
                         {:syms (count get subs format), :title "Use"}
                         {:syms (char
                                  char? string?), :title "Cast / Test"})}
                       {:title "Regular Expressions",
                        :groups
                        ({:syms (re-pattern re-matcher), :title "Create"}
                         {:syms (re-find re-matches re-seq re-groups), :title "Use"})})}
                     {:title "Operations",
                      :categories
                      ({:title "Flow Control",
                        :groups
                        ({:syms
                          (if
                              if-not
                            if-let
                            when
                            when-not
                            when-let
                            when-first
                            cond
                            condp
                            case
                            do
                            eval
                            loop
                            recur
                            trampoline
                            while),
                          :title "Normal"}
                         {:syms (try catch finally throw assert), :title "Exceptional"}
                         {:syms (delay delay? deref force), :title "Delay"}
                         {:syms (repeatedly iterate), :title "Function Based"}
                         {:syms (dotimes doseq for), :title "Sequence Based"}
                         {:syms (lazy-seq lazy-cat doall dorun), :title "Laziness"})}
                       {:title "Type Inspection",
                        :groups
                        ({:syms (type extends? satisfies?), :title "Clojure Types"}
                         {:syms (class bases supers class? instance? isa? cast),
                          :title
                          "Java Types"})}
                       {:title "Concurrency",
                        :groups
                        ({:syms (deref get-validator set-validator!), :title "General"}
                         {:syms (atom swap! reset! compare-and-set!), :title "Atoms"}
                         {:syms
                          (ref
                            sync
                            dosync
                            ref-set
                            alter
                            commute
                            ensure
                            io!
                            ref-history-count
                            ref-max-history
                            ref-min-history),
                          :title "Refs"}
                         {:syms
                          (agent
                            send
                            send-off
                            await
                            await-for
                            agent-error
                            restart-agent
                            shutdown-agents
                            *agent*
                            error-handler
                            set-error-handler!
                            error-mode
                            set-error-mode!
                            release-pending-sends),
                          :title "Agents"}
                         {:syms
                          (future
                            future-call
                            future-done?
                            future-cancel
                            future-cancelled?
                            future?),
                          :title "Futures"}
                         {:syms
                          (bound-fn
                            bound-fn*
                            get-thread-bindings
                            push-thread-bindings
                            pop-thread-bindings
                            thread-bound?),
                          :title "Thread
 Local Values"}
                         {:syms
                          (locking
                              pcalls
                            pvalues
                            pmap
                            seque
                            promise
                            deliver
                            add-watch
                            remove-watch),
                          :title "Misc"})})}
                     {:title "Functions",
                      :categories
                      ({:title "General",
                        :groups
                        ({:syms
                          (fn
                            defn
                            defn-
                            definline
                            identity
                            constantly
                            memfn
                            comp
                            complement
                            partial
                            juxt
                            memoize),
                          :title "Create"}
                         {:syms (-> ->> apply), :title "Call"}
                         {:syms (fn? ifn?), :title "Test"})}
                       {:title "Multifunctions",
                        :groups
                        ({:syms (defmulti defmethod), :title "Create"}
                         {:syms
                          (get-method
                            methods
                            prefer-method
                            prefers
                            remove-method
                            remove-all-methods),
                          :title "Inspect and Modify"})}
                       {:title "Macros",
                        :groups
                        ({:syms (defmacro macroexpand macroexpand-1 gensym),
                          :title "Create"})}
                       {:title "Java Interop",
                        :groups
                        ({:syms (doto .. set!), :title "Objects"}
                         {:syms
                          (make-array
                            object-array
                            boolean-array
                            byte-array
                            char-array
                            short-array
                            int-array
                            long-array
                            float-array
                            double-array
                            aclone
                            to-array
                            to-array-2d
                            into-array),
                          :title "Array Creation"}
                         {:syms
                          (aget
                            aset
                            aset-boolean
                            aset-char
                            aset-byte
                            aset-int
                            aset-long
                            aset-short
                            aset-float
                            aset-double
                            alength
                            amap
                            areduce),
                          :title "Array Use"}
                         {:syms (booleans bytes chars ints shorts longs floats doubles),
                          :title "Casting"})}
                       {:title "Proxies",
                        :groups
                        ({:syms (proxy get-proxy-class construct-proxy init-proxy),
                          :title "Create"}
                         {:syms (proxy-mappings proxy-super update-proxy),
                          :title "Misc"})})}
                     {:title "Collections / Sequences",
                      :categories
                      ({:title "Collections",
                        :groups
                        ({:syms (count empty not-empty into conj),
                          :title "Generic Operations"}
                         {:syms
                          (contains?
                            distinct? empty? every? not-every? some not-any?),
                          :title "Content Tests"}
                         {:syms (sequential? associative? sorted? counted? reversible?),
                          :title "Capabilities"}
                         {:syms (coll? seq? vector? list? map? set?),
                          :title "Type Tests"})}
                       {:title "Vectors",
                        :groups
                        ({:syms (vec vector vector-of), :title "Create"}
                         {:syms (conj peek pop get assoc subvec rseq), :title "Use"})}
                       {:title "Lists",
                        :groups
                        ({:syms (list list*), :title "Create"}
                         {:syms (cons conj peek pop first rest), :title "Use"})}
                       {:title "Maps",
                        :groups
                        ({:syms
                          (hash-map
                            array-map
                            zipmap
                            sorted-map
                            sorted-map-by
                            bean
                            frequencies),
                          :title "Create"}
                         {:syms
                          (assoc
                              assoc-in
                            dissoc
                            find
                            key
                            val
                            keys
                            vals
                            get
                            get-in
                            update-in
                            select-keys
                            merge
                            merge-with),
                          :title "Use (Sorted Maps)"}
                         {:syms (rseq subseq subseq rsubseq rsubseq),

                          :title "Use"})}
                       {:title "Sets",
                        :groups
                        ({:syms (hash-set set sorted-set sorted-set-by), :title "Create"}
                         {:syms (conj disj get), :title "Use"})}
                       {:title "Structs",
                        :groups
                        ({:syms (defstruct create-struct struct struct-map accessor),
                          :title "Create"}
                         {:syms (get assoc), :title "Use"})}
                       {:title "Sequences",
                        :groups
                        ({:syms
                          (seq
                            sequence
                            repeat
                            replicate
                            range
                            repeatedly
                            iterate
                            lazy-seq
                            lazy-cat
                            cycle
                            interleave
                            interpose
                            tree-seq
                            xml-seq
                            enumeration-seq
                            iterator-seq
                            file-seq
                            line-seq
                            resultset-seq),
                          :title "Create"}
                         {:syms
                          (first
                            second
                            last
                            rest
                            next
                            ffirst
                            nfirst
                            fnext
                            nnext
                            nth
                            nthnext
                            rand-nth
                            butlast
                            take
                            take-last
                            take-nth
                            take-while
                            drop
                            drop-last
                            drop-while
                            keep

                            keep-indexed),
                          :title "Use (General)"}
                         {:syms
                          (conj
                            concat
                            distinct
                            group-by
                            partition
                            partition-all
                            partition-by
                            split-at
                            split-with
                            filter
                            remove
                            replace
                            shuffle),
                          :title "Use ('Modification')"}
                         {:syms
                          (for
                              doseq
                            map
                            map-indexed
                            mapcat
                            reduce
                            reductions
                            max-key
                            min-key
                            doall
                            dorun),
                          :title "Use (Iteration)"})}
                       {:title "Transients",
                        :groups
                        ({:syms (transient persistent!), :title "Create"}
                         {:syms (conj! pop! assoc! dissoc! disj!), :title "Use (General)"}
                         {:syms
                          (conj
                            concat
                            distinct
                            group-by
                            partition
                            partition-all
                            partition-by
                            split-at
                            split-with
                            filter
                            remove
                            replace
                            shuffle),
                          :title "Use ('Modification')"}
                         {:syms
                          (for
                              doseq
                            map
                            map-indexed
                            mapcat

                            reduce
                            reductions
                            max-key
                            min-key
                            doall
                            dorun),
                          :title "Use (Iteration)"})})}
                     {:title "Code Structure",
                      :categories
                      ({:title "Variables",
                        :groups
                        ({:syms (def defonce intern declare), :title "Create"}
                         {:syms
                          (set!
                            alter-var-root
                            binding
                            with-bindings
                            with-bindings*
                            with-local-vars
                            letfn
                            gensym),
                          :title "Use"}
                         {:syms
                          (var
                            find-var
                            var-get
                            var?
                            bound?
                            resolve
                            ns-resolve
                            special-symbol?),
                          :title "Inspect"})}
                       {:title "Namespaces",
                        :groups
                        ({:syms (ns create-ns remove-ns), :title "Create & Delete"}
                         {:syms
                          (*ns*
                            ns-name
                            all-ns
                            the-ns
                            find-ns
                            ns-publics
                            ns-interns
                            ns-refers
                            ns-aliases
                            ns-imports
                            ns-map),
                          :title "Inspect"}
                         {:syms (in-ns ns-resolve ns-unalias ns-unmap alias), :title "Use"}
                         {:syms (namespace-munge print-namespace-doc
                                  ), :title "Misc"})}
                       {:title "Hierarchies",
                        :groups
                        ({:syms
                          (make-hierarchy
                            derive
                            underive
                            parents
                            ancestors
                            descendants
                            isa?),
                          :title "General"})}
                       {:title "User Defined Types",
                        :groups
                        ({:syms
                          (defprotocol
                              defrecord
                            deftype
                            reify
                            extend
                            extend-protocol
                            extend-type
                            extenders),
                          :title "General"})}
                       {:title "Metadata",
                        :groups
                        ({:syms (meta with-meta vary-meta reset-meta! alter-meta!),
                          :title "General"})})}
                     {:title "Environment",
                      :categories
                      ({:title "Require / Import",
                        :groups
                        ({:syms (use require import refer-clojure refer),
                          :title "General"})}
                       {:title "Code",
                        :groups
                        ({:syms
                          (*compile-files*
                            *compile-path*
                            *file*
                            *warn-on-reflection*
                            compile
                            load
                            load-file
                            load-reader
                            load-string
                            read
                            read-string
                            gen-class
                            gen-interface
                            loaded-libs

                            test),
                          :title "General"})}
                       {:title "IO",
                        :groups
                        ({:syms
                          (*in*
                            *out*
                            *err*
                            print
                            printf
                            println
                            pr
                            prn
                            print-str
                            println-str
                            pr-str
                            prn-str
                            newline
                            flush
                            read-line
                            slurp
                            spit
                            with-in-str
                            with-out-str
                            with-open),
                          :title "General"})}
                       {:title "REPL",
                        :groups
                        ({:syms
                          (*1
                            *2
                            *3
                            *e
                            *print-dup*
                            *print-length*
                            *print-level*
                            *print-meta*
                            *print-readably*),
                          :title "General"})}
                       {:title "Misc",
                        :groups
                        ({:syms
                          (*clojure-version* clojure-version *command-line-args* time),
                          :title "General"})})}))
