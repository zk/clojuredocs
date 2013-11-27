# clojuredocs

The clojuredocs.org webapp

## Dev

Run `bin/dev`

### App Structure

The CD webapp is structured around functionality, where each distinct part has it's own root namespace (`quickref`, `vars`, etc). This is different from a traditional *ails app setup, where source files are organized by type (controllers, views, models), instead of by function.

See `clj/clojuredocs/site`.

CD is still kind of an MVC app, in that we separate datastore access, transformation, and rendering into different parts. It's just that these parts are all colocated under a single root namespace. Think trees where major bits of functionality are sub-trees.

### Interesting Files

* `clj/clojuredocs/main.cljs` -- Root of the app, sets up the aleph server, repl, etc. Things that affect the runtime environment should go here. Not reloadable, don't put things that you're going to dev on here.
* `clj/clojuredocs/entry.clj` -- Entry point into the webapp. Site-wide middleware and base routes should go here.

### Conventions

* Functions that return hiccup structures should be prefixed with a `$`, like `$layout`.


## TODO

* Figure out how to get all the existing users hooked up to their GH accounts.


## License

Copyright © 2013 Zachary Kim

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
