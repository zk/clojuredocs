# clojuredocs

The clojuredocs.org webapp

![](http://cl.ly/image/1C2o2d181716/Screen%20Shot%202014-07-12%20at%202.03.25%20AM.png)


## Contributing

The codebase needs a good scrub before groking will not be
painful. However, feel free to jump in if you don't mind digging
around.

Here's a few ways to contribute:

* Find & report bugs: https://github.com/zk/clojuredocs/issues
* Suggestions on how to make development on the site more friendly
  (docs, codebase organization).

Let's use GH issues for discussion for now

If you're looking for a project:

* All the copy could use another set of eyes
* Content for namespaces (see `src/md/namespaces`) needs to be added /
  edited. Example of
  [clojure.core.async](http://next.clojuredocs.org/clojure.core.async)
* Search quick lookup requires a round-trip to the server, which is
  way too slow. It would be nice to experiment with embedding vars and
  doing search directly in the js
* Listing of clojure training / classes / events on home page
* Stand-alone example page, maybe have the var info (signature, doc
  string, etc) at the top.
* Source-linking on libs not included in the standard library
  e.g. core.async.

## Deploy

Production is deployed on an AWS t2.micro instance. There's an nginx
process running on the box, balancing to two JVMs managed by Upstart
to support zero-downtime deploys.

To regenerate the upstart scripts:

```
cd $REPO
sudo foreman export -a clojuredocs -e ./.env -u ubuntu -c "web=2" upstart /etc/init/
```

To start the app processes:

```
sudo service clojuredocs-web-1 start
sudo service clojuredocs-web-2 start
```

To redeploy:

```
# in $REPO
sudo service clojuredocs-web-1 stop
git pull origin master
# This will compile assets & run tests
bin/build
sudo service clojuredocs-web-1 start
# Wait for proc 1 to start serving requests
sudo service clojuredocs-web-2 restart
```


## Reqs

* [> JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [JCE Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
* [lein](http://leiningen.org)
* [foreman](https://github.com/ddollar/foreman) (see `Procfile`, `bin/dev`)
* [less](http://lesscss.org/#using-less-installation)
* [entr](http://entrproject.org/) (available in homebrew)
* MongoDB



## Dev

Run `bin/dev`, which will start all the things (repl, web process,
scss compiler, etc). See `Procfile` for more info.

Connect to the repl and / or visit http://localhost:5000

You'll notice that var information is already populated. In an effort for not make the same mistakes again, all core-related var info is loaded from the runtime version of Clojure on start up.

OTOH, examples, see-alsos, and notes (and any other user-generated content) are stored in the database.


### Local Data

The app uses a MongoDB database named `clojuredocs` to store data. Run `bin/db-reset` to seed the database with a recent production export (you must be running `mongod` for this to work).

### Prod Local

Occasionally you'll need to compile and run things as they would be in production: `bin/prod-local`


### CLJS Source Maps

The ClojureDocs project is set-up to emit source-maps for compiled javascript. To enable in Chrome, check the 'Enable JS source maps' option in the Developer Tools settings pane.


### Clojure Version

Clojure vars are pulled directly from the runtime, and are not stored in the database. When new versions of Clojure are released:

* Change the Clojure dep in `project.clj`
* Update the version string and source base url in `clojuredocs.search/clojure-lib`
* Update the github URL in `clojuredocs.pages.vars/source-url`.


### App Structure

Interesting files:

* `src/clj/clojuredocs/main.clj` -- Main entry-point into the app, starts the jetty server
* `src/clj/clojuredocs/entry.clj` -- Root routes and middleware
* `src/clj/clojuredocs/pages.clj` -- User-facing HTML pages
* `src/clj/clojuredocs/api.clj` -- API endpoints for ajax calls from the frontend.
* `src/cljs/clojuredocs/main.cljs` -- Main js entry-point into the app


### Conventions

* Functions that return hiccup structures should be prefixed with a `$`, like `$layout`.
* Mutable state should be prefixed with a `!`, ex: `!my-atom`.


### Adding Functions, Macros, Special Forms, Namespaces & Other Vars

Most vars are picked up from Clojure at runtime, using core namespace
and var introspection utilities. Special forms and namespaces to
include on the site are specified explicitly in the
`clojuredocs.search.static` namespace.

Vars are picked up automatically based on the namespaces specified in
`clojuredocs.search.static/clojure-namespaces` vector. Any namespace
in this vector will be queried for public vars to be made searchable
and displayable on the site.

Special forms are specified in the
`clojuredocs.search.static/special-forms` list, and require a server
restart to be picked up in a dev environment.


### Adding Core Libraries

Sometimes we'd like to add core libraries that are not part of the
standard Clojure distribution (like core.async) to the site. Here's
how to do that:

1. Add dependency to `project.clj`
1. Add ns sym to `clojure-namespaces` in `clojuredocs.search.static`
1. Add a short description + links to community articles / videos /
   other resources to `src/md/namespaces/`


## Dev-Prod differences

* Dev starts the environment using `lein repl :headless`, prod uses `lein run -m clojuredocs.main`. See `:repl-options` in `project.clj` for initialization options.


## Contributors

[Zachary Kim](https://github.com/zk), [Lee Hinman](https://github.com/dakrone), and [more](https://github.com/zk/clojuredocs/graphs/contributors).


## License

Copyright © 2010-present Zachary Kim

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
