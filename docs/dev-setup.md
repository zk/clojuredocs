# Dev Environment Setup

Supplementary notes for getting a local development environment running. See also the [README](../README.md#dev) for the quick-start.

## Prerequisites

- Java (JDK 8+)
- [Leiningen](https://leiningen.org)
- MongoDB running locally

## MongoDB

Start MongoDB if it isn't already running:

```bash
mongod --dbpath ./dev-db
```

Check if it's running: `pgrep -f mongod`

### Seeding the Database

On first setup (or to reset), seed from the bundled production export:

```bash
bin/db-reset
```

This runs `mongorestore` against `data/mongodb/`. Verify with:

```bash
mongosh --eval "db.stats()" mongodb://localhost:27017/clojuredocs
```

## Environment Variables

The app requires several env vars (`MONGO_URL`, `SESSION_KEY`, `GH_CLIENT_ID`, `ALLOW_ROBOTS`, etc.). Dev defaults live in `bin/.devenv`. Source it before starting a REPL:

```bash
source bin/.devenv
```

Without these, the app will crash on startup. The most common symptom is:

```
No implementation of method: :named of protocol:
  #'somnium.congomongo/StringNamed found for class: nil
```

This means `MONGO_URL` is not set.

## Starting the REPL

`bin/dev` uses `foreman` to start everything (REPL, CLJS compiler, etc.). For a lighter backend-only REPL:

```bash
source bin/.devenv && lein repl :headless
```

The nREPL port is written to `.nrepl-port`.

The `:repl-options :init` in `project.clj` loads `reup.clj` on REPL start, which automatically starts the web server and sets up namespace reloading. If you need to start the server manually:

```clojure
(require 'clojuredocs.main)
(clojuredocs.main/start-app)
```

## Web Server

- Default port: **8080** (or whatever `PORT` env var is set to)
- `bin/dev` sets `PORT=4000`; `bin/.devenv` does not set `PORT`
- Visit http://localhost:8080 (or your configured port) to verify
