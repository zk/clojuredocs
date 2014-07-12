# clojuredocs

The clojuredocs.org webapp

![](http://cl.ly/image/1C2o2d181716/Screen%20Shot%202014-07-12%20at%202.03.25%20AM.png)


## Reqs

* lein
* foreman (see `Procfile`, `bin/dev`)
* scss (`gem install scss`)
* MongoDB

## Deploy

* Force add resources/public/app.css, commit with message `deploy commit`
* Run `bin/ship`


## Dev

Run `bin/dev`, which will start all the things (repl, web process,
scss compiler, etc). See `Procfile` for more info.

Connect from cider (emacs) or Light Table, repl port 7888.

### Prod Local

Occasionally you'll need to compile and run things as they would be in production: `bin/prod-local`


### CLJS Source Maps

The ClojureDocs project is set-up to emit source-maps for compiled javascript. To enable in Chrome, check the 'Enable JS source maps' option in the Developer Tools settings pane.


### App Structure

The CD webapp is structured around functionality, where each distinct part has it's own root namespace (`quickref`, `vars`, etc). This is different from a traditional *ails app setup, where source files are organized by type (controllers, views, models), instead of by function.

See `clj/clojuredocs/site`.

CD is still kind of an MVC app, in that we separate datastore access, transformation, and rendering into different parts. It's just that these parts are all colocated under a single root namespace. Think trees where major bits of functionality are sub-trees.


### Interesting Files

* `clj/clojuredocs/main.cljs` -- Root of the app, sets up the aleph server, repl, etc. Things that affect the runtime environment should go here. Not reloadable, don't put things that you're going to dev on here.
* `clj/clojuredocs/entry.clj` -- Entry point into the webapp. Site-wide middleware and base routes should go here.


### Conventions

* Functions that return hiccup structures should be prefixed with a `$`, like `$layout`.


## Dev-Prod differences

* Dev starts the environment using `lein repl :headless`, prod uses `lein run -m clojuredocs.main`. See `:repl-options` in `project.clj` for initialization options.


## TODO

* Figure out how to get all the existing users hooked up to their GH accounts.
* 301 old var urls to new
* Tag / show which runtimes a var is available in (clj / cljs)


## License

Copyright © 2013 Zachary Kim

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
