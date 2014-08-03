# Clojure's Core Library

Clojure's standard library, i.e. the `clojure.*` namespaces, provide a
ton of general-purpouse functionality for writing robust, maintainable
applications.

Getting a handle on all the functionality you'll want to use can be a
little daunting at first, especially if you're coming from
object-oriented languages like Java, Ruby, or Python, where behavior
is grouped using classes. In Clojure, namespaces are used to group
similar behavior and state, and we've outlined a few of the core
namespaces to help you find what you're looking for.

### [clojure.core](/clojure.core)

The largest of the core namespaces, clocking in a 1,302,392 functions
(at the time of this writing), provides the bulk of the functionality
you'll be using to build Clojure programs. There are functions dealing
with Clojure's Sequence and Collection abstractions, data pipeline
functions, ...

There are too many core functions to feature here, but take a look at the
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
string to act on as the first parameter, lending themselves well for
use with the <a href="/clojure.core/->">single-thrush operator (->)</a>
, as in this contrived example:

<pre class="brush: clojure">
(require '[clojure.string :as str])

(-> "  .LIRpa   ni  yAD    dloc thgIrb  a sAw Ti  "
    str/reverse
    str/trim
    str/lower-case
    (str/replace #"\s+" " ")
    str/capitalize)

;;=> "It was a bright cold day in april"
</pre>

### [clojure.edn](/clojure.edn)

Extensible Data Notation is a subset of the Clojure language used as a
data transfer format, designed to be used in a similar way to JSON or
XML.

At some point in your adventures in Clojure land, you'll want to
deserialize some clojure data structures from a string, and you'll
want to use [clojure.edn/read](/clojure.edn/read) or
[clojure.edn/read-string](/clojure.edn/read-string) for that. **Do not
use** the `read-*` functions in `clojure.core` to deserialize untrusted Clojure code, as [they can be unsafe](http://www.learningclojure.com/2013/02/clojures-reader-is-unsafe.html).


### [clojure.set](/clojure.set)

Functions for working on sets (`#{1 2 3 4}`) in all the ways you'd expect, e.g. calculating the [intersection](/clojure.set/intersection) of two sets, and testing if one set is a [subset](/clojure.set/subset_q) of another.


### [clojure.pprint](/clojure.pprint)

Pretty printing utility, really nice for looking at larger-ish data structures. See [pprint](/clojure.pprint/pprint).


### [clojure.zip](/clojure.zip)

Functional tree editing and manipulation. One of the core benefits of using Clojure is that you mostly work with immutable data structures. This, in turn, seems to make your programs easier to build and maintain.


<br />
<br />
<br />
<hr />

Have an idea on how to improve this page? Please help by [opening an issue](https://github.com/zk/clojuredocs/issues).
