Extensible Data Notation is a subset of the Clojure language used as a
data transfer format, designed to be used in a similar way to JSON or
XML.

At some point in your adventures in Clojure land, you'll want to
deserialize some clojure data structures from a string, and you'll
want to use [clojure.edn/read](/clojure.edn/read) or
[clojure.edn/read-string](/clojure.edn/read-string) for that. **Do not
use** the `read-*` functions in `clojure.core` to deserialize untrusted Clojure code, as [they can be unsafe](http://www.learningclojure.com/2013/02/clojures-reader-is-unsafe.html).

##### Community Links

* [Official EDN Spec](http://edn-format.org)
* [Official documentation for clojure.edn](https://clojure.github.io/clojure/clojure.edn-api.html)
* [EDN walkthrough by Mark Mandel](http://www.compoundtheory.com/clojure-edn-walkthrough)
