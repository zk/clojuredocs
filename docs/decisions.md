# Decision Log

Document design and architecture decisions. Lightweight alternative to full ADRs.

---

## 2026-05-11 — Schedule export JSON regeneration in-process

### Status
Decided

### Context
- `clojuredocs-export.json` is consumed by editor plugins (Calva, CIDER, etc.) to show usage examples inline. It was only regenerated manually via `lein run -m clojuredocs.export`.
- The file went stale — vars like `halt-when` had `null` examples in the JSON despite having examples on the website. A community member reported the issue in Slack.
- Issue #38 was created to automate regeneration.

### Decision
- Use `java.util.concurrent.ScheduledExecutorService` in `start-app` to run `export/run-export` every 6 hours, starting immediately on server boot.

### Rationale
- In-process scheduling reuses the existing DB connection and data access layer — no cold-start JVM, no separate env config.
- Zero new dependencies; `ScheduledExecutorService` is a JVM primitive.
- Initial delay of 0 means every deploy immediately refreshes the export.
- Scales through the planned redesign: as the data model changes (multi-library, dialect metadata, quality signals), the export function evolves with it and the scheduler is indifferent.

### Alternatives Considered
- Cron job on the server (`lein run -m clojuredocs.export`) — spins up a separate JVM each time, requires MongoDB URL in cron environment, loses access to shared caches and connection pools.
- External scheduler (e.g., systemd timer) — adds infrastructure dependency and deployment complexity for a simple periodic task.

### Impacts and Risks
- Export runs on a dedicated single-thread executor, so at most one export runs at a time.
- Risk: a long-running export could overlap with the next scheduled run. Mitigation: `scheduleAtFixedRate` skips if the previous run hasn't finished; export currently takes seconds.
- Risk: export failure kills the scheduler thread. Mitigation: `run-scheduled-export` wraps the call in try/catch.

### Links
- [Issue #38](https://github.com/nubank/clojuredocs/issues/38)
- [src/clj/clojuredocs/export.clj](src/clj/clojuredocs/export.clj)
- [src/clj/clojuredocs/main.clj](src/clj/clojuredocs/main.clj)

---

## 2026-04-28 — Defer jank dialect until stable var enumeration exists

### Status
Decided

### Context
- jank is a native Clojure dialect on LLVM with C++ interop, currently in alpha.
- Nubank sponsors the jank project.
- jank has a WIP nREPL server PR (#698) from 2 months ago, suggesting growing introspection capabilities — though the feature may not be complete.
- The `jank-lang/clojure-test-suite` repo already tests 5 dialects (Clojure, ClojureScript, babashka, ClojureCLR, Basilisp), but jank can't run `clojure.test` yet.
- The current dialect badge architecture requires a data source function (~10 lines), `cond->` clauses in `build-compat-map` (~4 lines), one `dialect-info` entry, a special forms set, and a logo file.

### Decision
- Do not add jank as a dialect in v1; revisit when jank supports `ns-publics` or provides a stable var enumeration mechanism.

### Rationale
- jank is pre-1.0 and its var surface is actively evolving (the project has daily commits).
- The test suite is a proxy for coverage ("tested") not API surface ("supported"), which is a meaningful distinction for user-facing badges.
- The architecture is additive — adding jank later requires no breaking changes to data format, rendering, or CSS.
- Worth a conversation with Jeaye (jank author) about a blessed way to enumerate supported vars before building a scraper.

### Alternatives Considered
- Scrape `jank-lang/clojure-test-suite` directory listing to infer supported vars — depends on directory naming conventions and file structure, which are not a stable API; conflates "tested" with "supported."
- Add jank now with frequent regeneration — high maintenance burden for unstable data; could mislead users.

### Impacts and Risks
- jank users won't see compatibility badges on ClojureDocs.
- Risk: none. Adding a fourth dialect requires changes to ~4 locations in the codebase, assuming a working var enumeration mechanism exists.

### Links
- [jank-lang/jank](https://github.com/jank-lang/jank)
- [jank-lang/clojure-test-suite](https://github.com/jank-lang/clojure-test-suite)
- [jank nREPL PR #698](https://github.com/jank-lang/jank/pull/698)
- [2026-04-14 — Three initial dialects](docs/decisions.md)

---

## 2026-04-28 — Verify dialect compat via rich comment blocks, not unit tests

### Status
Decided

### Context
- After implementing dialect badges, needed to capture REPL verification results as a repeatable artifact.
- The project has no real test suite — only a placeholder test (`core_test.clj` with `(is (= 1 1))`).
- PR #27 explores REPL-verified code review as a demo topic, with a tentative presentation date of June 11, 2026. `cognitect-labs/transcriptor` was flagged as relevant prior art.

### Decision
- Verify dialect compatibility lookups via inline rich comment blocks in `search/compat.clj`, not unit tests.

### Rationale
- Rich comment blocks are evaluable at the REPL during development — no test runner needed, immediate feedback.
- They serve as living documentation: expected results are inline comments next to each form.
- Adding a unit test to a project with zero real test coverage sets a convention nobody is following yet. RCFs match the project's current maturity.
- The RCF in `compat.clj` doubles as a concrete artifact for the PR #27 REPL-verified code review demo.

### Alternatives Considered
- Unit tests in `core_test.clj` — establishes a convention the project doesn't follow; requires a test runner; results are less visible during development.
- No verification artifact — REPL results are ephemeral; not repeatable by other contributors.

### Impacts and Risks
- RCFs are not run in CI — regressions won't be caught automatically.
- Risk: someone changes `dialect-compat.edn` without evaluating the RCF. Mitigation: low probability; the EDN is script-generated, not hand-edited.

### Links
- [search/compat.clj RCF](src/clj/clojuredocs/search/compat.clj)
- [PR #27 — REPL-verified code review](https://github.com/nubank/clojuredocs/pull/27)
- [cognitect-labs/transcriptor](https://github.com/cognitect-labs/transcriptor)

---

## 2026-04-28 — Hardcode special forms dialect support

### Status
Decided

### Context
- ClojureDocs tracks 15 special forms in `search/static.clj`: `def`, `if`, `do`, `quote`, `var`, `recur`, `throw`, `try`, `catch`, `finally`, `.`, `set!`, `monitor-enter`, `monitor-exit`, `new`.
- Special forms are compiler built-ins — they don't appear in `ns-publics` output, so the generation script's programmatic extraction (CLJS analyzer, bb `ns-publics`) won't capture them.
- Need to decide how to include special forms in the compatibility data.

### Decision
- Hardcode which special forms each dialect supports in the generation script.
- All 15 special forms are supported in Clojure/JVM by definition.
- CLJS supports: `def`, `if`, `do`, `quote`, `var`, `recur`, `throw`, `try`, `catch`, `finally`, `.`, `set!`, `new` (13 of 15). `monitor-enter` and `monitor-exit` are JVM threading primitives with no JS equivalent.
- bb supports: `def`, `if`, `do`, `quote`, `var`, `recur`, `throw`, `try`, `catch`, `finally`, `.`, `set!`, `monitor-enter`, `monitor-exit`, `new` (all 15). bb runs on the JVM via SCI and supports the full set.

### Rationale
- Hardcoding is appropriate because the set of special forms changes extremely rarely (last change was `clojure.core/import*` in Clojure 1.0) and there are only 15 entries.
- Programmatic detection is not feasible — special forms are not vars and don't appear in `ns-publics` or analyzer `:defs`.
- The alternative of excluding special forms would leave gaps on some of the most-visited var pages (`if`, `def`, `do`, etc.).

### Alternatives Considered
- Exclude special forms from compat data entirely — leaves high-traffic pages without badges.
- Detect programmatically — not feasible; special forms aren't in `ns-publics`.

### Impacts and Risks
- If a future Clojure version adds or removes a special form, the hardcoded list needs manual update.
- Risk: very low. Special forms are the most stable part of the language.

### Links
- [search/static.clj special-forms](src/clj/clojuredocs/search/static.clj#L43)
- [Issue #30](https://github.com/nubank/clojuredocs/issues/30)

---

## 2026-04-28 — Standalone script for dialect data generation

### Status
Decided

### Context
- The generation script (`tools/gen_dialect_compat.clj`) needs JVM for the CLJS analyzer and shells out to `bb`.
- Need to decide how to invoke it: Leiningen task, lein-exec plugin, or standalone manual execution.
- The current site will be redesigned per the 2026 Vision — infrastructure choices should be portable.

### Decision
- Keep the script as a standalone Clojure file loaded and invoked manually from a REPL (`lein repl`, then `(load-file "tools/gen_dialect_compat.clj")`).
- Add a `tools/README.md` with instructions for running the script.

### Rationale
- Simplest and most composable approach — no build tool plugins, no source path changes.
- Portable: when the site is redesigned, the script can be moved to the new project without Leiningen coupling.
- Matches the existing `tools/` convention — `dev_export.clj`, `sanity_check.clj`, and `top_contribs.clj` are all standalone files.
- The script runs rarely (only when dialect versions change), so ergonomic automation is not yet justified.

### Alternatives Considered
- `lein run -m tools.gen-dialect-compat` — requires adding `tools/` to Leiningen source paths; couples script to build config.
- `lein exec tools/gen_dialect_compat.clj` — requires `lein-exec` plugin (not in `project.clj`); adds a dependency for rare use.

### Impacts and Risks
- Slightly more manual than a one-liner, but documented in `tools/README.md`.
- Risk: someone forgets how to run it. Mitigation: README with exact commands.

### Links
- [Issue #30](https://github.com/nubank/clojuredocs/issues/30)
- [tools/ directory convention](tools/)

---

## 2026-04-28 — Use EDN for dialect compatibility data file

### Status
Decided

### Context
- Need a format for the static compatibility index mapping 700 qualified var names to their supported dialects.
- The file is checked into the repo, read once at startup, never written by the app.
- Codebase uses EDN for figwheel/ClojureScript build configuration (`dev.cljs.edn`, `prod.cljs.edn`).

### Decision
- Use EDN as the data file format for `dialect-compat.edn`.
- Key format: qualified string (`"clojure.core/map"`) mapping to a set of dialect keywords (`#{:clj :cljs :bb}`).
- Example: `{"clojure.core/map" #{:clj :cljs :bb}, "clojure.core/gen-class" #{:clj}}`.

### Rationale
- Idiomatic to Clojure — readable by all three target dialects (JVM, CLJS, bb).
- No additional dependencies needed; `clojure.edn/read-string` is in core.
- JSON — already available via Cheshire (`project.clj`), but not idiomatic for Clojure config and lacks reader literals for symbols and keywords. Database would require schema changes (ruled out by the data model coupling audit).

### Alternatives Considered
- JSON — already available via Cheshire (`project.clj`), but not idiomatic for Clojure config, and lacks reader literals for symbols and keywords.
- Database (MongoDB) — ruled out by the data model coupling audit constraint of no schema changes; also overkill for static read-only data.

### Impacts and Risks
- EDN file must be regenerated when dialect versions change.
- Risk: stale data if regeneration is forgotten. Mitigation: version-pin dialect versions in the file header and document regeneration steps.

### Links
- [Issue #30](https://github.com/nubank/clojuredocs/issues/30)
- [Data Model Coupling Audit](docs/research/data-model-coupling-audit.md)
- [Planning doc, Part 3](docs/research/issue-30-dialect-compat-planning.md)

---

## 2026-04-28 — Script-generate dialect compatibility data

### Status
Decided

### Context
- The compatibility index covers 700 vars across 3 dialects — too many to maintain by hand.
- Data sources are programmatic: JVM `ns-publics`, CLJS `cljs.analyzer.api`, bb `ns-publics` via shell.

### Decision
- Write a Clojure script at `tools/gen_dialect_compat.clj` that generates `resources/dialect-compat.edn`.

### Rationale
- All three data sources are already proven programmatically accessible (verified 2026-04-21).
- Checked-in output means the app never needs CLJS or bb at startup.
- `tools/` directory matches existing repo convention (`tools/dev_export.clj`, `tools/sanity_check.clj`).

### Alternatives Considered
- Manual curation — error-prone at 700 entries, would fall out of date silently.
- Hybrid (script for initial generation, manual overrides) — unnecessary complexity; no known cases where manual override is needed.

### Impacts and Risks
- Requires `org.clojure/clojurescript` and `bb` to be available when regenerating.
- Risk: script output could diverge from live runtime if dialect versions drift. Mitigation: record exact versions in the EDN file header.

### Links
- [Issue #30](https://github.com/nubank/clojuredocs/issues/30)
- [Planning doc, Part 1 methodology note](docs/research/issue-30-dialect-compat-planning.md)

---

## 2026-04-28 — Load compatibility data at startup via def

### Status
Decided

### Context
- Need a way for `pages/vars.clj` to look up dialect support for a var at render time.
- Existing pattern: `search/clojure-lib` is a `def` computed at startup. Both are top-level defs evaluated at load time, but `clojure-lib` introspects live JVM namespaces while the compatibility index reads a static file.

### Decision
- Add a `def` (in `search/compat.clj` or similar) that reads the EDN file at startup and exposes a `dialect-support` lookup function.

### Rationale
- Follows the established `search.clj` pattern — a top-level def evaluated at load time.
- O(1) lookup per var via a hash map, no per-request I/O.

### Alternatives Considered
- On-demand `slurp` per request — wasteful I/O on every var page load.
- Config library (e.g., `aero`, `cprop`) — unnecessary new dependency for a single static file.

### Impacts and Risks
- Data is immutable for the lifetime of the JVM process — requires restart to pick up new data.
- Risk: none beyond existing pattern. `clojure-lib` already works this way.

### Links
- [search.clj L102-L108](src/clj/clojuredocs/search.clj#L102-L108) — existing pattern

---

## 2026-04-28 — Render dialect badges in $var-header

### Status
Decided

### Context
- Need to show which dialects support a var on its page.
- The `$var-header` function in `pages/vars.clj` already renders var name, namespace link, "Available since", and source link in a `.var-meta` div.

### Decision
- Render dialect icons as small inline elements inside `.var-meta` in `$var-header`.
- Use dialect logos (Clojure, ClojureScript, babashka) as small icons, ordered `clj` `cljs` `bb`.
- For in-scope vars: always show all three icons. Supported dialects are full opacity; unsupported dialects are dimmed. This communicates "we checked, it's absent" rather than leaving the user guessing.
- For out-of-scope vars (no compat data): show nothing (see "Omit badges for unknown" decision).

### Rationale
- Dialect support is var identity metadata — it belongs next to "Available since" and the namespace link, not in a separate section.
- Showing all three icons (with dimming) is more informative than omitting unsupported ones — users can see at a glance which dialects were checked and which support the var.
- Small logos are visually compact and recognizable to the Clojure community.

### Alternatives Considered
- Below arglists — too far from the var name; users scanning quickly would miss them.
- Sidebar — separated from var identity; sidebar is already used for namespace navigation.
- Omit unsupported icons — less informative; users can't distinguish "not supported" from "not checked."
- Text labels (`clj`, `cljs`, `bb`) instead of icons — functional but less visually appealing.

### Impacts and Risks
- Adds CSS for `.dialect-badge` styles (new Garden rules in `css.clj`).
- Need to source or create small dialect logo assets.
- Risk: visual clutter on var pages. Mitigation: icons are small, and dimmed icons have low visual weight.

### Links
- [pages/vars.clj $var-header](src/clj/clojuredocs/pages/vars.clj)
- [Feature proposal #4](docs/feature-proposals-q2-2026.md)

---

## 2026-04-28 — Omit badges for unknown dialect support

### Status
Decided

### Context
- Vars outside `clojure.core` and `clojure.string` (e.g., `core.async`, `core.logic`) have no dialect compatibility data yet.
- Need to decide what to show when the lookup returns `nil`.

### Decision
- Show nothing — no badges rendered when dialect support is unknown.

### Rationale
- Absence of badges honestly communicates "we haven't checked" without cluttering the UI.
- No risk of users misinterpreting a "?" badge as partial support.
- Clean: 700 vars get badges, ~100 vars (from other namespaces) don't.

### Alternatives Considered
- Show "?" badge — adds visual noise, could be confusing ("is it supported or not?").
- Show "unknown" label — takes significant horizontal space, no actionable information for the user.

### Impacts and Risks
- Users may not realize dialect info exists if they only visit out-of-scope var pages.
- Risk: low. The feature is most valuable for `clojure.core` and `clojure.string` vars, which are the most visited.

### Links
- [Issue #30](https://github.com/nubank/clojuredocs/issues/30)

---

## 2026-04-21 — Use CLJS compiler analyzer over API docs

### Status
Decided

### Context
- Needed an authoritative source for which `clojure.core` vars exist in ClojureScript.
- Initial approach consulted the CLJS API docs at cljs.github.io, which reported 980 vars.
- Discovery: the API docs page lists only `:defs` — it misses 191 macros imported from `cljs/core.clj`.

### Decision
- Use `cljs.analyzer.api/analyze-file` on the CLJS compiler source to extract both `:defs` and `:macros`.

### Rationale
- The compiler's analyzer state is the canonical source of what `cljs.core` provides — 1090 vars vs 980 from the API docs.
- Programmatic extraction is reproducible and version-pinned (tested with `org.clojure/clojurescript 1.12.134`).
- The 10% undercounting from API docs alone would produce incorrect compatibility badges.

### Alternatives Considered
- CLJS API docs (cljs.github.io) — misses macros; 980 vs 1090 vars (see errata #10 in planning doc).
- CLJS cheatsheet (cljs.info) — curated subset, not comprehensive, not machine-readable.
- Manual review of `cljs/core.cljs` source — would miss macros defined in `cljs/core.clj`.

### Impacts and Risks
- Requires `org.clojure/clojurescript` as a dependency in the generation script.
- Risk: analyzer API could change between CLJS versions. Mitigation: pin to specific CLJS version in the script.

### Links
- [Planning doc §1.1 methodology note](docs/research/issue-30-dialect-compat-planning.md)

---

## 2026-04-21 — Use bb ns-publics as babashka data source

### Status
Decided

### Context
- Needed an authoritative source for which `clojure.core` vars babashka supports.
- babashka documentation (book.babashka.org) does not maintain a var-by-var compatibility list.
- No machine-readable var list found in the babashka repository or documentation.

### Decision
- Use `bb -e '(keys (ns-publics (quote clojure.core)))'` on the installed bb binary as the source of truth.

### Rationale
- `ns-publics` reflects exactly what the running bb binary provides — 641 core + 21 string vars.
- Programmatic, reproducible, version-pinned (tested with bb 1.12.215).

### Alternatives Considered
- babashka documentation — no var-by-var listing exists.
- babashka source / var registry on GitHub — not consulted directly; `ns-publics` is simpler and authoritative.

### Impacts and Risks
- Requires bb to be installed when regenerating compatibility data.
- Risk: bb version drift means data goes stale. Mitigation: record bb version in generated file; regenerate with each bb release (~every 3 weeks based on observed cadence of 12 releases between Aug 2025 and Apr 2026).

### Links
- [Planning doc §1.2](docs/research/issue-30-dialect-compat-planning.md)
- [bb releases](https://github.com/babashka/babashka/releases)

---

## 2026-04-21 — Binary present/absent model for v1

### Status
Decided

### Context
- Some vars exist across dialects but may behave differently (e.g., `locking` in CLJS is a macro but JS has no threads; `pmap` in bb may not parallelize under SCI).
- Need to decide granularity: binary (yes/no), ternary (yes/partial/no), or richer.

### Decision
- Use binary present/absent for v1 — a var is either supported or not in a dialect.

### Rationale
- Binary is simplest to generate (var name lookup in `ns-publics` output), render (badge or no badge), and understand.
- Behavioral differences are known to affect at least 2 of 700 vars (`locking`, `pmap`) and have not been systematically cataloged (out of scope for v1).
- The data model (a set of dialect keywords per var) leaves room to add a future "partial" state without breaking anything.

### Alternatives Considered
- Ternary (supported/partial/unsupported) — requires defining "partial" per-var, which is subjective and labor-intensive.
- Rich behavioral metadata — out of scope; would require per-var documentation effort disproportionate to the feature's value.

### Impacts and Risks
- Some vars will show as "supported" when they behave differently than on JVM (e.g., `locking` in CLJS).
- Risk: users assume behavioral equivalence. Mitigation: documented as a known limitation in the planning doc; can add "partial" state later.

### Links
- [Planning doc, Risks #2](docs/research/issue-30-dialect-compat-planning.md)

---

## 2026-04-14 — Scope to clojure.core and clojure.string only

### Status
Decided

### Context
- ClojureDocs tracks 38 namespaces from `clojure-namespaces` in `search/static.clj`, spanning the core Clojure library and separate libraries (core.async, core.logic, data.csv, tools.build).
- Need to ship by end of April 2026 to gather learnings before a full site redesign informed by the 2026 Vision.
- Cross-dialect comparison is most meaningful for the standard library namespaces shared across dialects.

### Decision
- Scope the v1 compatibility index to `clojure.core` (679 vars) and `clojure.string` (21 vars) only — 700 vars total.

### Rationale
- These two namespaces contain the foundational standard library functions and are expected to be the most visited on ClojureDocs.
- End-of-April deadline: implementing for 700 vars is achievable; expanding to 38 namespaces requires researching each library's cross-dialect status individually.
- Learnings from this implementation will inform the broader site redesign described in the 2026 Vision.

### Alternatives Considered
- All 38 namespaces — would require researching cross-dialect support for core.async, core.logic, etc., which have different availability stories. Not achievable by end of April.
- Only `clojure.core` — misses `clojure.string`, which is trivial to include (identical in bb, 20/21 in CLJS).

### Impacts and Risks
- Vars from `core.async`, `core.logic`, etc. will have no dialect badges.
- Risk: users expect all namespaces to have badges. Mitigation: "omit badges for unknown" decision means graceful degradation — no badges is better than wrong badges.

### Links
- [2026 Vision](docs/2026vison.md) — site redesign context
- [Planning doc, Part 2 risk #4](docs/research/issue-30-dialect-compat-planning.md)
- [30-dialect-prompt.md](docs/research/30-dialect-prompt.md) — "April 27 deploy target"

---

## 2026-04-14 — Three initial dialects: Clojure/JVM, ClojureScript, babashka

### Status
Decided

### Context
- The 2026 Vision names four dialects: Clojure, ClojureScript, babashka, jank. This experiment does not include jank.
- jank has a [clojure-test-suite](https://github.com/jank-lang/clojure-test-suite) repo but is not yet stable enough for production compatibility data.
- Feature proposal #4 in `feature-proposals-q2-2026.md` specifies badges for dialect compatibility.

### Decision
- Start with three dialects: Clojure/JVM (baseline), ClojureScript, and babashka.

### Rationale
- These three have stable releases and programmatic data sources (verified 2026-04-21).
- jank's test suite is a future data source but the language is pre-1.0.
- The data model (set of keyword tags per var) naturally extends to additional dialects.

### Alternatives Considered
- Include jank — premature; no stable release, test suite is incomplete.
- Only Clojure/JVM + ClojureScript — misses babashka, which has 94.6% coverage and a large user base.

### Impacts and Risks
- jank users won't see compatibility info.
- Risk: none. Adding a dialect later is additive — new keyword in the set, regenerate the data file.

### Links
- [2026 Vision, cross-dialect section](docs/2026vison.md)
- [jank-lang/clojure-test-suite](https://github.com/jank-lang/clojure-test-suite)
- [Feature proposal #4](docs/feature-proposals-q2-2026.md)
