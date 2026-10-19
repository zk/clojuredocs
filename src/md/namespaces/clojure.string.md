Provides most standard string manipulation and processing function
that you'd expect in any general-purpose programming language.

In Clojure and ClojureScript strings are represented using the native
platform implementation, and can be directly manipulated,
e.g. `(.toLowerCase "FOO") ;=> "foo"`. The `clojure.string` namespace
gives you the ability to manipulate strings in an idiomatic way:
`(clojure.string/lower-case "FOO") ;=> "foo"`.

Something to keep in mind is most (all?) of these functions take the
string to act on as the first parameter, lending themselves well for
use with the <a href="/clojure.core/->">single-thrush operator (->)</a>
, as in this contrived example:

```clojure
(require '[clojure.string :as str])

(-> "  .LIRpa   ni  yAD    dloc thgIrb  a sAw Ti  "
    str/reverse
    str/trim
    str/lower-case
    (str/replace #"\s+" " ")
    str/capitalize)

;;=> "It was a bright cold day in april"
```
