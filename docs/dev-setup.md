---
type: Guide
title: Dev Environment Setup
description: Notes for getting a local development environment running.
tags: [dev-setup, mongodb, leiningen]
created: 2026-02-27
modified: 2026-07-01
review_maturity: L2
review_note: Human-authored setup notes; frontmatter added during OKF migration.
---

# Dev Environment Setup

Supplementary notes for getting a local development environment running. See also the [README](../README.md#dev) for the quick-start.

## Prerequisites

- Java (JDK 8+)
- [Leiningen](https://leiningen.org)
- MongoDB running locally
- [babashka](https://babashka.org) (`bb`) — for repo tooling and the doc-metadata pre-commit hook

## Git hooks

Enable the repo's pre-commit hook so document metadata is validated before each commit:

```bash
bin/install-hooks
```

This points `core.hooksPath` at `.githooks`; the hook runs `bb tools/validate_metadata.clj` — the same check CI runs. Validate manually any time with:

```bash
bb tools/validate_metadata.clj
```

See [CLAUDE.md](../CLAUDE.md) for the metadata convention and the [OKF metadata RFC](rfcs/okf-metadata-rfc.md) for rationale.

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

Start MongoDB, a backend REPL, and the CLJS build in separate terminals:

```bash
# terminal 1 — MongoDB (skip if already running)
mongod --dbpath ./dev-db

# terminal 2 — backend REPL
source bin/.devenv
lein repl

# terminal 3 — ClojureScript build (only needed if you're touching src/cljs)
source bin/.devenv
lein fig:build
```

The nREPL port is written to `.nrepl-port`.

The `:repl-options :init` in `project.clj` loads `reup.clj` on REPL start, which automatically starts the web server and sets up namespace reloading. If you need to start the server manually:

```clojure
(require 'clojuredocs.main)
(clojuredocs.main/start-app)
```

## Web Server

- Default port: **8080** (or whatever `PORT` env var is set to)
- `bin/.devenv` pins `PORT=4000` and `BASE_URL=http://localhost:4000`
- Visit http://localhost:4000 (or your configured port) to verify

> **GitHub login:** `BASE_URL` builds the OAuth `redirect_uri`, so it must match the port the server actually listens on. If they diverge (e.g. `BASE_URL` on `:5000` but the server on `:4000`), GitHub rejects the callback and login fails — see [issue #9](https://github.com/nubank/clojuredocs/issues/9).
