# Multi-Library :library-url Dependency Chain

This diagram shows how the `:library-url` property for each var is handled in the current clojuredocs.org architecture. Each layer is labeled in ALL CAPS, with file references below.

```mermaid
flowchart TB
  subgraph APP_LAYERS [APP LAYERS]
    direction TB
    A[STATIC CONFIG\nsearch/static.clj\nFlat list of namespaces, no library grouping]
    B[LIBRARY DEFINITION\nsearch.clj\nSingle clojure-lib map, :library-url hardcoded]
    C[IN-MEMORY VARS\nsearch.clj\ngather-vars, lookup-vars\n:library-url stamped on every var]
    D[LIBRARY RESOLUTION\npages/vars.clj, pages/nss.clj\nlibrary-for always returns clojure-lib]
    E[URL GENERATION & ROUTING\nutil.cljc, pages.clj\nvar-path, two-segment URLs]
    F[PAGE RENDERING\npages/common.clj, pages/vars.clj\nlibrary-nav, ns-tree, source-url]
  end

  subgraph PERSISTENCE [PERSISTENCE]
    direction TB
    G[MONGODB\ndata.clj\nQueries filter on :library-url\nMulti-lib ready, but always gets wrong data]
  end

  A --> B --> C --> D --> E --> F --> G

  classDef red stroke:#d32f2f,stroke-width:3px,fill:#fff,color:#111;
  classDef green stroke:#388e3c,stroke-width:3px,fill:#fff,color:#111;
  class A,B,C,D,E,F red;
  class G green;
```

## File References

- **search/static.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search/static.clj
- **search.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/search.clj
- **pages/vars.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/vars.clj
- **pages/nss.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/nss.clj
- **util.cljc:** https://github.com/nubank/clojuredocs/blob/master/src/cljc/clojuredocs/util.cljc
- **pages.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages.clj
- **pages/common.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/pages/common.clj
- **data.clj:** https://github.com/nubank/clojuredocs/blob/master/src/clj/clojuredocs/data.clj

## Context

- **Purpose:** This diagram traces how the `:library-url` property (which should identify the source library for each var) is handled from static config through to database queries.
- **Problem:** All layers except MongoDB treat the system as if there is only one library (Clojure itself). The `:library-url` is hardcoded and stamped on every var, even those from other libraries (e.g., `core.async`, `core.logic`).
- **Impact:** All vars, regardless of their true origin, are labeled as coming from the Clojure repo. MongoDB is capable of supporting multiple libraries, but always receives the wrong `:library-url` due to upstream design.
- **Next Steps:** To support true multi-library documentation, each red layer must be refactored to track and propagate the correct library for each var, and URLs/routes must be extended to disambiguate vars from different libraries.

**For more details, see [Issue #4: Multi-Library Support Audit](https://github.com/nubank/clojuredocs/issues/4).**

