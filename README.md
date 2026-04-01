# clojuredocs

The clojuredocs.org webapp

![](http://cl.ly/image/1C2o2d181716/Screen%20Shot%202014-07-12%20at%202.03.25%20AM.png)


## Contributing

A few ways to contribute:

* Find & report bugs: https://github.com/nubank/clojuredocs/issues
* Suggestions on how to make development on the site more friendly
  (docs, codebase organization).

Let's use GH issues for discussion for now

If you're looking for a project:

* Content for namespaces (see `src/md/namespaces`) needs to be added /
  edited. Example of
  [clojure.core.async](http://next.clojuredocs.org/clojure.core.async)
* Listing of clojure training / classes / events on home page
* Stand-alone example page, maybe have the var info (signature, doc
  string, etc) at the top.
* Source-linking on libs not included in the standard library
  e.g. core.async.

## Deploy

Production runs on an AWS EC2 instance with nginx proxying to a single
systemd-managed JVM process.

```bash
# 1. SSH into the production box (requires ClojureDocs.pem — ask the team lead)
ssh clojuredocs

# 2. Deploy
~/deploy.sh
```

This pulls latest, builds (deps, compile, CLJS), and restarts the
service. See [docs/release-protocol.md](docs/release-protocol.md) for
the full release checklist, SSH setup, and smoke tests.


## Reqs

* Java (JDK 8+) (note prod build installs and runs `openjdk-17-jdk` )
* [JCE Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
* [lein](http://leiningen.org)
* [foreman](https://github.com/ddollar/foreman) (see `Procfile`, `bin/dev`)
* [entr](http://entrproject.org/) (available in homebrew)
* MongoDB



## Dev

> **Note:** The dev CLJS build (figwheel via `lein fig:build`) is
> currently broken. Use `bin/prod-local` to compile and run locally
> with the production CLJS build instead.

To start the server and connect a REPL:

1. Run `bin/prod-local` in one terminal (compiles CLJS, starts the web
   server on port 4000)
2. Connect to the running nREPL with `lein repl :connect` (nREPL port
   is printed at startup)

Visit http://localhost:4000 to verify. 

See `docs/dev-setup.md` and `docs/dev-setup-vscode.md` for more.


### Local Data

The app uses a MongoDB database named `clojuredocs` to store data. Run `bin/db-reset` to seed the database with a recent production export (you must be running `mongod` for this to work).

### Prod Local

Compile and run things as they would be in production (checking advanced cljs compilation, for example): `bin/prod-local`.


### CLJS Source Maps

The ClojureDocs project is set-up to emit source-maps for compiled javascript. To enable in Chrome, check the 'Enable JS source maps' option in the Developer Tools settings pane.


### Clojure Version

Clojure vars are pulled directly from the runtime, and are not stored in the database. When new versions of Clojure are released:

* Change the Clojure dep in `project.clj`
* Update the version string, source base url, and gh tag url in
  `clojuredocs.search/clojure-lib`
* Update the version for the mobile nav in `clojuredocs.pages.common`
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

## Data Exports

* [See also relations](https://clojuredocs.org/api/exports/see-alsos-relations) (ns/name -> [ns/name]), results cached for 1 minute.


## Contributors

[Zachary Kim](https://github.com/zk), [Lee Hinman](https://github.com/dakrone), and [more](https://github.com/zk/clojuredocs/graphs/contributors).


## License

Copyright © 2010-present Zachary Kim

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
