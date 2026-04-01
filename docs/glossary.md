# Glossary

> **AI Disclaimer**: This was researched and drafted using Claude Code powered by Claude Opus 4.6. AI-generated research contains false statements, unsupported assumptions, and missing context. Do not treat any claim as verified unless you have confirmed it against a primary source.

Terms as used in [issue #4](https://github.com/nubank/clojuredocs/issues/4) analysis and related design documents. Definitions are scoped to this codebase — some terms have broader meanings elsewhere.

---

## $

**$library-nav** — A Hiccup-generating function that renders the left sidebar namespace tree for a given [library](#l). Accepts a library map and an optional current-namespace string (used to highlight the active page). Delegates to [$namespaces](#dollar-sign) and [$ns-tree](#dollar-sign) to build a nested `<ul>` from dot-separated namespace names. Called on every namespace page, var page, and core-library page — always with the single [clojure-lib](#c).

*Source*: [`pages/common.clj` lines 266–271](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/common.clj#L266-L271).

**$ns-tree** — A recursive Hiccup-generating function that renders one node of the namespace tree in the left sidebar. Each node has a `:part` (the dot-separated segment, e.g. `"core"`), a `:path` (the accumulated full namespace name), and child nodes `:cs`. Namespace segments that match an actual [namespace](#n) become links; non-namespace segments render as plain text.

*Source*: [`pages/common.clj` lines 252–259](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/common.clj#L252-L259).

**$var-link** — A Hiccup-generating function that produces an `[:a {:href ...} ...]` element linking to a [var](#v) page. Delegates to [var-path](#v) for URL computation. Used throughout server-side rendering (namespace pages, search results, see-alsos, homepage activity feed) and in the ClojureScript client (see-also widget). Every var link on the site passes through this function.

*Source*: [`util.cljc` lines 80–84](https://github.com/nubank/clojuredocs/blob/master/src/cljc/clojuredocs/util.cljc#L80-L84).

## C

**cd-encode / cd-decode** — A paired set of functions that encode and decode [var](#v) names for use in URL path segments. Replaces characters that conflict with URL syntax: `/` becomes `_fs`, `\` becomes `_bs`, `?` becomes `_q`, `.` becomes `_.`. The encode function is `.cljc` (shared server + client); the decode function is server-only (`.clj`). Used by [var-path](#v), [gather-vars](#g), route handlers, and redirect logic.

*Source*: [`util.cljc` lines 53–75](https://github.com/nubank/clojuredocs/blob/master/src/cljc/clojuredocs/util.cljc#L53-L75).

**clojure-lib** — The single `def` in `search.clj` that holds the entire library data structure at runtime. Built at JVM startup by threading a map (with [library-url](#l), version, and namespace list) through [gather-namespaces](#g) and [gather-vars](#g). Every page handler, sidebar renderer, and search index reads from this one value. The central point of single-library hardcoding.

*Source*: [`search.clj` lines 102–108](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L102-L108).

**clucy** — A Clojure library that wraps Apache Lucene, providing functions to create in-memory search indexes and add/query documents. ClojureDocs uses clucy to build the [Lucene index](#l) at startup. Key functions used: `clucy/memory-index`, `clucy/add`, `clucy/search`.

*Source*: [clucy on GitHub](https://github.com/weavejester/clucy) (v0.4.0); [`project.clj` line 14](https://github.com/nubank/clojuredocs/blob/master/project.clj#L14); [`search.clj` line 2](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L2).

**Compojure** — A Clojure routing library that maps HTTP method + URL pattern pairs to handler functions. ClojureDocs uses `defroutes` to define route tables in three places: page routes ([pages.clj](#r)), API routes ([api/server.clj](#r)), and legacy redirect routes ([entry.clj](#r)). Routes are matched top-to-bottom; the first match wins. The catch-all routes `GET "/:ns"` and `GET "/:ns/:name"` in `pages.clj` are Compojure patterns — their two-segment structure is where the absence of a library URL segment is enforced.

*Source*: [Compojure on GitHub](https://github.com/weavejester/compojure) (v1.7.0); [`project.clj` line 12](https://github.com/nubank/clojuredocs/blob/master/project.clj#L12).

**congomongo** — A Clojure library that wraps the MongoDB Java driver, providing functions like `mon/fetch`, `mon/fetch-one`, `mon/insert!`, and `mon/update!`. Aliased as `mon` throughout the codebase. All user-contributed content (examples, notes, see-alsos) and user accounts are stored in MongoDB and accessed through congomongo. MongoDB queries already filter by [library-url](#l), making the persistence layer partially multi-library ready.

*Source*: [congomongo on GitHub](https://github.com/congomongo/congomongo) (v2.6.0); [`project.clj` line 40](https://github.com/nubank/clojuredocs/blob/master/project.clj#L40).

## G

**gather-namespaces** — A function that accepts a library map containing a list of namespace symbols, `require`s each namespace at JVM startup, extracts its metadata (`:doc`, `:no-doc`, `:added`), and returns the library map with `:namespaces` populated as a vector of maps. Namespaces marked `:no-doc` are excluded. Currently called once, on the single [clojure-lib](#c) definition.

*Source*: [`search.clj` lines 95–100](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L95-L100).

**gather-vars** — A function that accepts a library map (after [gather-namespaces](#g) has run), calls `ns-publics` on each namespace to collect var metadata from the live JVM, transforms that metadata, and stamps every var with the library's single [library-url](#l) and a two-segment [href](#v). Special forms are concatenated into the var list here. Returns the library map with `:vars` populated.

*Source*: [`search.clj` lines 74–85](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L74-L85).

## L

**library** — A collection of [namespaces](#n) distributed as a single artifact (Maven JAR). Each library has a [library-url](#l), version, and source base URL. The codebase currently defines one library ([clojure-lib](#c)) that bundles namespaces from six distinct Maven artifacts.

*Source*: [`search.clj` lines 102–108](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L102-L108). The six artifacts are `org.clojure/clojure`, `org.clojure/core.async`, `org.clojure/core.logic`, `org.clojure/data.csv`, `org.clojure/spec.alpha`, and `org.clojure/tools.build` — identified by cross-referencing namespace prefixes in [`search/static.clj`](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search/static.clj#L3-L39) against Maven Central.

**library-for** — A function that accepts a namespace string or var map and returns the [library](#l) that owns it. Two copies exist (one in `pages.vars`, one in `pages.nss`), both of which ignore their argument and return the hardcoded [clojure-lib](#c).

*Source*: [`pages/vars.clj` lines 20–21](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/vars.clj#L20-L21); [`pages/nss.clj` lines 9–10](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/nss.clj#L9-L10).

**library-url** — A GitHub repository URL string (e.g. `"https://github.com/clojure/clojure"`) that serves as the identity key for a [library](#l) in MongoDB documents. MongoDB queries for examples, notes, and see-alsos filter on `library-url` to scope user-contributed content to a specific library. Currently, all [vars](#v) receive the same `library-url` regardless of which Maven artifact they actually originate from.

*Source*: [`data.clj` lines 6–31](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/data.clj#L6-L31) (MongoDB queries); [`search.clj` line 103](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L103) (hardcoded value); [`api/common.clj` lines 13–15](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/api/common.clj#L13-L15) (schema).

**lookup-vars** — A hash map built at startup that enables O(1) [var](#v) resolution by key. The key is a string of the form `"ns/name"` (e.g. `"clojure.core/map"`). Because the key includes no [library](#l) dimension, two vars with the same fully-qualified name from different libraries would overwrite each other. Currently populated solely from [clojure-lib](#c) vars.

*Source*: [`search.clj` lines 138–140](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L138-L140).

**Lucene index** — An in-memory Apache Lucene full-text search index (created via [clucy](#c)'s `memory-index`) that powers the autocomplete and search features. Populated at JVM startup with [vars](#v), [searchable namespaces](#s), and static pages. No library field is indexed, so search results carry no library provenance.

*Source*: [`search.clj` lines 8, 131–136](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L8).

## N

**namespace** — A named container for [vars](#v) within a [library](#l). On ClojureDocs, a namespace maps to both a Clojure namespace (loaded at JVM startup) and a browsable page at `/:ns`. Namespaces are listed in the left sidebar tree. The naming convention (e.g. `clojure.core.async`) can be misleading because `clojure.core.async` is *not* part of `clojure.core` — it belongs to a separate [library](#l).

*Source*: [`search/static.clj` lines 3–39](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search/static.clj#L3-L39) (the flat list); [Clojure reference on namespaces](https://clojure.org/reference/namespaces).

## R

**routes** — [Compojure](#c) route definitions that map URL patterns to handler functions. Three route tables exist: page routes in [`pages.clj`](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages.clj#L311-L358) (namespace pages, var pages, search, core-library, quickref), API routes in [`api/server.clj`](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/api/server.clj#L34-L51) (CRUD for examples, notes, see-alsos), and legacy redirect routes in [`entry.clj`](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/entry.clj#L86-L100) (old ClojureDocs URL patterns redirected to current URLs). Composed into a single handler in `entry.clj`.

*Source*: [`entry.clj` lines 100–104](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/entry.clj#L100-L104).

## S

**searchable namespace** — A map derived from the [static namespace list](#n) with `:name`, `:keywords` (tokenized for Lucene), `:type "namespace"`, and `:href`. The href is computed as `(str "/" name)` with no library prefix. Added to the [Lucene index](#l) at startup.

*Source*: [`search.clj` lines 117–128](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L117-L128).

**source-url** — A function that generates a GitHub permalink to a [var](#v)'s source code. Currently hardcoded: returns `nil` for any var not in `clojure.core`, and uses a hardcoded tag (`clojure-1.12.4`) for those that are. The [library](#l) map's `:source-base-url` field exists but this function does not use it.

*Source*: [`pages/vars.clj` lines 46–48](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/vars.clj#L46-L48).

## V

**var** — A documented function, macro, special form, or value entry on ClojureDocs. At runtime, a var is a Clojure map containing `:ns`, `:name`, `:doc`, `:arglists`, `:library-url`, `:type`, `:href`, and other metadata. The `:href` is a two-segment path (`/:ns/:name`) baked in at startup by [gather-vars](#g). Each var is the unit of user contribution: users add examples, notes, and see-alsos scoped to a specific var.

*Source*: [`search.clj` lines 19–83](https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj#L19-L83) (var-keys, transform-var-meta, gather-vars).

**var-path** — A `.cljc` function (shared between server and ClojureScript client) that computes the URL path for a [var](#v) page. Returns `(str "/" ns "/" (cd-encode name))` — a two-segment path with no [library](#l) component. Used by [$var-link](#dollar-sign) to generate every var hyperlink on the site.

*Source*: [`util.cljc` lines 77–78](https://github.com/nubank/clojuredocs/blob/master/src/cljc/clojuredocs/util.cljc#L77-L78).

---

## Errata

*None yet.*

---

## Version History

| Date | Changes |
|------|---------|
| 2026-03-04 | Initial version with 13 terms for issue #4 analysis |
| 2026-03-04 | Added 8 terms: $library-nav, $ns-tree, $var-link, cd-encode/cd-decode, clucy, Compojure, congomongo, routes, source-url. Total: 21 terms |
