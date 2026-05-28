# Tools

Scripts for data generation and maintenance. Run manually from a REPL.

## `gen_dialect_compat.clj`

Generates `resources/dialect-compat.edn` — the static compatibility index mapping 700 vars to their supported dialects (Clojure/JVM, ClojureScript, babashka).

### Prerequisites

- **Leiningen** — for the project REPL (provides CLJS analyzer dependency)
- **babashka** (`bb`) — installed and on `$PATH` (the script shells out to `bb -e` to query bb's `ns-publics`)

### How to run

```sh
lein repl
```

```clojure
(load-file "tools/gen_dialect_compat.clj")
(tools.gen-dialect-compat/generate!)
```

Output is written to `resources/dialect-compat.edn`. Commit the generated file.

### When to regenerate

Regenerate when any of these change:

- Clojure version (currently 1.12.4)
- ClojureScript version (currently 1.12.134, declared in `project.clj`)
- babashka version (currently 1.12.215, from `bb --version`)

The generated file includes a `:versions` header for traceability.

## Other scripts

| Script | Purpose |
|--------|---------|
| `dev_export.clj` | Export development data |
| `old_import.clj` | Legacy data import |
| `sanity_check.clj` | Data sanity checks |
| `top_contribs.clj` | Top contributors report |
