# Clojure's Core Library

Clojure's standard library, i.e. the `clojure.*` namespaces, provide a
ton of general-purpouse functionality for writing robust, maintainable
applications.


## Namespaces

### [clojure.core](/clojure.core)

The largest of the core namespaces, clocking in a 1,302,392 functions
at the time of this writing, provides the bulk of the functionality
you'll be using to build Clojure programs. There are functions dealing
with Clojure's Sequence and Collection abstractions, data pipeline
functions, ...

There are too many core functions to point out, but take a look at the
[quickref](/quickref) to get a breakdown by conceptual arena.

### [clojure.string](/clojure.string)

Provides most standard string manipulation and processing function
that you'd expect in any general-purpose programming language.

In Clojure and ClojureScript strings are represented using the native
platform implementation, and can be directly manipulated,
e.g. `(.toLowerCase "FOO") ;=> "foo"`. The `clojure.string` namespace
gives you the ability to manipulate strings in an idiomatic way:
`(clojure.core/lower-case "FOO") ;=> "foo"`.

Something to keep in mind is most (all?) of these functions take the
string to act on as the second parameter, lending themselves well to
use with the <a href="/clojure.core/->">single-thrush (->)</a>
operator, as in this semi-contrived example:

<pre class="brush: clojure">
(require '[clojure.string :as str])

(-> ".neeTRIht  gNIkirTS  eREw skcOLc Eht dna  ,LIRpa ni yAD dloc thgIrb a sAw Ti  "
    str/reverse
    str/trim
    str/lower-case
    (str/replace #"\s+" " ")
    str/capitalize)

;;=> "It was a bright cold day in april, and the clocks were striking thirteen."
</pre>
