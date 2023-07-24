# Destructuring in Clojure

In Clojure, destructuring is a shorthand for assigning names to parts
of data structures based on their forms. Don't worry if that's
confusing at first, it becomes very clear with a few examples.

Suppose we have a function that prints a greeting based on a user's
name, title, and location.

Here we'll manually pull out the name, title, and location from the
`user` parameter (a Map), and create bindings named `name`, `title`,
and `location` via [`let`](/clojure.core/let).

```
(defn greet [user]
  (let [name (:name user)
        location (:location user)]
    (println "Hey there" name ", how's the weather in" location "?")))

(greet {:name "Josie" :location "San Francisco"})
;; Hey there Josie, how's the weather in San Francisco?
;;=> nil

(greet {:name "Ivan" :location "Moscow"})
;; Hey there Ivan, how's the weather in Moscow?
;;=> nil

```

Destructuring lets us specify naming of the parameters directly from the
structure of the passed map:

```
(defn greet2 [{:keys [name location]}]
  (println "Hey there" name ", how's the weather in" location "?"))

(greet2 {:name "Josie" :location "San Francisco"})
;; Hey there Josie, how's the weather in San Francisco?
;;=> nil


```

* For more detailed take on destructuring see Daniel Gregoire's [blog post](https://danielgregoire.dev/posts/2021-06-13-code-observation-clojure-destructuring/).
* Bruno Bonacci's [guide on destructuring](https://blog.brunobonacci.com/2014/11/16/clojure-complete-guide-to-destructuring/) is worth a look.
* Jay Field's [introductory blog post](http://blog.jayfields.com/2010/07/clojure-destructuring.html) provides a good starting point.
* See John Del Rosario's [destructuring cheat sheet](https://gist.github.com/john2x/e1dca953548bfdfb9844) for a more comprehensive overview.
* Also there is [the official Clojure guide on destructuring](https://clojure.org/guides/destructuring).
