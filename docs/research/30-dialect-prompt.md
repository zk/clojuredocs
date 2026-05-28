# Claude Code Prompt — Create Planning Template for Issue #30

> Paste into a Claude Code Opus 4.6 session in VS Code. You should be on the `nubank/clojuredocs` repo.

---

I need to plan implementation of https://github.com/nubank/clojuredocs/issues/30 — adding cross-dialect compatibility indicators (Clojure/JVM, ClojureScript, babashka) to ClojureDocs var pages. Before writing any code, I need a research and planning document.

## What to create

Create a markdown file at `docs/research/issue-30-dialect-compat-planning.md` on a new branch `research/30/dialect-compat-planning`.

This document is a planning artifact — not a spec, not a design doc, not implementation notes. Its purpose is to help me (and anyone reading it later, human or AI) understand what we know, what we don't know, and what we need to find out before we can break this issue into implementable steps. I will use it to review the approach with my manager Alex Miller before writing code.

## Framework

Structure the document using Rich Hickey's Reflective Inquiry framework from his ["Design in Practice"](https://www.youtube.com/watch?v=c5QF2HjHLSE) talk (Clojure/conj 2023). The core tool is Father Watson's four questions, arranged on two axes:

|               | Understanding (why)          | Activity (what)          |
|---------------|------------------------------|--------------------------|
| **Status**    | What do you know?            | Where are you at?        |
| **Agenda**    | What do you need to know?    | Where are you going?     |

Design progress is measured by the accretion of understanding, not the accretion of activity. The document should make unknowns as visible as knowns. Sections that say "unknown" are the most valuable — they tell you where to direct effort next.

Alex Miller gave a follow-up talk ["Design in Practice in Practice"](https://www.youtube.com/watch?v=VBnGhQOyTM4) (Clojure/conj 2024) showing how the Clojure team applied this process to Clojure 1.12 design decisions. That talk is useful context for the level of rigor expected here.

## Context to read before writing

Read these files in the repo to inform the template's content and constraints. Correct anything in the issue that doesn't match what you find in the code.

1. `docs/2026vison.md` — two-year vision. Find the "Cross-dialect hub" strategic bet and "Var → Dialects" in the graph model. These give the *why* for this issue.
2. `docs/resources/datamodelaudit.md` — data model coupling audit. This explains why the constraint "no schema or data model changes" exists.
3. `docs/resources/Clojure_2026_1_Pager.pdf` — Clojure team bets for 2026. ClojureDocs appears under "Considering" as a reference application.
4. `src/clj/clojuredocs/search/static.clj` — static data loading pattern. Our dialect data should follow this pattern.
5. `src/clj/clojuredocs/pages/vars.clj` — var page renderer. This is where dialect indicators will eventually render.
6. `src/clj/clojuredocs/search.clj` — `clojure-lib` config with version string.
7. The issue itself: https://github.com/nubank/clojuredocs/issues/30

## Document structure

The file should contain these sections in this order:

### Header
- Title, issue link, branch name, author (placeholder), dates (placeholders for start and last-updated).
- A "How to use this document" section explaining the Reflective Inquiry framework and that this is iterative, not a checklist.

### Glossary
A table defining key terms precisely. At minimum: Dialect, Var, Compatibility indicator, EDN. Include placeholder rows for terms that emerge during research. (Precision in naming yields precision in thinking.)

### Part 1: Per-Dialect Research (Father Watson × 3)

For each of the three dialects — **ClojureScript**, **babashka**, **Clojure/JVM** — create a subsection with:

1. **What do we know? (Status — Understanding)** — Current state of that dialect's standard library coverage relative to ClojureDocs. Known data sources (with checkboxes for verification status). What the authoritative, machine-readable source is for "which vars exist" in that dialect. Pre-fill with what you can determine from the codebase and public sources, but mark anything uncertain.

2. **Where are we at? (Status — Activity)** — Checklist of concrete research steps (identified data source, retrieved var list, cross-referenced against ClojureDocs vars for `clojure.core` and `clojure.string`, documented gaps).

3. **What do we need to know? (Agenda — Understanding)** — Open questions. For each dialect, think about: Is there a programmatic extraction method? What version do we target? How fast does compatibility data go stale? Are there vars with the same name but different behavior? Are there existing machine-readable var lists we can consume?

4. **Where are we going? (Agenda — Activity)** — Placeholder for next steps, to be filled as research progresses.

5. **Maintainer / Point of Contact** — A table for each dialect with fields: Project, Repository URL, Primary maintainer(s), Contact info (email/Slack/etc.), Timezone/Location, Best way to reach them, Whether we've contacted them (yes/no + date + outcome), Their stance on this feature, and Notes. Fill in what you can find from public sources (GitHub profiles, READMEs, Clojurians Slack channels). I will need this info to reach out for collaboration or to ask questions about data sources.

For **Clojure/JVM**, note that this is the baseline — every var on ClojureDocs is JVM-supported by definition. The research is about confirming the version and var count, not about compatibility.

### Part 2: Cross-Cutting Understanding

- Observations spanning all three dialects (total var count in scope, expected overlap, CLJ-only vars, tricky cases).
- A **data quality assessment table** rating each data source by format, freshness, whether it's machine-readable, and confidence level.
- A **risks and open questions** list that promotes the most important unknowns from Part 1.

### Part 3: Implementation Approach

Gate this section with a note: "Do not fill this section until Parts 1 and 2 have enough substance to make decisions from."

Include placeholders for:
- High-level approach (2-4 sentences)
- A **key decisions table** (decision matrix style — what decision, options considered, chosen option, why). Pre-populate rows for: data file format, data generation method, loading pattern, rendering location, unknown-state handling.
- Breakdown into smaller steps (numbered, each small enough for a single PR)
- "What I want to review with Alex" — placeholder list for questions/decisions needing manager input

### Part 4: Timeline and Coordination

- Milestone table with target dates and status. Include: research complete, approach reviewed with Alex, data file generated, rendering implemented, local verification, PR submitted, deployed. Note the April 27 deploy target from the issue.
- External dependencies table. Include: ClojureScript var list, babashka var list, Dutch Clojure Days (mid-May), babashka conf (mid-May) — shipping before these events is a feedback opportunity.

### References
Links to: the issue, vision doc, data model audit, both talks (with transcript link for Design in Practice), Clojure 2026 bets PDF, ClojureScript cheatsheet, babashka docs, jank-lang/clojure-test-suite (future data source), and relevant source files in the repo.

### Version History
Table with date and changes columns. One initial row with placeholder date.

### Errata
Empty numbered list with a comment explaining: this section is for errors discovered and corrected. Each entry should state what was wrong, the correction, and why the error occurred. Errata are not limitations.

### Learnings
Empty numbered list with a comment explaining: this section is for process, tooling, or design insights gained during this work that are reusable beyond this issue. Not dialect-specific findings (those go in Parts 1-2).

### AI Disclaimer (last element in the file)
A blockquote at the very bottom attributing who did what:
- Jordan Miller: defined the task, specified the research-first approach, chose the Father Watson / Reflective Inquiry framework, required per-dialect maintainer contact tables and manager-review sections, and will fill in research findings and make all decisions.
- Claude (Opus 4.6, via VS Code Copilot): drafted the template structure, read the codebase and context documents, and pre-filled sections with information from the code and public sources.
- All research sections that Claude filled in should be independently verified. Include a "Trust nothing" link.

## Style rules

- Write for an audience of open-source contributors and a manager who may skim this in 5 minutes.
- No jargon without definition. No acronyms without expansion on first use.
- Use code formatting for file paths, function names, namespace names, and URLs.
- Every section should have HTML comments explaining what goes there and how to fill it in — the template should be self-documenting for future me or for an AI agent working on it later.
- Use checkboxes (`- [ ]`) for progress tracking within research sections.
- Use placeholder comments (`<!-- -->`) for fields I need to fill in, not dummy data that looks real.
- Be precise about what is known from code inspection vs. what is assumed or inferred.

## What NOT to do

- Do not write implementation code.
- Do not generate the dialect compatibility EDN data file.
- Do not propose a UI design.
- Do not fill in Part 3 (Implementation Approach) beyond the placeholder structure.
- Do not make confident claims about dialect compatibility — this template is for planning research, not reporting conclusions.

## Output

Create the file at `docs/research/issue-30-dialect-compat-planning.md`. After creating it, summarize what you pre-filled from the codebase, what you left as placeholders, and the three most important open questions that emerged.

Do not commit or push — I will review first.

---

> **AI Disclaimer:** Jordan Miller defined the task, the research-first approach, the Father Watson / Reflective Inquiry framework, and all structural requirements for this prompt. Claude (Opus 4.6) via claude.ai drafted the prompt text based on Jordan's direction and the project context documents. This prompt has not yet been executed — no planning template has been produced from it.
