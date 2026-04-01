# Investigate User Behavior to Inform April Feature Ship

## Problem Statement

We have near-zero observability into how people use ClojureDocs.

| What we know | What we need to know |
|---|---|
| Page views exist in Matomo (unverified) | Which vars/namespaces drive traffic |
| Users submit examples, notes, see-alsos | What content is missing vs. over-served |
| Search exists client-side | What users search for, whether they find it |
| GitHub OAuth is the only auth | What % of visitors are logged in, what % contribute |
| The site gets traffic | Whether it's growing, shrinking, or flat |

We need to ship **one small, visible, additive feature by end of April**. That feature must be defensible with data, not require schema changes, and provide clear user value. This investigation produces the data to make that decision.

## Goals

1. Establish baseline metrics — traffic, top pages, session behavior, contribution rates
2. Identify user behavior patterns — how they arrive, what they do, where they leave
3. Determine content coverage — which vars have examples, how traffic correlates with content density
4. Quantify the contribution funnel — visitors → logged-in → contributors → repeat contributors (to the extent data allows)
5. Produce 3–5 candidate features with data-backed rationale for or against each

## Non-Goals

- Schema or data model changes
- Refactoring the analytics layer (beyond fixing dead code)
- Building a dashboard product
- Migrating away from Matomo or MongoDB
- Any change to authentication flow

## Prior Art

We have internal Datomic docs analytics work that provides reusable Matomo query patterns, dashboard structures, and analysis frameworks.

| Resource | What's reusable |
|---|---|
| **Dev Success metrics consolidation** (Google Sheet — internal, ask Jordan for access) | Monthly Matomo + GSC metrics template: visits, new vs returning, search vs direct, top pages, exit rates, visit frequency buckets. Copy dimensions for ClojureDocs. |
| **"Enable external Datomic sustainable growth" report** (Dec 2024 — internal, ask Jordan for access) | Narrative model for turning Matomo + Slack + support data into a usage story. Contains example metrics from docs.datomic.com (not ClojureDocs). |
| **clj-docs 🧠** (Google Sheet — internal, ask Jordan for access) | Existing ClojureDocs audit: data-model coupling analysis, multi-libs UX/SEO audit table with columns for Matomo insights / problems / proposals / user-confusion risk / implementation complexity. Matomo column is sparse — this investigation fills it. |

The Datomic work also demonstrates multi-source developer behavior analysis: Slack thread volume + doc-link %, Zendesk ticket themes, YouTube/events as awareness signals — all tied back to docs traffic. We can mirror this for ClojureDocs using Clojurians Slack and StackOverflow.

---

## Workstream A: Matomo Analytics

**Access**: `cognitect.matomo.cloud` (site ID 15) — confirmed.

Investigate whether Glean or Claude can assist with interpreting Matomo dashboards and extracting the deliverables below (vs. manual report pulling).

### A1. Validate Infrastructure

- [ ] Determine how far back Matomo data goes
- [ ] Confirm long-term availability of the Cognitect cloud instance
- [ ] Audit for legacy GA code — [`metrics.cljs`](../src/cljs/clojuredocs/metrics.cljs) calls `js/ga` (verify if functional); search event calls in [`search.cljs`](../src/cljs/clojuredocs/mods/search.cljs#L286) are commented out with `#_` (L286, L339, and a `go-loop` block ~L373)
- [ ] Assess data quality: bot contamination, ad-blocker impact, whether SPA navigation fires `trackPageView` on client-side route changes

**Current Matomo config** (from code audit — unverified against live instance):

| Configured | Not configured |
|---|---|
| `trackPageView` | Custom events |
| `enableLinkTracking` | Site search tracking |
| — | Goals / conversions |
| — | Custom dimensions (comment placeholder exists) |

Source: [`src/clj/clojuredocs/pages/common.clj` L17-28](../src/clj/clojuredocs/pages/common.clj#L17-L28)

**Custom dimensions to add** (front-end only, no schema change — mirrors Datomic docs pattern):

| Dimension | Values | Enables |
|---|---|---|
| `page_type` | `var`, `namespace`, `landing`, `search`, `quickref`, `concept` | Segment all metrics by page category |
| `library` | `clojure.core`, `tools.build`, etc. | Per-library traffic analysis |
| `auth_state` | `authenticated`, `anonymous` | Segment by logged-in vs. anonymous |
| `ns` + `var` | e.g. `clojure.core` / `map` | Join Matomo data with MongoDB by stable key |

### A2. Extract Traffic Data

Collect from Matomo built-in reports or Reporting API (see [Appendix A](#appendix-a-matomo-api-queries)).
Use the Dev Success metrics sheet as a template for which dimensions to pull.

- [ ] Top 20 landing pages with visit counts
- [ ] Referrer breakdown: direct vs. search engine vs. external site
- [ ] Top referring domains and search engine keywords
- [ ] Pages with >80% bounce rate (min 50 visits)
- [ ] Top exit pages
- [ ] Average session duration and pages-per-session distribution
- [ ] Top 10 page transition flows and drop-off points
- [ ] Top 20 outbound link targets (verify `enableLinkTracking` data exists first)
- [ ] Daily/weekly/monthly visit trends (period TBD by data availability)
- [ ] Desktop vs. mobile split, top countries, new vs. returning ratio
- [ ] Visit frequency distribution (1 / 2 / 3 / 4+ visits) — reuse Datomic pattern

### A3. Behavioral Segmentation

Apply Matomo segments (no backend changes) to distinguish user types:

| Segment | Definition | Purpose |
|---|---|---|
| **New vs. established** | New visitor (Matomo built-in) vs. returning with ≥3 visits | Distinguish onboarding from reference use |
| **Explorer vs. lookup** | Explorer: ≥3 pages/session, or used site search. Lookup: single-page session, high exit rate. | Decide where CTAs add value vs. add friction |
| **Library** | Custom dimension `library` (once instrumented) | Whether features benefit core vs. long-tail libs |

These mirror the behavioral segmentation used in the Datomic docs analysis.

---

## Workstream B: MongoDB Content Analysis

No access blockers — can run against the production database directly.

### B1. Content Inventory

- [ ] Total counts: examples (non-deleted), notes, see-alsos, example-histories
- [ ] Vars with zero examples (absolute count and % of total vars)
- [ ] Distribution: examples per var (mean, median, p90, max)
- [ ] Soft-deletion rate
- [ ] Verify whether a voting/rating system exists (none found in code audit — grep of `src/` for `vote|upvote|rating|score` returned zero relevant matches)

### B2. Content Popularity

- [ ] Top 20 vars by example count
- [ ] Top 20 vars by note count
- [ ] Top 20 vars by see-also count

### B3. Temporal Trends

- [ ] Monthly contribution volume (examples, notes, see-alsos) over full project history
- [ ] Activity spikes — attempt to correlate with Clojure releases, blog posts, conferences
- [ ] Characterize contribution rate trend
- [ ] New contributor rate over time (use earliest contribution as proxy if `users` lacks `created-at`)

### B4. Contributor Distribution

- [ ] Total unique contributors across all content types
- [ ] Contribution distribution analysis (80/20 or Gini)
- [ ] Top 20 contributors by volume
- [ ] Single-contribution users (% who contributed exactly once)
- [ ] Registered users who never contributed

See [Appendix B](#appendix-b-mongodb-queries) for all queries.

---

## Workstream C: Synthesis

Depends on A + B completion.

### C1. Cross-Reference (requires both Matomo and MongoDB data)

Join Matomo page data with MongoDB coverage stats by `ns/var` key. No schema change — read + aggregate offline (or via script that writes a static JSON).

Classify each var into one of four quadrants:

| | High content coverage | Low content coverage |
|---|---|---|
| **High traffic** | Top content (canonical) | **Under-served** (dead zone — high demand, low supply) |
| **Low traffic** | Over-documented (or niche) | Low priority |

- [ ] Produce the quadrant classification for all vars with traffic data
- [ ] Most viewed namespaces (aggregate var page traffic by namespace)
- [ ] Most-edited examples as quality proxy — do these correlate with traffic?

### C1b. External Signals (lightweight)

Mirror the Datomic docs pattern of cross-referencing with community channels:

- [ ] Spot-check Clojurians Slack and StackOverflow `[clojure]` for vars/topics that generate frequent questions but have weak ClojureDocs coverage
- [ ] Note any patterns (this is qualitative — not a comprehensive scrape)

### C2. Blind Spots

Document questions we cannot answer due to missing instrumentation:

| Question | Why we can't answer it |
|---|---|
| What do users search for? | No Matomo site search configured; [`metrics.cljs`](../src/cljs/clojuredocs/metrics.cljs) targets removed GA |
| Do users read examples or just skim? | No scroll/viewport tracking |
| What's the auth conversion rate? | No login/auth events tracked |
| How many visitors are logged in? | No custom dimension for auth state |
| Do users click see-also links? | No event tracking on see-also clicks |
| What's the example creation drop-off? | No contribution funnel events |

### C3. Proposed Instrumentation Improvements

All front-end only, no schema changes. Prioritized by unlock value.

| Priority | Change | What it unlocks |
|---|---|---|
| **P0** | Fix [`metrics.cljs`](../src/cljs/clojuredocs/metrics.cljs): replace `js/ga` → `_paq.push`; uncomment search tracking in [`search.cljs`](../src/cljs/clojuredocs/mods/search.cljs#L286) | Search query data (currently zero) |
| **P0** | Add Matomo site search: `trackSiteSearch(query, category, resultCount)` | Top queries, no-result queries, search → page conversion |
| **P1** | Add custom dimensions: `page_type`, `library`, `auth_state`, `ns`/`var` | Segment all metrics; join with MongoDB |
| **P1** | Add example interaction events: CTA click, editor open, preview, submit | Contribution funnel measurement |
| **P2** | Add see-also / outbound click events | Content navigation tracking |

### C4. Feature Evaluation

Evaluate each candidate against the data. The investigation may surface better alternatives.

| Candidate | What | Data needed to evaluate | Schema changes |
|---|---|---|---|
| **"Most Searched, Under-Served" panel** | Landing page block showing 5–10 vars with high search volume + low example coverage (Matomo SiteSearch × Mongo). Refreshes via offline script → static JSON. | Requires SiteSearch instrumentation first (P0). Then: search volume, coverage stats, whether the panel changes contribution or bounce behavior. | None |
| **"Needs Examples" CTA** | Badge on var pages with zero examples: "Help others: add an example." Track CTA clicks via `trackEvent`. | % of vars with zero examples; traffic to those vars; contribution rate. Measurable funnel: pageview → CTA click → example created (Mongo month-over-month). | None |
| **Search Improvements** | Result count display; fuzzy suggestions on zero results; Matomo search logging | No baseline search data exists — evaluate as instrumentation ship. Partially a prerequisite for the "Most Searched" panel. | None |
| **Example Edit History** | Diff timeline on `/ex/:id` page | `example-histories` collection volume; `/ex/:id` traffic | None |
| **Namespace Overview Stats** | Per-var content density indicators on `/:ns` pages | Namespace page traffic; whether they're entry points or pass-throughs | None |

The top two candidates ("Most Searched, Under-Served" and "Needs Examples" CTA) are complementary:
- The CTA can ship immediately using only MongoDB data.
- The panel requires SiteSearch instrumentation first, then a data collection period.
- Both produce measurable funnels: changes in example coverage for targeted vars (Mongo) and changes in bounce/exit rates for those pages (Matomo).

---

## Success Criteria

- [ ] Matomo data quality assessed (access confirmed)
- [ ] Baseline numbers: daily visits, top 20 pages, bounce rate, session duration, referrer mix
- [ ] Content inventory complete: totals, zero-content vars, distribution stats
- [ ] Contribution funnel quantified (even if approximate)
- [ ] At least 3 feature candidates evaluated with data
- [ ] One feature selected for April ship with definition of done

## Risks

Likelihood and impact are unknown until the investigation begins.

| Risk | What to check | Mitigation |
|---|---|---|
| Matomo has no data (never worked, or stopped) | Check whether site ID 15 has data; determine how far back | Fall back to MongoDB-only + server access logs (nginx) |
| `created-at` is epoch millis not Date | Inspect a sample document | Use `{ $toDate: "$created-at" }` |
| Bot traffic in Matomo | Check visitor logs | Filter by duration >0s |
| `users` collection lacks `created-at` | Inspect schema | Use earliest contribution as proxy |
| Ad-blockers suppress Matomo JS | Compare server logs to Matomo volume | Note underreporting factor |
| SPA navigation not tracked | Check client-side route behavior | Document gap |

## Labels

`investigation`, `analytics`, `roadmap`, `data`, `april-ship`

---

## Appendix A: Matomo API Queries

All queries assume `idSite=15` and `format=JSON`.

```
# Entry pages
Actions.getEntryPageUrls  &period=range&date=last90&filter_limit=50

# Referrers
Referrers.getAll          &period=range&date=last90
Referrers.getKeywords     &period=range&date=last90&filter_limit=50
Referrers.getWebsites     &period=range&date=last90&filter_limit=30

# Page behavior
Actions.getPageUrls       &period=range&date=last90&filter_sort_column=bounce_rate&filter_sort_order=desc&filter_limit=50
Actions.getExitPageUrls   &period=range&date=last90&filter_limit=30
Actions.getPageUrls       &period=range&date=last90&filter_limit=100&flat=1
Actions.getOutlinks       &period=range&date=last90&filter_limit=30

# Session engagement
VisitorInterest.getNumberOfVisitsPerVisitDuration  &period=range&date=last90
VisitorInterest.getNumberOfVisitsPerPage           &period=range&date=last90
Transitions.getTransitionsForAction                &period=range&date=last90&actionType=url&actionName=clojuredocs.org/clojure.core/map

# Trends
VisitsSummary.getVisits    &period=day&date=last365

# Demographics
DevicesDetection.getType      &period=range&date=last90
DevicesDetection.getBrowsers  &period=range&date=last90
UserCountry.getCountry        &period=range&date=last90&filter_limit=20
VisitFrequency.get            &period=range&date=last90
```

## Appendix B: MongoDB Queries

```javascript
// --- Content Inventory ---
db.examples.countDocuments({ "deleted-at": null })
db.notes.countDocuments({})
db["see-alsos"].countDocuments({})
db["example-histories"].countDocuments({})
db.examples.countDocuments({ "deleted-at": { $ne: null } })

// --- Top vars by content count ---
// Examples
db.examples.aggregate([
  { $match: { "deleted-at": null } },
  { $group: { _id: { ns: "$var.ns", name: "$var.name" }, count: { $sum: 1 } } },
  { $sort: { count: -1 } },
  { $limit: 20 }
])
// Notes — same pattern with db.notes, grouping on $var.ns/$var.name
// See-alsos — same pattern with db["see-alsos"], grouping on $from-var.ns/$from-var.name

// --- Distribution stats ---
db.examples.aggregate([
  { $match: { "deleted-at": null } },
  { $group: { _id: { ns: "$var.ns", name: "$var.name" }, count: { $sum: 1 } } },
  { $group: {
      _id: null,
      avg: { $avg: "$count" },
      max: { $max: "$count" },
      total_vars_with_examples: { $sum: 1 }
  }}
])

// --- Most-edited examples ---
db["example-histories"].aggregate([
  { $group: { _id: "$example-id", edit_count: { $sum: 1 }, editors: { $addToSet: "$editor.login" } } },
  { $match: { edit_count: { $gt: 1 } } },
  { $sort: { edit_count: -1 } },
  { $limit: 20 }
])

// --- Temporal trends (per content type) ---
// Pattern: project created-at → month string, group by month, sort ascending
db.examples.aggregate([
  { $match: { "deleted-at": null } },
  { $project: { month: { $dateToString: { format: "%Y-%m", date: { $toDate: "$created-at" } } } } },
  { $group: { _id: "$month", count: { $sum: 1 } } },
  { $sort: { _id: 1 } }
])
// Repeat for db.notes and db["see-alsos"]

// --- Contributor distribution ---
db.examples.aggregate([
  { $match: { "deleted-at": null } },
  { $group: { _id: "$author.login", count: { $sum: 1 } } },
  { $sort: { count: -1 } }
])
// Compute: unique authors, top 10 as % of total, single-contribution %

// --- Registered vs. active ---
db.users.countDocuments({})
// Compare to distinct author.login across examples + notes + see-alsos

// --- New contributors over time ---
db.users.aggregate([
  { $lookup: { from: "examples", localField: "login", foreignField: "author.login", as: "exs" } },
  { $project: { login: 1, first: { $min: "$exs.created-at" } } },
  { $match: { first: { $ne: null } } },
  { $project: { month: { $dateToString: { format: "%Y-%m", date: { $toDate: "$first" } } } } },
  { $group: { _id: "$month", count: { $sum: 1 } } },
  { $sort: { _id: 1 } }
])
```
