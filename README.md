# clojuredocs

The clojuredocs.org webapp

![](http://cl.ly/image/1C2o2d181716/Screen%20Shot%202014-07-12%20at%202.03.25%20AM.png)


## Rewrite In Progress

http://next.clojuredocs.org

[Rewrite Release Checklist](https://github.com/zk/clojuredocs/blob/clj-rewrite/.notes.zk.org#release-checklist)


## Reqs

* [lein](http://leiningen.org)
* [foreman](https://github.com/ddollar/foreman) (see `Procfile`, `bin/dev`)
* [less](http://lesscss.org/)
* MongoDB

## Deploy

* Run `bin/ship next | prod`


## Dev

Run `bin/dev`, which will start all the things (repl, web process,
scss compiler, etc). See `Procfile` for more info.

Connect to the repl and / or visit http://localhost:5000

You'll notice that var information is already populated. In an effort for not make the same mistakes again, all core-related var info is loaded from the runtime version of Clojure on start up.

OTOH, examples, see-alsos, and notes (and any other user-generated content) are stored in the database.

### Prod Local

Occasionally you'll need to compile and run things as they would be in production: `bin/prod-local`


### CLJS Source Maps

The ClojureDocs project is set-up to emit source-maps for compiled javascript. To enable in Chrome, check the 'Enable JS source maps' option in the Developer Tools settings pane.


### Clojure Version

Clojure vars are pulled directly from the runtime, and are not stored in the database. When new versions of Clojure are released:

* Change the Clojure dep in `project.clj`
* Update the version string and source base url in `clojuredocs.search/clojure-lib`


### App Structure

Interesting files:

* `src/clj/clojuredocs/main.clj` -- Main entry-point into the app, starts the jetty server
* `src/clj/clojuredocs/entry.clj` -- Root routes and middleware
* `src/clj/clojuredocs/pages.clj` -- User-facing HTML pages
* `src/clj/clojuredocs/api.clj` -- API endpoints for ajax calls from the frontend.
* `src/cljs/clojuredocs/main.cljs` -- Main js entry-point into the app


### Conventions

* Functions that return hiccup structures should be prefixed with a `$`, like `$layout`.


## Dev-Prod differences

* Dev starts the environment using `lein repl :headless`, prod uses `lein run -m clojuredocs.main`. See `:repl-options` in `project.clj` for initialization options.


## License

Copyright © 2010-present Zachary Kim

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
