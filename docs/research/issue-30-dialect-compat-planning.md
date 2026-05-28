# Planning: Cross-Dialect Compatibility Indicators

> **Issue:** [nubank/clojuredocs#30](https://github.com/nubank/clojuredocs/issues/30)
> **Branch:** `research/30/dialect-compat-planning`
> **Author:** L. Jordan Miller
> **Reviewers:** David Nolen (ClojureScript), Michiel Borkent (babashka)
> **Date started:** 2026-04-14 ([`02c9802`](https://github.com/nubank/clojuredocs/commit/02c980200923afdc502176eba6aea62ce2fe92f3))
> **Last updated:** 2026-04-28

---

## How to use this document

This is a planning artifact for [Issue #30: Cross-Dialect Compatibility Indicators](https://github.com/nubank/clojuredocs/issues/30). It follows the Reflective Inquiry framework from Rich Hickey's ["Design in Practice"](https://www.youtube.com/watch?v=c5QF2HjHLSE) talk and Alex Miller's ["Design in Practice in Practice"](https://www.youtube.com/watch?v=VBnGhQOyTM4) follow-up.

The structure uses Father Watson's four questions applied to two axes — Understanding and Activity — forming a 2×2 matrix that can be revisited at any point during the work:

|               | Understanding (why)          | Activity (what)          |
|---------------|------------------------------|--------------------------|
| **Status**    | What do we know?             | Where are we at?         |
| **Agenda**    | What do we need to know?     | Where are we going?      |

Fill this in iteratively. Come back and update sections as understanding grows. Sections that say "unknown" are the most valuable — they tell you where to direct effort next.

---

## Glossary

<!-- Precision in naming yields precision in thinking. Define terms on first use.
     Cross-reference the project glossary at docs/glossary.md where terms overlap. -->

| Term | Definition | Source |
|------|-----------|--------|
| **Dialect** | A host-targeted implementation of the Clojure language. The three dialects in scope for this issue are Clojure/JVM, ClojureScript, and babashka. | [2026 Vision](../2026vison.md) ("Cross-dialect hub" strategic bet); informal Clojure community usage. |
| **Var** | A documented function, macro, special form, or value entry on ClojureDocs, identified by namespace and name (e.g., `clojure.core/map`). Each var has a dedicated page. | [Project glossary](../glossary.md); [`search.clj`](../../src/clj/clojuredocs/search.clj). |
| **Compatibility indicator** | A badge on a var page that marks whether a given dialect supports that var. Three states: supported, not supported, unknown. | [Feature proposal #4](../feature-proposals-q2-2026.md) (Feature 4: Cross-Dialect Compatibility Indicators). |
| **EDN** | A data serialization format based on a subset of Clojure literal syntax, used for configuration and data exchange. Used here for the static compatibility data file. | [edn-format/edn](https://github.com/edn-format/edn). |
| **Namespace** | A named container for vars, identified by a dotted symbol (e.g., `clojure.core`). On ClojureDocs, a namespace corresponds to a browsable page at `/:ns`. | [Project glossary](../glossary.md); [`search/static.clj`](../../src/clj/clojuredocs/search/static.clj). |
| **Static compatibility index** | A map from qualified var name to the set of dialects that support it. Loaded at startup, not stored in MongoDB. | This document. |
| **Var page** | A page on ClojureDocs dedicated to a single var, displaying its docstring, examples, see-alsos, and notes. Located at `/:ns/:name`. | [`pages/vars.clj`](../../src/clj/clojuredocs/pages/vars.clj). |
| <!-- term --> | <!-- definition --> | <!-- source --> |

---

## Part 1: Per-Dialect Research (Father Watson × 3)

For each dialect, answer the four questions to build understanding before planning implementation.

---

### 1.1 ClojureScript

#### What do we know? (Status — Understanding)

- ClojureScript compiler version: **1.12.134** (from [cljs.github.io/api](https://cljs.github.io/api/), checked 2026-04-21).
- `cljs.core` analyzed via `cljs.analyzer.api/analyze-file` on the compiler source. Results:
  - **1090 total vars** in `cljs.core` (980 defs + 191 macros, 81 overlap).
  - **474 of 679 JVM `clojure.core` vars present** in `cljs.core` (70% coverage).
  - **205 JVM vars missing** from `cljs.core` — see categorized table below.
  - **616 CLJS-only vars** (JS interop, protocols, types, internal helpers) — not relevant for compatibility indicators since they have no JVM counterpart.
- ClojureScript uses `cljs.core` internally but auto-aliases `clojure.core` → `cljs.core`. Code written as `(require '[clojure.core])` works in CLJS.
- `clojure.string` in CLJS: **20 vars** — identical to JVM *except* `re-quote-replacement` is missing. Verified at [cljs.github.io/api/clojure.string](https://cljs.github.io/api/clojure.string) on 2026-04-21.
- Known data sources:
  - [x] [ClojureScript cheatsheet](https://cljs.info/cheatsheet/) — last verified: 2026-04-21. Useful for quick reference but not machine-readable.
  - [x] ClojureScript compiler source (`cljs.core` namespace) — [cljs/core.cljs](https://github.com/clojure/clojurescript/blob/r1.12.134/src/main/cljs/cljs/core.cljs). Analyzed programmatically via `cljs.analyzer.api` on 2026-04-21.
  - [x] ClojureScript API docs — [cljs.github.io/api/cljs.core](https://cljs.github.io/api/cljs.core/). Consulted 2026-04-21. Listing of public defs (functions, protocols, types). Does not include macros — see methodology note below.
- Authoritative data source: The ClojureScript compiler itself via `cljs.analyzer.api`. Added `org.clojure/clojurescript 1.12.134` as a dependency and ran `(ana-api/analyze-file "cljs/core.cljs")` to extract both `:defs` (functions, protocols, types) and `:macros` from the analyzer state. This is more complete than the API docs alone — it reflects what the compiler actually provides, including macros defined in `.cljc` files.

  > **Methodology note:** An earlier pass consulted only the [ClojureScript API docs](https://cljs.github.io/api/cljs.core/) and found 980 vars in `cljs.core`. This undercounted by ~10% because the API docs page for `cljs.core` lists `:defs` only — functions, protocols, and types defined in `cljs/core.cljs`. It does not include the 191 macros that `cljs.core` imports from its companion file `cljs/core.clj` (a Clojure-hosted file that defines macros like `defn`, `fn`, `let`, `when`, `->`, `cond`, `for`, `doseq`, etc.). The CLJS compiler's analyzer state stores these under the `:macros` key of the namespace, separate from `:defs`. By querying both keys and taking their union, we get the complete picture: 1090 unique vars. The 81-var overlap between `:defs` and `:macros` represents vars that have both a runtime function and a compile-time macro form (e.g., `+`, `*`, `-` have macro versions for inlining). This distinction matters for correctness — consulting only the `.cljs` source or only the API docs page would miss core macros and produce a misleading gap count.

  <details><summary><strong>205 JVM `clojure.core` vars missing from `cljs.core`</strong> (click to expand)</summary>

  | Category | Vars | Count |
  |----------|------|-------|
  | Agents | `*agent*`, `agent`, `agent-error`, `agent-errors`, `await`, `await-for`, `await1`, `clear-agent-errors`, `error-handler`, `error-mode`, `release-pending-sends`, `restart-agent`, `send`, `send-off`, `send-via`, `set-agent-send-executor!`, `set-agent-send-off-executor!`, `set-error-handler!`, `set-error-mode!`, `shutdown-agents` | 20 |
  | Refs / STM | `alter`, `commute`, `dosync`, `ensure`, `ref`, `ref-history-count`, `ref-max-history`, `ref-min-history`, `ref-set`, `sync` | 10 |
  | Futures / promises | `deliver`, `future`, `future-call`, `future-cancel`, `future-cancelled?`, `future-done?`, `future?`, `promise`, `seque` | 9 |
  | Thread bindings | `bound-fn`, `bound-fn*`, `bound?`, `get-thread-bindings`, `pop-thread-bindings`, `push-thread-bindings`, `thread-bound?`, `with-bindings`, `with-bindings*`, `with-local-vars` | 10 |
  | Proxy infrastructure | `construct-proxy`, `gen-class`, `gen-interface`, `get-proxy-class`, `init-proxy`, `proxy`, `proxy-call-with-super`, `proxy-mappings`, `proxy-name`, `proxy-super`, `update-proxy` | 11 |
  | Struct-maps (deprecated) | `accessor`, `create-struct`, `defstruct`, `struct`, `struct-map` | 5 |
  | Java I/O | `file-seq`, `line-seq`, `read`, `read+string`, `read-line`, `read-string`, `slurp`, `spit`, `with-in-str`, `with-open`, `xml-seq` | 11 |
  | Java types / interop | `bean`, `bigdec`, `bigint`, `biginteger`, `boolean-array`, `byte-array`, `cast`, `char-array`, `class`, `class?`, `decimal?`, `definterface`, `definline`, `enumeration-seq`, `float-array`, `format`, `iterator-seq`, `memfn`, `num`, `numerator`, `denominator`, `ratio?`, `rational?`, `rationalize`, `resultset-seq`, `short-array`, `vector-of`, `bytes?`, `io!`, `PrintWriter-on`, `StackTraceElement->vec` | 31 |
  | Typed array setters | `aset-boolean`, `aset-byte`, `aset-char`, `aset-double`, `aset-float`, `aset-int`, `aset-long`, `aset-short` | 8 |
  | Namespace management | `alias`, `all-ns`, `in-ns`, `ns`, `ns-aliases`, `ns-map`, `ns-refers`, `ns-resolve`, `ns-unalias`, `refer`, `remove-ns`, `the-ns`, `find-var`, `intern`, `requiring-resolve`, `resolve`, `use`, `loaded-libs`, `namespace-munge`, `load`, `load-reader`, `load-string`, `with-loading-context` | 23 |
  | Vars | `alter-var-root`, `var-get`, `var-set`, `with-redefs-fn` | 4 |
  | Compilation | `*compile-files*`, `*compile-path*`, `*compiler-options*`, `*allow-unresolved-vars*`, `*fn-loader*`, `*use-context-classloader*`, `compile` | 7 |
  | Dynamic vars (JVM-specific) | `*clojure-version*`, `*data-readers*`, `*default-data-reader-fn*`, `*err*`, `*file*`, `*in*`, `*math-context*`, `*read-eval*`, `*reader-resolver*`, `*repl*`, `*source-path*`, `*suppress-read*`, `*unchecked-math*`, `*verbose-defrecords*`, `*warn-on-reflection*` | 15 |
  | Reader | `char-escape-string`, `char-name-string`, `clojure-version`, `default-data-readers`, `reader-conditional`, `reader-conditional?` | 6 |
  | Parallel execution | `pcalls`, `pmap`, `pvalues` | 3 |
  | Protocols / extensions | `extend`, `extenders`, `extends?`, `find-protocol-impl`, `find-protocol-method` | 5 |
  | Misc / arithmetic | `*'`, `+'`, `-'`, `dec'`, `inc'`, `with-precision`, `destructure`, `add-classpath`, `print-dup`, `print-method`, `print-simple`, `printf`, `print-ctor`, `method-sig`, `primitives-classnames`, `unquote`, `unquote-splicing`, `stream-into!`, `stream-reduce!`, `stream-seq!`, `stream-transduce!` | 21 |
  | Internal / type constructors | `->Vec`, `->VecNode`, `->VecSeq`, `-cache-protocol-fn`, `-reset-methods`, `EMPTY-NODE` | 6 |

  </details>

#### Where are we at? (Status — Activity)

- [x] Identified authoritative data source — CLJS compiler via `cljs.analyzer.api`
- [x] Retrieved var list from data source — 1090 vars (980 defs + 191 macros) extracted programmatically
- [x] Cross-referenced against ClojureDocs `clojure.core` vars — 474 of 679 present, 205 missing
- [x] Cross-referenced against ClojureDocs `clojure.string` vars — 20 of 21 JVM vars present; `re-quote-replacement` missing
- [x] Documented gaps and surprises

#### What do we need to know? (Agenda — Understanding)

- ~~How many `cljs.core` vars correspond to ClojureDocs `clojure.core` vars?~~ **Answered:** 474 of 679 (70%).
- Are there vars where the signature differs (e.g., different arities)? Out of scope for binary yes/no, but worth noting for future "partial" support.
- `locking` exists in CLJS (confirmed in analyzer output as a macro) — but is it a no-op or functional? Needs verification.

#### Where are we going? (Agenda — Activity)

1. ~~Write a script that extracts `cljs.core` var names from the CLJS API or compiler source and cross-references against ClojureDocs `clojure.core` vars.~~ **Done** — used `cljs.analyzer.api/analyze-file` with `org.clojure/clojurescript 1.12.134` as dependency.
2. Produce the CLJS column of `dialect-compat.edn` for `clojure.core` and `clojure.string` — data is ready, script not yet written.

#### Maintainer / Point of Contact

| Field | Value |
|-------|-------|
| Project | ClojureScript |
| Repository | [clojure/clojurescript](https://github.com/clojure/clojurescript) |
| Primary maintainer(s) | David Nolen (dnolen) |
| Contact info | Discord DM |
| Timezone / Location | EST / New York, NY |
| Best way to reach them | Discord DM; also `#clojurescript` on Clojurians Slack |
| Have we contacted them? | Not yet (planned via Discord DM) |
| Their stance on this feature | Expected supportive |
| Notes | ClojureScript is maintained under the Clojure contributor agreement. |

---

### 1.2 babashka

#### What do we know? (Status — Understanding)

- babashka version tested: **1.12.215** (`bb --version`, 2026-04-21).
- `clojure.core` in bb: **641 vars** vs JVM's 679.

  <details><summary><strong>57 JVM vars missing from bb</strong> (click to expand)</summary>

  | Category | Vars | Count |
  |----------|------|-------|
  | Proxy infrastructure | `construct-proxy`, `gen-class`, `get-proxy-class`, `init-proxy`, `proxy-call-with-super`, `proxy-mappings`, `proxy-name`, `proxy-super`, `update-proxy` | 9 |
  | Struct-maps (deprecated) | `accessor`, `create-struct`, `defstruct`, `struct`, `struct-map` | 5 |
  | Java interop | `bases`, `cast`, `definterface`, `gen-interface`, `import`, `io!`, `resultset-seq` | 7 |
  | Compilation | `compile`, `*allow-unresolved-vars*`, `*fn-loader*`, `*use-context-classloader*`, `with-loading-context` | 5 |
  | Agent-related | `*agent*`, `agent-errors`, `await1`, `clear-agent-errors` | 4 |
  | Ref-related | `ref-history-count`, `ref-max-history`, `ref-min-history` | 3 |
  | Parallel execution | `pcalls`, `pvalues` | 2 |
  | Internal / type constructors | `->ArrayChunk`, `->Vec`, `->VecNode`, `->VecSeq`, `EMPTY-NODE`, `-cache-protocol-fn`, `-reset-methods` | 7 |
  | Protocol introspection | `extenders`, `find-protocol-impl`, `find-protocol-method` | 3 |
  | Miscellaneous | `add-classpath`, `definline`, `find-keyword`, `Inst`, `inst-ms*`, `method-sig`, `mix-collection-hash`, `primitives-classnames`, `print-ctor`, `unquote-splicing`, `vector-of`, `*verbose-defrecords*` | 12 |

  </details>

  <details><summary><strong>19 bb-only vars</strong> not in JVM (click to expand)</summary>

  | Category | Vars | Count |
  |----------|------|-------|
  | Internal implementations | `proxy*`, `reify*`, `multi-fn-impl`, `multi-fn?-impl`, `multi-fn-add-method-impl`, `protocol-type-impl`, `-reified-methods` | 7 |
  | Threading / binding internals | `-locking-impl`, `binding-conveyor-fn`, `get-thread-binding-frame-impl`, `has-root-impl`, `reset-thread-binding-frame-impl` | 5 |
  | Var construction | `-new-var`, `-new-dynamic-var` | 2 |
  | Transaction / loading | `-run-in-transaction`, `-with-precision`, `-add-loaded-lib` | 3 |
  | Other | `global-hierarchy`, `system-time` | 2 |

  </details>
- `clojure.string` in bb: **21 vars — identical set to JVM**. Verified by sorted list comparison on 2026-04-21.
- **Surprise finding:** `pmap`, `locking`, `agent`, `send`, `ref`, `dosync` all resolve in bb. These vars were initially expected to be absent based on the template's pre-filled claim that bb "supports a subset of `clojure.core`" — in fact bb supports 641 of 679 vars (94%).
- Known data sources:
  - [ ] [babashka documentation](https://book.babashka.org/) — last verified: 2026-04-21. Does not maintain a var-by-var compatibility list.
  - [x] `bb -e '(keys (ns-publics (quote clojure.core)))'` — tested: 2026-04-21. Returns 641 vars.
  - [x] `bb -e '(keys (ns-publics (quote clojure.string)))'` — tested: 2026-04-21. Returns 21 vars.
  - [ ] babashka source / var registry — [github.com/babashka/babashka](https://github.com/babashka/babashka). Not consulted directly; `ns-publics` used as source of truth.
- Authoritative data source: `bb -e '(ns-publics ...)'` on the installed bb binary. Claude did not find a separate machine-readable var list in [any of the sources examined](#references) — the binary itself is the source of truth.

#### Where are we at? (Status — Activity)

- [x] Identified authoritative data source
- [x] Retrieved var list from data source
- [x] Cross-referenced against ClojureDocs `clojure.core` vars — 57 JVM vars missing, 19 bb-only vars
- [x] Cross-referenced against ClojureDocs `clojure.string` vars — identical 21-var set
- [x] Documented gaps and surprises

#### What do we need to know? (Agenda — Understanding)

- ~~Can we rely on `bb -e '(ns-publics ...)'` as the authoritative source, or does it miss vars?~~ **Answered:** Yes, `ns-publics` is the definitive source. It reflects the running bb binary's capabilities.
- How frequently does babashka add new var support? Is our data stale within weeks or months? **Partially answered:** bb v1.12.215 aligns closely with Clojure 1.12. New bb releases are frequent (see [releases page](https://github.com/babashka/babashka/releases) for exact cadence — 12 releases from v1.12.207 to v1.12.218 between Aug 2025 and Apr 2026). Data should be regenerated with each major bb release.
- Are there vars that exist in bb but behave differently in non-obvious ways? **Open.** The 19 bb-only vars are mostly internal. Behavioral differences in shared vars (e.g., `pmap` single-threaded in bb?) are out of scope but worth noting.
- ~~Does babashka version matter?~~ **Answered:** Yes. Target latest stable (currently 1.12.215). Record version in generated data file.
- ~~Is there an existing machine-readable list of supported vars we can consume instead of generating?~~ **Answered:** No. `ns-publics` on a running bb is the only source.

#### Where are we going? (Agenda — Activity)

1. Include `bb -e '(ns-publics ...)'` in the generation script to produce the bb column of `dialect-compat.edn`.
2. Record bb version in the EDN file header for reproducibility.

#### Maintainer / Point of Contact

| Field | Value |
|-------|-------|
| Project | babashka |
| Repository | [babashka/babashka](https://github.com/babashka/babashka) |
| Primary maintainer(s) | Michiel Borkent (borkdude) |
| Contact info | `#babashka` on Clojurians Slack |
| Timezone / Location | CET / Netherlands |
| Best way to reach them | `#babashka` on Clojurians Slack |
| Have we contacted them? | Not yet (planned via Clojurians Slack) |
| Their stance on this feature | Expected supportive |
| Existing compatibility data they publish | None — bb binary itself is the source of truth via `ns-publics` |
| Notes | bb releases are frequent (see [releases page](https://github.com/babashka/babashka/releases) for exact cadence). Data will need periodic regeneration. |

---

### 1.3 Clojure/JVM

#### What do we know? (Status — Understanding)

<!-- This dialect is the baseline — all vars on ClojureDocs are JVM-supported by definition.
     But document what we know about the data source anyway. -->

- Clojure version tracked by ClojureDocs: declared as `1.12.4` in `search/clojure-lib` config ([`search.clj`](../../src/clj/clojuredocs/search.clj#L102-L108)). Note: vars are loaded from whatever Clojure version the running JVM provides — if deployment runs a different version, the var list and declared version could disagree silently.
- All vars loaded by ClojureDocs come from JVM namespaces listed in `clojuredocs.search.static/clojure-namespaces`. Note: this list includes namespaces from separate libraries (`core.async`, `core.logic`, `data.csv`, `tools.build`) that are not part of `org.clojure/clojure` itself.
- By definition, every var currently loaded by ClojureDocs runs on Clojure/JVM, since `gather-vars` calls `ns-publics` on live JVM namespaces. The future `:clj` indicator will always be "supported" for these vars.
- Source of truth for which vars ClojureDocs displays: `clojuredocs.search/gather-vars`, which calls `ns-publics` on namespaces listed in `search.static/clojure-namespaces` at JVM startup. This may not include all Clojure/JVM vars — only those from listed namespaces.

#### Where are we at? (Status — Activity)

- [x] Confirmed the Clojure version in `src/clj/clojuredocs/search.clj` — `1.12.4`, matches `clojure -M -e '(clojure-version)'` output.
- [x] Enumerated `clojure.core` var count from ClojureDocs — **679 vars** (via `ns-publics` on running JVM, 2026-04-21). Plus special forms defined in `search.static/special-forms`.
- [x] Enumerated `clojure.string` var count from ClojureDocs — **21 vars** (via `ns-publics`, 2026-04-21).

#### What do we need to know? (Agenda — Understanding)

- ~~How many vars are in scope?~~ **Answered:** 679 (`clojure.core`) + 21 (`clojure.string`) = **700 vars** from `ns-publics`, plus special forms.
- Are there vars on ClojureDocs that lack `:name` or `:ns` metadata and would break our lookup key? **Open** — special forms are manually defined in `search.static/special-forms` with string `:ns` and symbol `:name`, so they use a different metadata shape. The generation script must handle both.
- The `clojure-namespaces` list includes separate Maven artifacts (`core.async`, `core.logic`, `data.csv`, `tools.build`). These are out of scope for this issue (scoped to `clojure.core` + `clojure.string` only), but the lookup function should gracefully return `nil` for vars in those namespaces.

#### Where are we going? (Agenda — Activity)

1. No additional research needed — Clojure/JVM is the baseline. All vars in `clojure.core` and `clojure.string` are `:clj`-supported by definition.
2. Ensure the generation script handles the special forms list from `search.static/special-forms`.

#### Maintainer / Point of Contact

| Field | Value |
|-------|-------|
| Project | Clojure |
| Repository | [clojure/clojure](https://github.com/clojure/clojure) |
| Primary maintainer(s) | Alex Miller (puredanger), Rich Hickey |
| Contact info | `#clojure` on Clojurians Slack |
| Timezone / Location | EST / various |
| Notes | This is the baseline dialect. No outreach needed unless questions arise about var metadata. |

---

## Part 2: Cross-Cutting Understanding

### What do we know across all three dialects?

<!-- Observations that span dialects. Fill in as research progresses. -->

- Total var count in scope (`clojure.core` + `clojure.string`): **700 vars** (679 + 21) from JVM `ns-publics`, plus special forms tracked separately in `search.static/special-forms`.
- Overlap — all three dialects support: **488 vars** (70% of 700). This is the portable core.
- CLJ-only vars (neither CLJS nor bb): **51 vars** (7%) — mostly proxy infrastructure, struct-maps, and deep internals.
- Known tricky cases:
  - `locking` exists in both CLJS and bb but may behave differently. In CLJS it's a macro in the analyzer but the JavaScript host has no threads, so `locking` cannot provide mutual exclusion — its runtime behavior needs verification. In bb it resolves but threading semantics under SCI are unclear. **Out of scope for v1** — binary present/absent is sufficient.
  - `pmap` is present in bb but may not parallelize in the same way as JVM `pmap` due to SCI's execution model. Present/absent still correct.
  - 6 vars exist in CLJS but not bb: `->ArrayChunk`, `Inst`, `bases`, `import`, `inst-ms*`, `mix-collection-hash`. These are internal/interop vars.

#### Three-way partition of `clojure.core` (679 vars)

| Partition | Count | % | Examples |
|-----------|-------|---|----------|
| All 3 dialects (JVM ∩ CLJS ∩ bb) | **468** | 69% | `map`, `filter`, `reduce`, `atom`, `swap!`, `str` |
| JVM + bb only | **154** | 23% | `agent`, `send`, `ref`, `dosync`, `future`, `promise`, `slurp`, `spit`, `read-string`, `ns-publics` |
| JVM + CLJS only | **6** | <1% | `->ArrayChunk`, `Inst`, `bases`, `import`, `inst-ms*`, `mix-collection-hash` |
| JVM only | **51** | 8% | `gen-class`, `proxy`, `construct-proxy`, `defstruct`, `struct-map`, `compile`, `pcalls`, `pvalues` |

#### Three-way partition of `clojure.string` (21 vars)

| Partition | Count | Var(s) |
|-----------|-------|--------|
| All 3 dialects | **20** | All except `re-quote-replacement` |
| JVM + bb only | **1** | `re-quote-replacement` |
| JVM + CLJS only | 0 | — |
| JVM only | 0 | — |

#### Combined scope (700 vars)

| Partition | Count | % |
|-----------|-------|---|
| All 3 dialects | **488** | 70% |
| JVM + bb only | **155** | 22% |
| JVM + CLJS only | **6** | <1% |
| JVM only | **51** | 7% |

**Key insight:** bb's coverage (662/700 = 94.6%) is almost a strict superset of CLJS's coverage (494/700 = 70.6%). Only 6 vars exist in CLJS but not bb. This means the data model can be simple: most vars are either "all three" or "JVM + bb."

> **Coverage math:** bb = 641 (`clojure.core`) + 21 (`clojure.string`) = 662 of 700 vars. CLJS = 474 (`clojure.core`) + 20 (`clojure.string`) = 494 of 700 vars.

### Data quality assessment

| Data source | Format | Freshness | Machine-readable? | Confidence |
|-------------|--------|-----------|-------------------|------------|
| ClojureDocs var list | in-memory at startup via `ns-publics` | Clojure `1.12.4` (declared in `search.clj` config) | yes (code) | **high** |
| CLJS compiler (`cljs.analyzer.api`) | Clojure analyzer state | `org.clojure/clojurescript 1.12.134` (2026-04-21) | yes (programmatic) | **high** — captures both `:defs` and `:macros` |
| babashka `ns-publics` | EDN via `bb -e` | bb `1.12.215` (2026-04-21) | yes (programmatic) | **high** |
| CLJS API docs (cljs.github.io) | HTML | current | no (manual only) | **medium** — misses macros (see methodology note in §1.1) |
| CLJS cheatsheet (cljs.info) | HTML | current | no | **low** — curated subset, not comprehensive |
| babashka docs (book.babashka.org) | HTML | current | no | **low** — no var-by-var listing |

### Risks and open questions

1. **Version drift** — all three dialects release independently. The compatibility data is a snapshot tied to specific versions (Clojure 1.12.4, CLJS 1.12.134, bb 1.12.215). Data needs version pinning in the generated file and periodic regeneration. A var added to bb in a future release would show as "unsupported" until regenerated.
2. **Behavioral differences** — binary present/absent doesn't capture vars that exist but behave differently across dialects. Out of scope for v1, but the data model should leave room for a future "partial" state.
3. **Special forms** — ClojureDocs tracks special forms in [`search.static/special-forms`](../../src/clj/clojuredocs/search/static.clj#L41-L130) with a different metadata shape (string `:ns`, symbol `:name`) than regular vars. The generation script and lookup function must handle both.
4. **Separate Maven artifacts** — `clojure-namespaces` in [`search/static.clj`](../../src/clj/clojuredocs/search/static.clj#L3-L39) includes `core.async`, `core.logic`, `data.csv`, `tools.build`, etc. (38 namespaces total — verified by counting entries in the `clojure-namespaces` vector). These are out of scope for this issue but the lookup function must gracefully return `nil` for vars in those namespaces.

---

## Part 3: Implementation Approach

> Do not fill this section until Parts 1 and 2 have enough substance to make decisions from. The purpose of the research above is to make this section obvious, not to justify a pre-chosen approach.

### Proposed approach (high-level)

Add a static EDN file (`resources/dialect-compat.edn`) that maps each of the 700 in-scope vars to the set of dialects that support it. A standalone generation script produces this file by querying the CLJS compiler analyzer and a running bb binary, with special forms hardcoded. At startup, a new namespace reads the file into a map and exposes a lookup function. The var page renderer calls this function and renders three small dialect logo icons — full opacity for supported, dimmed for unsupported — inside the existing `.var-meta` div.

### Key decisions

| Decision | Options considered | Chosen | Why |
|----------|-------------------|--------|-----|
| Data file format | EDN / JSON / database | **EDN** (`resources/dialect-compat.edn`) | Idiomatic to Clojure; no new deps; readable by all three dialects. JSON lacks keywords/sets. Database ruled out by [data model coupling audit](data-model-coupling-audit.md). |
| Data file key format | Qualified string / two-level nested map | **Qualified string** (`"clojure.core/map"` → `#{:clj :cljs :bb}`) | Simple, flat, O(1) lookup. Matches how vars are displayed. |
| Data generation | Manual / scripted / hybrid | **Scripted** (`tools/gen_dialect_compat.clj`) | 700 entries too many to hand-maintain. All three data sources are programmatically accessible. |
| Script execution | `lein run -m` / `lein-exec` / standalone | **Standalone** (load from `lein repl`) | Simplest; no build tool coupling; portable to future site redesign. Instructions in `tools/README.md`. |
| Special forms handling | Hardcoded / excluded / detected | **Hardcoded** in gen script | 15 entries, rarely change, not in `ns-publics`. All 15 in JVM+bb; 13 in CLJS (`monitor-enter`/`monitor-exit` excluded). |
| Loading pattern | Startup def / on-demand slurp / config lib | **Startup def** (`search/compat.clj`) | Follows existing `clojure-lib` pattern in [`search.clj`](../../src/clj/clojuredocs/search.clj#L102-L108). O(1) lookup, no per-request I/O. |
| Rendering location | `$var-header` / below signature / sidebar | **`$var-header`** inside `.var-meta` div | Dialect support is var identity metadata — belongs next to "Available since" and namespace link. |
| Badge visual | Text labels / logo icons / colored pills | **Small dialect logo icons** (Clojure, ClojureScript, babashka) | Compact, visually recognizable, community-standard iconography. |
| Unsupported display | Omit / dim / strikethrough | **Dimmed** (reduced opacity) | Shows all three icons for in-scope vars; communicates "we checked, it's absent" rather than leaving the user guessing. |
| Unknown state handling | Show "?" / show nothing / show "unknown" | **Show nothing** (omit all badges) | No badges for out-of-scope vars honestly communicates "we haven't checked." |

### Breakdown into smaller steps

1. **Generate compatibility data** — Write `tools/gen_dialect_compat.clj`. Run it to produce `resources/dialect-compat.edn` with a `:versions` header and 715 entries (700 vars + 15 special forms). **Done signal:** EDN file exists, contains version-pinned data, round-trips through `clojure.edn/read-string`.

2. **Add lookup namespace** — Create `src/clj/clojuredocs/search/compat.clj` with a top-level `def` that reads the EDN file and a `(defn dialect-support [ns name])` function. Both regular vars and special forms use string `:ns`; the only difference is `:name` (string for regular vars, symbol for special forms). The lookup key is always `"ns/name"` so the function coerces both to strings. Returns `nil` for out-of-scope vars. **Done signal:** `(dialect-support "clojure.core" "map")` returns `#{:clj :cljs :bb}`; `(dialect-support "clojure.core" "gen-class")` returns `#{:clj}`; `(dialect-support "clojure.core.async" "go")` returns `nil`.

3. **Render badges in var page** — Modify `$var-header` in [`pages/vars.clj`](../../src/clj/clojuredocs/pages/vars.clj) to call `dialect-support` and render three `<img>` elements with appropriate CSS classes and `title`/`aria-label` attributes for accessibility (e.g., `title="Supported in ClojureScript"` vs `title="Not available in ClojureScript"`). **Done signal:** var pages for in-scope vars show three dialect icons; out-of-scope var pages show none.

4. **Add CSS and logo assets** — Add dialect logo images to `resources/public/img/` and `.dialect-badge` styles in [`css.clj`](../../src/clj/clojuredocs/css.clj) (supported = full opacity, unsupported = dimmed). **Done signal:** badges render correctly with visual distinction between supported and unsupported.

5. **Verify and clean up** — Spot-check badge rendering for representative vars: all-three (`map`), JVM+bb (`agent`), JVM+CLJS (`->ArrayChunk`), JVM-only (`gen-class`), special form (`if`), out-of-scope (`clojure.set/union`). Verify `title`/`aria-label` attributes on icons. **Done signal:** all spot-checks pass; no badges on out-of-scope vars.

---

### External dependencies

<!-- People, repos, or events that could block or accelerate this work. -->

| Dependency | Who/what | Status | Impact if delayed |
|------------|----------|--------|-------------------|
| ClojureScript var list | CLJS compiler v1.12.134 via `cljs.analyzer.api` | **done** — 1090 vars extracted (2026-04-21) | None — data is captured. Regenerate when CLJS releases a new version. |
| babashka var list | bb v1.12.215 via `bb -e '(ns-publics ...)'` | **done** — 641 core + 21 string vars extracted (2026-04-21) | None — data is captured. Regenerate when bb releases a new version. |
| ClojureScript maintainer review | David Nolen (dnolen) — Discord DM | not started | Low — data is programmatically derived from compiler source; review is for courtesy and accuracy, not blocking. |
| babashka maintainer review | Michiel Borkent (borkdude) — `#babashka` on Clojurians Slack | not started | Low — same rationale. |
| Dutch Clojure Days (mid-May) | community event | upcoming | Shipping before this event could provide a feedback opportunity. |
| babashka conf (mid-May) | community event | upcoming | Shipping before this event could provide a feedback opportunity. |

---

## References

- [Issue #30: Cross-Dialect Compatibility Indicators](https://github.com/nubank/clojuredocs/issues/30)
- [ClojureDocs Two-Year Vision (2026-2028)](../2026vison.md) — "Cross-dialect hub" strategic bet
- [Data Model Coupling Audit](data-model-coupling-audit.md) — constraints on schema changes
- Rich Hickey, ["Design in Practice"](https://www.youtube.com/watch?v=c5QF2HjHLSE) (Clojure/conj 2023) — [transcript](https://github.com/matthiasn/talk-transcripts/blob/c879a07b037557f15d98587fbb1e7d7b23251fe4/Hickey_Rich/DesignInPractice.md)
- Alex Miller, ["Design in Practice in Practice"](https://www.youtube.com/watch?v=VBnGhQOyTM4) (Clojure/conj 2024)
- Clojure Team Bets for 2026 (internal) — ClojureDocs as reference application
- [ClojureScript cheatsheet](https://cljs.info/cheatsheet/)
- [babashka documentation](https://book.babashka.org/)
- [jank-lang/clojure-test-suite](https://github.com/jank-lang/clojure-test-suite) — future data source for jank dialect
- `src/clj/clojuredocs/pages/vars.clj` — var page renderer
- `src/clj/clojuredocs/search/static.clj` — static data loading pattern to follow

---

## Version History

| Date | Changes |
|------|---------|
| <!-- YYYY-MM-DD --> | Initial planning document created. |
| 2026-04-14 | Glossary review: fixed 4 definitions (Dialect, Var, Compatibility indicator, EDN), added 3 terms (Namespace, Static compatibility index, Var page), added Source column. Added Errata, Learnings, and AI Disclaimer sections. |
| 2026-04-14 | Claims audit: fixed 6 unverified claims in ClojureScript, babashka, and Clojure/JVM sections. Fixed data quality table freshness descriptions. Fixed broken Data Model Coupling Audit link. Softened event dependency wording. Added errata 5–9. |
| 2026-04-21 | Part 1: filled all three dialect sections with verified research. CLJS vars extracted programmatically via `cljs.analyzer.api` on compiler 1.12.134 (474/679 present, 205 missing). bb vars via `ns-publics` on bb 1.12.215 (641/679, 57 missing). Added categorized gap tables, maintainer contacts, methodology note on API-docs undercounting, reviewer line. ([`099e94f`](https://github.com/nubank/clojuredocs/commit/099e94f)) |
| 2026-04-21 | Part 2: cross-cutting analysis. Computed three-way overlap matrix (488/700 vars portable across all dialects, 70%). Updated data quality table with confidence levels and version-pinned sources. Added 4 risks. ([`3828d57`](https://github.com/nubank/clojuredocs/commit/3828d57)) |
| 2026-04-21 | Claims audit revisions: fixed bb coverage math (622/700→662/700, 89%→94.6%), fixed CLJS API docs "comprehensive" claim, fixed namespace count (37→38), replaced "~monthly" bb release cadence with verified data (12 releases in ~8.5 months), scoped absence claim for bb machine-readable var list, added source links to `search/static.clj`, refined tricky cases wording, added errata 10–12. |
| 2026-04-24 | Created `docs/decisions.md` with decided entries for Issue #30 (EDN format, qualified-string keys, scripted generation, standalone execution, hardcoded special forms). ([`75deacd`](https://github.com/nubank/clojuredocs/commit/75deacd)) |
| 2026-04-27 | Added proposed implementation decisions (loading pattern, rendering location, badge visual, unsupported display, unknown state handling). Applied audit corrections to decision entries. ([`fff2ec7`](https://github.com/nubank/clojuredocs/commit/fff2ec7)) |
| 2026-04-28 | Part 3: filled implementation approach, key decisions table (10 decisions), and 5 implementation steps with done signals. ([`5078c8a`](https://github.com/nubank/clojuredocs/commit/5078c8a), [`73c34bd`](https://github.com/nubank/clojuredocs/commit/73c34bd)) |
| 2026-04-28 | Implementation: generation script + EDN data ([`f2eac48`](https://github.com/nubank/clojuredocs/commit/f2eac48)), dialect badges in var pages ([`72a9eeb`](https://github.com/nubank/clojuredocs/commit/72a9eeb)), official logos ([`3ae3cf8`](https://github.com/nubank/clojuredocs/commit/3ae3cf8), [`5b7fcc9`](https://github.com/nubank/clojuredocs/commit/5b7fcc9)), RCF verification + decision log entries ([`38bd87f`](https://github.com/nubank/clojuredocs/commit/38bd87f)), jank deferral decision ([`6ab29a2`](https://github.com/nubank/clojuredocs/commit/6ab29a2)). Added 6 learnings. Updated AI disclaimer with implementation acknowledgements and runtime verification results. |

---

## Errata

<!-- This section is for errors discovered and subsequently corrected. Each entry states
     what was wrong, the correction, and why the error likely occurred. Errata are not
     limitations — limitations qualify scope or confidence and belong inline. -->

1. **Dialect definition said "runtime implementation"** — ClojureScript is a compiler targeting JavaScript, not a runtime implementation. Corrected to "language implementation that shares Clojure syntax but targets a distinct host platform." Error likely occurred because the initial draft generalized from Clojure/JVM and babashka (both of which involve a runtime) without considering ClojureScript's compilation model.

2. **Var definition excluded special forms** — The original definition ("named reference to a value in a Clojure namespace") is the Clojure language definition of var, which excludes special forms like `def`, `if`, and `recur`. But ClojureDocs documents special forms as vars, and they are in scope for dialect compatibility. The project glossary already defines var to include special forms. Corrected to match. Error: the initial draft used the Clojure language definition rather than the project-specific definition.

3. **Compatibility indicator called "label" instead of "badge"** — The feature proposal document (`feature-proposals-q2-2026.md`) consistently uses "badge" terminology. Using "label" introduced a terminological split. Corrected to "badge." Error: synonym substitution without checking existing usage.

4. **EDN definition was an acronym expansion, not a definition** — "Extensible Data Notation. Clojure's data format" expands the acronym but doesn't define what EDN is (a data serialization format). Also, "Clojure's data format" implies exclusivity when Clojure uses multiple formats (Transit, JSON, etc.). Corrected to a substitutable definition. Error: conflating acronym expansion with definition.

5. **ClojureScript `cljs.core` claim asserted unverified facts** — Original: "`cljs.core` maps to `clojure.core` with known omissions (e.g., `pmap`, `locking`, concurrency primitives)." This stated unverified claims as facts, used "maps to" ambiguously, and defined omissions by example only ("concurrency primitives" is undefined). Corrected to mark as unverified. Error: pre-filling research sections with plausible-sounding claims that hadn't been checked.

6. **ClojureScript `clojure.string` claim asserted full equivalence** — Original: "`clojure.string` is available in ClojureScript as `clojure.string` (same namespace)." The word "available" implied all vars are present and behave identically. Corrected to note existence without claiming completeness. Error: same as #5.

7. **babashka claim presupposed research outcome** — Original: "babashka supports a subset of `clojure.core` and `clojure.string` (plus additional namespaces)." This asserted "a subset" and "plus additional namespaces" before any verification. Corrected to note the namespaces exist without claiming the relationship. Error: same as #5.

8. **Clojure/JVM section conflated namespace list with var list** — Original: "All vars in `clojuredocs.search.static/clojure-namespaces` are Clojure/JVM vars." `clojure-namespaces` is a list of namespace symbols, not vars. Also silently included separate Maven artifacts (`core.async`, `core.logic`, etc.) under "Clojure/JVM vars" — technically true but misleading. Corrected to distinguish namespaces from vars and note the multi-artifact composition. Error: imprecise language about the data structure, plus conflating the runtime (JVM) with the specific libraries.

9. **Data Model Coupling Audit reference was a dead link** — Path `../resources/datamodelaudit.md` does not exist. The file is at `data-model-coupling-audit.md` (same directory). Corrected. Error: the prompt specified the path as `docs/resources/datamodelaudit.md`, and the initial draft copied it without verifying.

10. **CLJS API docs described as "Comprehensive listing of all public vars"** — The API docs page for `cljs.core` lists only `:defs` (functions, protocols, types), not the 191 macros imported from `cljs/core.clj`. Corrected to "Listing of public defs (functions, protocols, types). Does not include macros." Error: the word "comprehensive" was applied without verifying against the methodology note that already documented the undercounting.

11. **Namespace count was 37, actually 38** — The `clojure-namespaces` vector in `search/static.clj` contains 38 entries, not 37. Corrected and added verification note. Error: off-by-one from manual counting (a common AI failure mode).

12. **bb coverage calculated as 622/700 (89%)** — Used 679−57=622 (core only) with denominator 700 (core + string). Correct calculation: 641 (core) + 21 (string) = 662/700 = 94.6%. Similarly, CLJS was stated as 71% without showing work; correct is 474+20=494/700=70.6%. Corrected both figures and added explicit coverage math. Error: mixing denominators — subtracted missing vars from the wrong total.

13. **Preamble said "not a checklist" but research tasks used checkbox syntax** — The "Where are we at?" sections used `- [x]` / `- [ ]` checklist formatting despite the preamble stating "It is not a checklist." Removed the contradictory sentence from the preamble. Error: the preamble was written before the research sections were filled in, and the checkbox format was a natural fit for tracking completed research steps — but nobody went back to reconcile the two.

14. **Dialect definition didn't match Clojure/JVM** — Original: "A language implementation that shares Clojure syntax but targets a distinct host platform." Clojure/JVM doesn't "share" its own syntax — it defines it. Corrected to "A host-targeted implementation of the Clojure language." Error: the definition framed dialects as deviations from a reference, then included the reference itself.

15. **Namespace definition used undefined term and JVM-centric framing** — Original: "A named container for vars within a library." The term "library" is defined in the project glossary but not in this document's glossary. Also, "loaded at JVM startup" is host-specific in a cross-dialect document. Corrected to use dotted-symbol example and remove host-specific language. Error: carried over JVM-centric framing from the project glossary without adapting for cross-dialect context.

16. **Static compatibility index definition was non-substitutable** — Original began with "The data artifact this issue produces:" — a self-referential clause that can't be substituted for the term. Removed the initial clause, keeping the substitutable definition. Error: preamble-as-definition.

17. **Clojure Team Bets reference was a dead link** — Path `../resources/Clojure_2026_1_Pager.pdf` does not exist in the repository. Replaced with title and "(internal)" marker. Error: the reference was added without verifying the file existed in the repo.

18. **`search.clj` line number off by one** — Document said "L105" for `version "1.12.4"` but the version string is at line 104; L105 is `:source-base-url`. Replaced with a line range link to the full `clojure-lib` def (L102–L108). Error: manual line counting.

19. **Transcript link used branch name instead of permalink** — `talk-transcripts/blob/master/...` moves with HEAD. Pinned to commit `c879a07` (latest commit on that file). Error: copied URL from GitHub's default branch view.

20. **CLJS compiler source link used branch name instead of version tag** — `clojurescript/blob/master/...` instead of the `r1.12.134` tag matching the analyzed version. Corrected. Error: same as #19.

21. **Repository URLs in maintainer tables were raw URLs** — Plain `https://github.com/...` instead of markdown links with descriptive text. Converted to `[org/repo](url)` format. Error: tables were filled quickly without applying link formatting.

22. **`search/static.clj` links lacked line ranges** — References to `special-forms` and `clojure-namespaces` linked to the whole file without anchoring to the specific vectors. Added `#L41-L130` and `#L3-L39` respectively. Error: links were added without line specificity.

---

## Learnings

<!-- This section is for process, tooling, or design insights gained during this work
     that are reusable beyond this issue. Not dialect-specific findings (those go in
     Parts 1-2). -->

1. **RCFs as persistent verification** — Rich comment forms serve double duty: verify behavior during development and persist as executable documentation for future contributors. Any new namespace with non-obvious behavior should include one.
2. **Static EDN over database for infrequently-changing reference data** — The [data model coupling audit](data-model-coupling-audit.md) showed that adding fields to MongoDB would couple this feature to the deployment pipeline. A checked-in EDN file is version-controlled, diffable, and requires no migration. Apply this pattern whenever reference data changes on a release cadence rather than per-request.
3. **Dimmed vs. omitted communicates different things** — Showing a dimmed icon means "we checked, it's absent." Omitting icons entirely means "we haven't checked." This distinction avoids misleading users about coverage scope. Reusable for any feature with a "not yet measured" state.
4. **Generation scripts should record input versions** — `dialect-compat.edn` includes `:versions` so anyone can tell exactly what was measured. Without this, stale data looks identical to fresh data.
5. **Claims audits catch arithmetic errors that prose review misses** — Decomposing into atomic claims caught two arithmetic errors (bb coverage 89%→94.6%, namespace count 37→38). When a document contains computed numbers, audit the math explicitly rather than reading for plausibility.
6. **`ns-publics` is the universal cross-dialect enumeration primitive** — The same function works in JVM, CLJS (via analyzer), and bb, making it the natural join key for cross-dialect comparison. Future dialect additions (e.g., jank) should use the same primitive if available.

---

> **AI Disclaimer**
>
> - **Jordan Miller** directed all decisions: task definition, research approach, methodology choices (CLJS compiler parsing over API docs, RCFs over unit tests), logo sourcing, reviewer list, and claims audit review. Approved every finding before it was written.
> - **Claude (Opus 4.6, via VS Code Copilot)** drafted this document, ran the var-extraction commands, computed overlap tables, performed the claims audit (14 findings, 10 revisions), wrote the implementation code, and analyzed jank for deferral.
>
> **Verified at runtime** (2026-04-28, via Calva nREPL):
> - Data loading: 715 entries, correct versions (`1.12.4` / `1.12.134` / `v1.12.215`)
> - `dialects-for` lookups: all-3, JVM-only, JVM+bb, cross-namespace, and nil-for-unknown cases
> - Badge rendering: correct HTML output and visual display at `localhost:8080`
> - Test suite: 1 test, 1 assertion, 0 failures
> - Verification forms preserved in the RCF in `search/compat.clj`
>
> AI-generated content may contain false statements. All research sections should be independently verified.