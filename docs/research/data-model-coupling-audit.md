# Data Model Coupling Audit

> **AI Disclaimer**: This research was conducted and drafted by Claude (Opus 4.6) via GitHub Copilot in VS Code. Trust nothing — AI-generated content may contain false statements. All code references should be verified against the repository at the time of reading.

**Date**: 2026-03-11
**Branch**: `research/4/problem-statement-audit`
---

## Purpose

Evaluate the accuracy of three candidate problem statements about the ClojureDocs data model by inspecting the repository and providing concrete evidence from the code.

## Research Context

- **Model**: Claude Opus 4.6 (via GitHub Copilot + Calva w Clojure MPC Server), applied "research-analyst" agent stolen from stu-ai-projects
- **Token-intensive context**: ~15 source files read across `src/clj/`, `src/cljs/`, and `src/cljc/` directories; ~3,500 lines of code inspected
- **Search patterns used**: `schema.core|s/Str|s/Int|s/Any`, `:library-url`, `:var\.ns|:var\.name|:from-var|:to-var`, plus broad file-structure and content exploration
- **Key files examined**:
  - `src/clj/clojuredocs/api/common.clj` — partial Prismatic Schema definitions
  - `src/clj/clojuredocs/api/examples.clj` — example entity schemas and handlers
  - `src/clj/clojuredocs/api/notes.clj` — note entity schemas and handlers
  - `src/clj/clojuredocs/api/see_alsos.clj` — see-also schemas (with duplicates)
  - `src/clj/clojuredocs/api/server.clj` — API middleware and routing
  - `src/clj/clojuredocs/search.clj` — Lucene indexing and var ingestion
  - `src/clj/clojuredocs/search/static.clj` — hardcoded namespace/special-form data
  - `src/clj/clojuredocs/data.clj` — MongoDB query functions
  - `src/clj/clojuredocs/data/import.clj` — duplicated var ingestion (in comment block)
  - `src/clj/clojuredocs/export.clj` — JSON export logic
  - `src/clj/clojuredocs/pages/vars.clj` — var page rendering and data assembly
  - `src/clj/clojuredocs/pages.clj` — search result enrichment
  - `src/clj/clojuredocs/pages/gh_auth.clj` — user construction at login
  - `src/clj/clojuredocs/pages/common.clj` — shared page rendering
  - `src/clj/clojuredocs/pages/quickref.clj` — quick reference page
  - `src/clj/clojuredocs/main.clj` — MongoDB index setup
  - `src/cljs/clojuredocs/mods/var_page.cljs` — client-side var page state management
  - `src/cljs/clojuredocs/mods/search.cljs` — client-side search autocomplete
  - `src/cljs/clojuredocs/examples.cljs` — client-side example rendering
  - `src/cljs/clojuredocs/notes.cljs` — client-side note rendering
  - `src/cljs/clojuredocs/see_alsos.cljs` — client-side see-also rendering
  - `src/cljc/clojuredocs/util.cljc` — shared utilities
  - `project.clj` — dependency list

---

## Prompt

```
You are performing a technical audit of the ClojureDocs codebase.

Goal
-----
Evaluate the accuracy of the following problem statement by inspecting the
repository and providing concrete evidence from the code.

Problem Statements to consider
-----------------
"The ClojureDocs data model is implicitly encoded throughout business logic
rather than defined as an explicit schema or abstraction. As a result,
extending or modifying the model requires changes across multiple parts of
the system, increasing complexity, contributor friction, and the risk of
brittle behavior."

"The ClojureDocs data model is embedded in application business logic rather
than defined as a separate abstraction, making the system difficult to
extend and prone to breakage when the model changes."

"ClojureDocs encodes its data model implicitly across multiple areas of
application logic instead of defining it in a single explicit schema or
abstraction layer. This tight coupling makes the system difficult to evolve
and increases the risk of breakage when introducing new data structures or
features."


Your Task
---------
Investigate the codebase to determine which of these claims is true,
partially true, or false. Choose a problem statement that is the most
correct.

Focus specifically on whether the data model is:

- implicitly encoded across functions and namespaces
- duplicated in multiple places
- coupled to business logic
- difficult to extend without touching many files

Steps
-----

1. Identify the core domain entities used by ClojureDocs.
   Examples may include:
   - library
   - var
   - example
   - docstring
   - author
   - namespace

2. Locate where these entities are defined or constructed.
   Look for:
   - maps representing entities
   - destructuring patterns
   - keys used across different namespaces
   - Lucene indexing structures
   - JSON serialization
   - search indexing logic
   - page rendering logic

3. Determine whether a canonical schema exists.
   Search for things like:
   - spec
   - malli
   - schema definitions
   - typed structures
   - central data definitions

4. If no explicit schema exists, find examples where the same structure is
   reconstructed in multiple places.

5. Identify locations where adding a new field to an entity would require
   modifying multiple parts of the codebase.

6. Document examples of implicit coupling between:
   - search indexing
   - storage format
   - rendering logic
   - API responses

Output Format
-------------

Provide a structured report with the following sections:

1. Summary Assessment
   - Is the problem statement accurate? (Yes / Partially / No)
   - Short explanation.

2. Evidence of Implicit Data Model
   Table:

   Entity | File | Code Pattern | Description
   ------ | ---- | ------------ | -----------
   var | search.clj | map construction | var fields embedded in indexing logic
   library | static.clj | hardcoded config | library metadata encoded in config

3. Example Code Snippets
   Show concrete examples where data structures are reconstructed or
   assumptions about fields appear.

4. Change Impact Example
   Hypothetical example:
   "If we added field `:since-version` to vars, these files would likely
   need changes."

5. Architectural Observations
   - patterns of coupling
   - areas where brittleness may occur
   - whether the system relies on implicit contracts

6. Candidate Locations for a Central Schema
   Suggest places where a schema layer could be introduced.

Important Constraints
---------------------

Do not speculate. Only report conclusions supported by code examples.

When making claims, include:
- file paths
- function names
- code snippets
- explanation of why this indicates implicit schema coupling.

The goal is to produce evidence that either supports or falsifies the
problem statements.
```

---

## Response

### 1. Summary Assessment

**Is the problem statement accurate?** **Partially — the third statement is the most correct, with a nuance.**

The third statement — *"ClojureDocs encodes its data model implicitly across multiple areas of application logic instead of defining it in a single explicit schema or abstraction layer"* — is the most accurate, but it overstates the situation in one respect: Prismatic Schema definitions **do** exist for API write paths. However, these schemas are:

- **local** to individual API handler namespaces, not shared
- **duplicated** across namespaces (e.g., `User` and `Var` are defined identically in both `api.common` and `api.see-alsos`)
- **absent** from the read path, rendering, search indexing, export, and client-side code

The data model is **partially** formalized (write-side API validation) and **mostly** implicit (everywhere else). The core entities — var, example, note, see-also, user — have no single authoritative definition. Their shapes are reconstructed via `select-keys`, destructuring, and map construction across ~15 files spanning server and client.

---

### 2. Evidence of Implicit Data Model

#### 2.1 Schema Duplication — `User` and `Var` Defined in Multiple Places

**`User` is defined identically in two separate namespaces:**

`src/clj/clojuredocs/api/common.clj` (lines 8-11):
```clojure
(def User
  {:login s/Str
   :account-source s/Str
   :avatar-url s/Str})
```

`src/clj/clojuredocs/api/see_alsos.clj` (lines 12-15):
```clojure
(def User
  {:login s/Str
   :account-source s/Str
   :avatar-url s/Str})
```

**`Var` is also defined identically in two places:**

`src/clj/clojuredocs/api/common.clj` (lines 13-16):
```clojure
(def Var
  {:ns s/Str
   :name s/Str
   :library-url s/Str})
```

`src/clj/clojuredocs/api/see_alsos.clj` (lines 17-20):
```clojure
(def Var
  {:ns s/Str
   :name s/Str
   :library-url s/Str})
```

The `see_alsos` namespace defines its own `User` and `Var` locally instead of referencing `c/User` and `c/Var` from `api.common`, which it already requires. This is classic schema duplication — the kind that drifts silently.

#### 2.2 `var-keys` Defined Twice

The list of var metadata keys is defined identically in two files:

`src/clj/clojuredocs/search.clj` (lines 20-36):
```clojure
(def var-keys
    [:ns :name :file :column :line :added :arglists :doc
     :static :tag :macro :dynamic :special-form :forms
     :deprecated :url :no-doc])
```

`src/clj/clojuredocs/data/import.clj` (lines 6-22, inside a `comment` block, but representing the original source):
```clojure
(def var-keys
    [:ns :name :file :column :line :added :arglists :doc
     :static :tag :macro :dynamic :special-form :forms
     :deprecated :url :no-doc])
```

Both files also duplicate `transform-var-meta`, `cond-update-in`, `gather-var`, and `gather-vars` — the entire var ingestion pipeline is copy-pasted.

#### 2.3 Entity Shape Encoding Across Layers

| Entity | File | Code Pattern | Description |
|--------|------|-------------|-------------|
| **Var** | `src/clj/clojuredocs/api/common.clj` (L13-16) | `(def Var {:ns s/Str :name s/Str :library-url s/Str})` | Prismatic Schema for API validation |
| **Var** | `src/clj/clojuredocs/api/see_alsos.clj` (L17-20) | `(def Var {:ns …})` — duplicate | Identical schema, not imported |
| **Var** | `src/clj/clojuredocs/search.clj` (L20-36) | `(def var-keys [:ns :name …])` | Key list for `select-keys` during indexing |
| **Var** | `src/clj/clojuredocs/search.clj` (L80-84) | `(assoc % :library-url …) (assoc % :type …) (assoc % :href …)` | Ad-hoc enrichment during gathering |
| **Var** | `src/clj/clojuredocs/pages/vars.clj` (L103-107) | `{:keys [arglists name ns doc …]}` | Destructuring in page handler |
| **Var** | `src/clj/clojuredocs/data.clj` (L6) | `{:keys [ns name library-url]}` | Query parameter destructuring |
| **Var** | `src/cljs/clojuredocs/mods/var_page.cljs` (L118) | `(select-keys (:var state) [:ns :name :library-url])` | Client-side var identity extraction |
| **Var** | `src/clj/clojuredocs/pages.clj` (L136) | `(select-keys % [:ns :name :library-url])` | Search results see-also enrichment |
| **Example** | `src/clj/clojuredocs/api/examples.clj` (L43-49) | `(def InsertExample {:author c/User :body s/Str …})` | Write-path schema |
| **Example** | `src/clj/clojuredocs/data.clj` (L6-12) | `mon/fetch :examples :where {:var.name … :var.ns …}` | MongoDB query with dot-notation paths |
| **Example** | `src/clj/clojuredocs/pages/vars.clj` (L87-89) | `(mapv clean-example examples)` | Example cleaning for page data serialization |
| **Example** | `src/cljs/clojuredocs/examples.cljs` (L98-99) | `{:keys [can-delete? can-edit?]}` then `{:keys [body editing? _id]}` | Client-side destructuring |
| **Note** | `src/clj/clojuredocs/api/notes.clj` (L11-17) | `(def Note {:body s/Str :var c/Var :author c/User …})` | Write-path schema |
| **Note** | `src/clj/clojuredocs/data.clj` (L16-21) | `mon/fetch :notes :where {:var.ns …}` | Query with implicit field assumptions |
| **Note** | `src/cljs/clojuredocs/mods/var_page.cljs` (L252) | `:var (select-keys (:var state) [:ns :name :library-url])` | Client-side var extraction for note creation |
| **SeeAlso** | `src/clj/clojuredocs/api/see_alsos.clj` (L22-27) | `(def SeeAlso {:from-var Var :to-var Var :author User …})` | Write-path schema |
| **SeeAlso** | `src/clj/clojuredocs/pages/vars.clj` (L35-38) | `mon/fetch :see-alsos :where {:from-var.ns …}` | Direct MongoDB query in rendering |
| **SeeAlso** | `src/cljs/clojuredocs/see_alsos.cljs` (L15-22) | `{:keys [from-var to-var created-at doc author can-delete? …]}` | Client-side destructuring |
| **SeeAlso** | `src/cljs/clojuredocs/mods/var_page.cljs` (L396) | `:from-var (select-keys (:var state) [:ns :name :library-url])` | Client-side map construction for API call |
| **User** | `src/clj/clojuredocs/pages/gh_auth.clj` (L9-12) | `{:avatar-url avatar_url :account-source "github" :login login}` | User construction at authentication |
| **User** | `src/clj/clojuredocs/api/common.clj` (L8-11) | `(def User {:login s/Str …})` | Schema definition |
| **User** | `src/cljs/clojuredocs/examples.cljs` (L100) | `(select-keys user [:login :account-source])` | Client-side identity comparison |
| **Library** | `src/clj/clojuredocs/search.clj` (L101-106) | `{:library-url "…" :version "1.12.4" :source-base-url "…" …}` | Hardcoded library config |
| **MongoDB indexes** | `src/clj/clojuredocs/main.clj` (L50-77) | `add-indexes-to-coll!` calls | Index setup references entity fields by string paths |

---

### 3. Example Code Snippets

#### 3.1 Var Identity Reconstructed via `select-keys` in 7+ Locations

The "identity" of a var — `[:ns :name :library-url]` — is never defined as a concept. Instead, `select-keys` with this exact triple appears scattered through the codebase:

**Server — API handler** (`src/clj/clojuredocs/api/see_alsos.clj`, L42-45):
```clojure
new-see-also {:from-var (select-keys from-var [:ns :name :library-url])
              :to-var (select-keys to-var [:ns :name :library-url])
```

**Server — search enrichment** (`src/clj/clojuredocs/pages.clj`, L136):
```clojure
(map #(select-keys % [:ns :name :library-url]))
```

**Client — example creation** (`src/cljs/clojuredocs/mods/var_page.cljs`, L118):
```clojure
(let [var (select-keys (:var state) [:ns :name :library-url])
```

**Client — see-also dedup** (`src/cljs/clojuredocs/mods/var_page.cljs`, L342-343):
```clojure
(= (select-keys current-var [:ns :name :library-url])
   (select-keys ac-result [:ns :name :library-url]))
```

The same three-key pattern appears in at least 7 distinct call sites. If `:library-url` were renamed or a fourth identity key added, each site must be found and updated manually.

#### 3.2 User Identity Comparison — Implicit Contract

User equality is checked via `select-keys` with implicit field agreement:

**Server** (`src/clj/clojuredocs/api/examples.clj`, L26):
```clojure
(let [eds (map #(select-keys % [:login :account-source]) editors)]
  (if (get (set eds) (select-keys user [:login :account-source]))
```

**Client** (`src/cljs/clojuredocs/examples.cljs`, L100-101):
```clojure
(defn user-can-delete? [user {:keys [author]}]
  (= (select-keys user [:login :account-source])
     (select-keys author [:login :account-source])))
```

The server and client independently agree that `[:login :account-source]` constitutes user identity. This is an implicit contract — if `gh_auth.clj` started including `:id` or changed `:account-source`, both sides would need updating.

#### 3.3 MongoDB Dot-Notation Encodes Document Shape

MongoDB queries in `src/clj/clojuredocs/data.clj` embed assumptions about nested document structure through dot notation:

```clojure
(mon/fetch :examples
  :where {:var.name name
          :var.ns ns
          :var.library-url library-url
          :deleted-at nil})
```

And independently in `src/clj/clojuredocs/pages/vars.clj` (L35-38):
```clojure
(mon/fetch :see-alsos
  :where {:from-var.ns ns
          :from-var.name name
          :from-var.library-url library-url})
```

These queries hardcode knowledge that examples store a nested `:var` map and see-alsos store nested `:from-var` / `:to-var` maps. The same knowledge is encoded in the MongoDB index setup (`src/clj/clojuredocs/main.clj`, L55-73).

---

### 4. Change Impact Example

**Hypothetical: Add `:since-version` field to vars.**

Files requiring changes:

| File | Reason |
|------|--------|
| `src/clj/clojuredocs/search.clj` (L20-36) | Add `:since-version` to `var-keys` so it's preserved during `transform-var-meta` |
| `src/clj/clojuredocs/search.clj` (L107-114) | Decide if `searchable-vars` should index it |
| `src/clj/clojuredocs/search/static.clj` (L42-130) | Add to special-forms if applicable |
| `src/clj/clojuredocs/pages/vars.clj` (L103-118) | Add to `$var-header` rendering and `var-page-handler` destructuring |
| `src/clj/clojuredocs/pages.clj` (L125-170) | Add to search result handler if surfaced in search |
| `src/clj/clojuredocs/export.clj` (L14-32) | Ensure included in JSON export |
| `src/cljs/clojuredocs/mods/search.cljs` (L47-68) | Render in autocomplete entry if desired |
| `src/cljs/clojuredocs/mods/var_page.cljs` | Surface in client-side var rendering |
| `src/clj/clojuredocs/data/import.clj` (L6-22) | Add to duplicated `var-keys` in comment block |

A single new field on the var entity touches **at least 8-9 files** across 3 layers (ingestion, storage+API, rendering). No central schema change propagates automatically.

**Hypothetical: Rename `:library-url` to `:source-url`.**

This would require changes in **every file listed in Section 2.3** that uses `:library-url` — at minimum 15 distinct call sites across 10 files, spanning both Clojure and ClojureScript, plus MongoDB index definitions and existing MongoDB documents.

---

### 5. Architectural Observations

#### Patterns of Coupling

1. **Implicit identity contracts.** Var identity (`[:ns :name :library-url]`) and user identity (`[:login :account-source]`) are defined by convention, not abstraction. Every site that compares or extracts these identities re-specifies the key set.

2. **Schema duplication across API handlers.** Despite having `api.common` with `User` and `Var` schemas, the `see_alsos` namespace redefines them locally. The `examples` and `notes` namespaces correctly use `c/User` and `c/Var`, but `see_alsos` diverges — showing that even the partial centralization has drifted.

3. **MongoDB shape encoded in queries.** The nested document structure (`:var.name`, `:from-var.ns`, `:author.login`) is hardcoded in queries, index definitions, and `select-keys` calls independently. Nothing enforces that these locations agree.

4. **No schema on the read path.** Prismatic Schema is used only for API write validation. The read path — queries in `data.clj`, page handlers in `pages/vars.clj`, search enrichment in `pages.clj`, export in `export.clj`, and all ClojureScript rendering — operates on raw maps with no validation or shape declaration.

5. **Cross-boundary implicit contracts (server ↔ client).** The server constructs `:page-data` in `src/clj/clojuredocs/pages/vars.clj` (L115-120) with specific keys (`:examples`, `:var`, `:notes`, `:see-alsos`, `:user`). The client reads these in `src/cljs/clojuredocs/mods/var_page.cljs` (L77) via `page/page-data`. This serialization boundary has no shared schema — the two sides must agree by convention.

#### Areas of Brittleness

- **Var ingestion pipeline** is duplicated between `search.clj` and `data/import.clj`, so changes to var transformation logic must be applied twice.
- **MongoDB index setup** in `main.clj` references field paths that must match the document shapes created by API handlers. Nothing links these.
- **User construction** in `gh_auth.clj` must produce maps compatible with `api.common/User` schema, but doesn't reference it.

---

### 6. Candidate Locations for a Central Schema

> **Note**: These are observations about where centralization *could* happen, not recommendations to act on. Included for context only.

1. **`src/clj/clojuredocs/schema.clj`** (hypothetical) — A single namespace defining `Var`, `User`, `Example`, `Note`, `SeeAlso`, and `Library` schemas. All API handlers, data access, and export code would import from here. This replaces the current `api/common.clj` partial definitions and eliminates the duplicate in `api/see_alsos.clj`.

2. **`src/cljc/clojuredocs/schema.cljc`** (hypothetical, cross-platform) — If schemas need to be shared with ClojureScript (e.g., for client-side validation or documentation), a `.cljc` file using either Prismatic Schema or a data-literal approach would serve both sides.

3. **`src/clj/clojuredocs/api/common.clj`** (existing) — Already contains `User` and `Var`. Could be expanded to include all entity schemas and identity-extraction functions (e.g., `(defn var-identity [v] (select-keys v [:ns :name :library-url]))`). This is the lowest-effort path.

4. **Extract var ingestion** — Consolidate the duplicated `var-keys`, `transform-var-meta`, `gather-var`, and `gather-vars` from `search.clj` and `data/import.clj` into a single namespace that both import from.

---

## Core Domain Entities Identified

For future reference, Claude identified these as the core domain entities in ClojureDocs:

| Entity | MongoDB Collection | Identity Keys | Write Schema Exists? |
|--------|--------------------|---------------|---------------------|
| **Var** | (in-memory, from ns metadata) | `[:ns :name :library-url]` | `api/common.clj` + duplicate in `api/see_alsos.clj` |
| **Example** | `:examples` | `[:_id]` | `api/examples.clj` (`InsertExample`, `UpdateExample`) |
| **Note** | `:notes` | `[:_id]` | `api/notes.clj` (`Note`) |
| **SeeAlso** | `:see-alsos` | `[:_id]` | `api/see_alsos.clj` (`SeeAlso`) |
| **User** | `:users` | `[:login :account-source]` | `api/common.clj` (`User`) + duplicate in `api/see_alsos.clj` |
| **Library** | `:libraries` | `[:library-url]` | None |
| **Namespace** | `:namespaces` | `[:name]` | None |
| **ExampleHistory** | `:example-histories` | `[:_id]` | `api/examples.clj` (`InsertExampleHistory`) |

---

## Limitations

- Claude did not run the application or verify MongoDB document shapes against a live database. All claims about document structure are inferred from query patterns and map construction in source code.
- The `data/import.clj` file's var ingestion code is inside a `(comment ...)` block, meaning it is dead code. Claude flagged it as duplication because it represents the original source that `search.clj` was likely copied from, but it is not executed.
- Claude did not verify whether the Prismatic Schema validation in API handlers is actually invoked at runtime (e.g., whether `validate-schema!` is called in all code paths).
- Claude did not search Git history to determine when the `see_alsos.clj` schema duplication was introduced or whether it was intentional.
- No independent code review by a human was performed.

---

## Version History

| Date | Summary |
|------|---------|
| 2026-03-11 | Initial research document created from technical audit session. |
