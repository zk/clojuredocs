# ClojureDocs Example Submission Guidelines

The example sections on each var page are there to provide simple, isolated examples of var usage.  In a nutshell, the examples you add to Clojuredocs should be easy to understand, and to help you with that we've outlined a few guidelines below.

## General Guidelines

Examples should be short, unique, self-contained snippets of code that illustrate var usage in the simplest possible way.

* Try to imagine clear conceptual boundries of your example before submitting it.
* Assume the reader has a background in software development, with little Clojure experience.
* Short, sweet, and complete is the name of the game.

If the target var is not part of the core ns (or otherwise not <code>use</code>d by default) please include the <code>use</code> / <code>require</code> statement.

Bad:

<pre class="brush: clojure">

(sh "ls" "-aul")

;; {:exit 0,
;;  :out "total 64
;; drwxr-xr-x  11 zkim  staff    374 Jul  5 13:21 ."
;; ...
</pre>

Good:

<pre class="brush: clojure">
(use '[clojure.java.shell :only [sh]])

(sh "ls" "-aul")

;; {:exit 0,
;;  :out "total 64
;; drwxr-xr-x  11 zkim  staff    374 Jul  5 13:21 ."
...
</code></pre>

Each example should be either broad, or deep, not both.  For example, the following example for <code>not=</code> shows the broad range of inputs allowed.

<pre class="brush: clojure">
(not= 1 1) ;;=> false

(not= 1 2) ;;=> true

(not= true true) ;;=> false

(not= true false) ;;=> true

(not= true true true true) ;;=> false

(not= true true false true) ;;=> true
</pre>

Where this example for <code>future</code> has depth.

<pre class="brush: clojure">
;; A future is calculated in another thread
(def f (future (Thread/sleep 10000) 100))
;;=> #'user/f

;; When you dereference it you will block until the result is available.
@f
;;=> 100

;; Dereferencing again will return the already calculated value immediately.
@f
;;=> 100
</pre>

Also, please mention any gotchas you feel are associated with your example (specifically) or the var (in general).

## Comments

Comments should be used to describe the following code block or blocks and/or point out bits of information that may not be obvious to new Clojure devs.

Bad:

<pre class="brush: clojure">
(with-precision 10 (/ 1M 3))
;;=> 0.3333333333M

user=&gt; (.floatValue 0.3333333333M)
;;=> 0.33333334
</pre>

Good:

<pre class="brush: clojure">
;; The "M" suffix denotes a BigDecimal instance
;; http://download.oracle.com/javase/6/docs/api/java/math/BigDecimal.html

(with-precision 10 (/ 1M 3))
;;=> 0.3333333333M

(.floatValue 0.3333333333M)
;;=> 0.33333334
</pre>

<code>;;</code> should be for a general comment about a block of code, <code>;</code> should be used to add a comment to the end of a line of code.

Bad:

<pre class="brush: clojure">
; This function will print 'hello world' to the console
(defn hello-world []
  (println "hello world"))  ;; Does the actual printing
</pre>

Good:

<pre class="brush: clojure">
;; This function will print 'hello world' to the console
(defn hello-world []
  (println "hello world"))  ; Does the actual printing
</pre>

Differentiating code from context in your example will help

Using comments to differentiate code from context and output will help new Clojure devs match up the executable parts of your example in their REPLs.

Good:

<pre class="brush: clojure">
;; You can use destructuring to have keyword arguments. This would be a pretty
;; verbose version of map (in an example a bit more verbose than the first above):

(defn keyworded-map [&amp; {function :function sequence :sequence}]
  (map function seq))


;; You call it like this:

(keyworded-map :sequence [1 2 3] :function #(+ % 2))
;;=> (3 4 5)


;; The declaration can be shortened with ":keys" if your local variables should be
;; named in the same way as your keys in the map:

(defn keyworded-map [&amp; {:keys [function sequence]}]
  (map function sequence))
</pre>

Feel free to omit comments for very simple examples.

## Indentation / Formatting

Please follow the conventions outlined in this [Scheme style guide](http://mumble.net/~campbell/scheme/style.txt), which follows Emacs' (among others) indentation and formatting conventions.  We realize that code style can often be largely dictated by personal preference, however, uniformity across examples on ClojureDocs is important.

Lines should have a maximum width of 80 characters to prevent wrapping when displayed on ClojureDocs pages, and please indent with spaces, not tabs.

Consider leaving one line of whitespace after output from the repl to make your examples easier to visually scan.

Bad:

<pre class="brush: clojure">
(println "foo")
;; foo
;;=> nil
(println "bar")
;; bar
;;=> nil
(println "baz")
;; baz
;;=> nil
</pre>


Good:

<pre class="brush: clojure">
(println "foo")
;; foo
;;=> nil

(println "bar")
;; bar
;;=> nil

(println "baz")
;; baz
;;=> nil
</pre>


## Linking

Urls in examples source are automatically converted to links.  Feel free to use them where appropriate to link to external resources.
