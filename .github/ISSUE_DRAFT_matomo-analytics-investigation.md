# Investigate Matomo Analytics to Inform Project Strategy and Roadmap

## Summary

Audit and enhance ClojureDocs' Matomo analytics integration to understand how users interact with the site. The data should inform long-term strategy, feature prioritization, and the project roadmap.

## Problem

We currently have minimal analytics coverage. After replacing Google Analytics with Matomo ([CHANGELOG](../CHANGELOG.md)), only basic page view and outbound link tracking were configured. Meanwhile, the client-side search event tracking (`metrics.cljs`) still targets the now-removed Google Analytics (`js/ga`) and all calls to it are commented out. **We are effectively flying blind on user behavior beyond page views.**

## Current State (Code Audit)

| What | Status | Location |
|------|--------|----------|
| Matomo page view tracking | **Active** — `trackPageView` | `src/clj/clojuredocs/pages/common.clj` L17-29 |
| Matomo link tracking | **Active** — `enableLinkTracking` | same |
| Matomo custom events | **Not configured** | — |
| Matomo site search | **Not configured** | — |
| Matomo goals/conversions | **Not configured** | — |
| Matomo custom dimensions | **Not configured** (comment references them) | — |
| Client-side search tracking | **Dead code** — calls `js/ga` which no longer exists | `src/cljs/clojuredocs/metrics.cljs` |
| Search event calls | **Commented out** (`#_`) | `src/cljs/clojuredocs/mods/search.cljs` L286, L339, L369 |
| Matomo instance | Cognitect-hosted cloud (`cognitect.matomo.cloud`, site ID 15) | — |

## Tasks

### Phase 1: Validate Existing Analytics

- [ ] **Confirm Matomo is receiving data** — Log into `cognitect.matomo.cloud` and verify site ID 15 is actively collecting page views. Determine how far back data goes.
- [ ] **Assess access and ownership** — Confirm who has admin access to the Matomo instance. Determine if the Cognitect cloud instance will remain available long-term or if we need to self-host / migrate.
- [ ] **Review built-in reports** — Inventory what Matomo is already telling us out of the box (top pages, referrers, geography, devices, bounce rate, visit duration, etc.)
- [ ] **Identify data gaps** — Compare what Matomo provides by default against the questions we need answered (see "Strategic Questions" below).

### Phase 2: Fix Broken / Dead Tracking Code

- [ ] **Remove or replace `metrics.cljs` GA dead code** — The `ga-event`, `track-search`, and `track-search-choose` functions call `js/ga` which no longer exists. Either rewrite them to use Matomo's `_paq.push()` API or remove the file.
- [ ] **Uncomment or re-implement search tracking** — The `#_` reader-macro'd calls in `search.cljs` (L286, L339, L369) represent valuable search behavior data. Re-implement using Matomo's site search tracking or custom events.

### Phase 3: Expand Tracking Coverage

Based on the strategic questions below, consider adding:

- [ ] **Site Search tracking** — Use Matomo's built-in site search feature (`_paq.push(['trackSiteSearch', keyword, category, resultsCount])`) to capture what users search for, whether they find results, and which results they click.
- [ ] **Contribution flow events** — Track when users add/edit examples, notes, and see-alsos (and where they drop off if they don't complete the flow).
- [ ] **Authentication events** — Track GitHub login initiation vs. completion to understand friction in the auth flow.
- [ ] **Content engagement** — Track scroll depth or time-on-page for var documentation pages vs. namespace listing pages.
- [ ] **Custom dimensions** — Consider tracking: logged-in vs. anonymous, Clojure version context, namespace category.
- [ ] **Goals** — Define conversion goals (e.g., "user contributed an example", "user completed a search and visited a var page").

### Phase 4: Report and Synthesize

- [ ] **Build a baseline dashboard** — Create a Matomo dashboard (or export) with the key metrics that will be monitored over time.
- [ ] **Document findings** — Write up what the data tells us about current usage patterns, and where the biggest opportunities lie.
- [ ] **Feed into roadmap** — Use the data to prioritize roadmap items (e.g., if 40% of searches return no results, improving search is high-impact).

## Strategic Questions Analytics Should Answer

These are the questions that should guide what we track:

1. **What are users looking for?** — Top search queries, no-result queries, search-to-page-view conversion.
2. **Where do users spend time?** — Which namespaces/vars get the most traffic? Are users reading examples, notes, or see-alsos?
3. **How do users arrive?** — Referrer breakdown (search engines, direct, Clojure docs, blogs, Stack Overflow).
4. **What is the contribution funnel?** — How many visitors are logged in? Of those, how many contribute? Where do they drop off?
5. **What content is missing?** — Vars with high traffic but no examples. Search queries with no results.
6. **How is usage trending?** — Is traffic growing, shrinking, or flat? Are there seasonal patterns (conference seasons, release cycles)?
7. **What devices/contexts?** — Desktop vs. mobile split. Does the mobile experience need investment?

## Context

- Matomo replaced Google Analytics (see CHANGELOG)
- The Matomo instance is hosted at `cognitect.matomo.cloud` — ownership/longevity of this instance is an open question
- The current integration is the default Matomo snippet with no customization
- Privacy: Matomo Cloud is GDPR-compliant by default and does not require cookie consent banners in most configurations — worth confirming current settings

## Labels

`analytics`, `investigation`, `roadmap`
