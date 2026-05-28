# ClojureDocs: Two-Year Vision (2026–2028)

> Make ClojureDocs the highest-signal, REPL-native, community-connected documentation system in the programming ecosystem.

---

## What does success look like?

Examples on ClojureDocs are accurate, executable, and written by real practitioners. Every example runs — if it doesn't execute correctly against the documented var, it is structurally rejected, not manually moderated. Contributors are known community members, and every contribution carries visible quality signals: authorship, freshness, and engagement. AI-generated slop doesn't survive contact with the verification pipeline. People trust what they read because the system earns that trust through enforcement, not curation alone.

The data model is explicit, documented, and separated from business logic. Adding a new feature — a scoring system, a REPL widget, a new library — does not require understanding the entire codebase. Contributors ship improvements without fear of breaking unrelated functionality. The architecture supports multi-library documentation, not just `clojure.core`. The system is AI-legible: loosely coupled, well-contracted, and independently testable — qualities that make it safe for both human and agentic contributors.

ClojureDocs is a hub between dialects. Clojure, ClojureScript, babashka, jank — the ecosystem is no longer one language on one runtime. Documentation that surfaces where behavior is shared and where it diverges makes the whole ecosystem more navigable. By connecting vars to cross-dialect test suites and marking dialect compatibility, ClojureDocs helps grow the ecosystem by making it easier to move between dialects and adopt new ones.

Every var page has an embedded REPL. Examples are executable — input and output are visible, and users can modify and re-run them in place. This is not just a convenience. In a world where AI tools threaten to eliminate the repetition necessary to build skills, the REPL is where developers put in the reps. Learning Clojure on ClojureDocs is active, not passive learning. The gap between "reading about a function" and "using a function" is eliminated.

Docs are not a dead end. Each var and namespace is a hub connecting to blog posts, talks, GitHub usage, mob programming sessions, and pairing opportunities. Engineers discovering a function or a library also discover the humans and resources around it. ClojureDocs is an entry point into the Clojure community, not a static reference page.

Decisions are data-driven from quantitative and qualitative sources. Analytics tell us which pages matter, which are dead, and where users drop off. User interviews give us insight to how people are using the site and features that would help them use it more.

ClojureDocs implementation exeplifies modern good quality code conventions and serves as a project template for modern clojure/clojurescript applications. The code is configured with built in guidelines for AI. As an open source project it is a place where engineers can demonstrate the skills that matter most in the next era of software development.

Contributing to ClojureDocs exercises the capabilities that distinguish effective developers in an AI-assisted world:
- Reviewing and verifying code written by others
- Writing and following clear specifications that define what "correct" means
- Designing systems that enforce quality through structure, not process
- Thinking architecturally about data models and extension points
- Making judgment calls about what's useful versus what's merely correct

An engineer who ships a contribution to ClojureDocs — whether it's a verified example, a hardened validation rule, a data model improvement, or a new library integration — has produced a public, verifiable artifact that demonstrates these skills in a way no interview question can simulate. The codebase is intentionally designed to reward this kind of work: loosely coupled so contributions are independently testable, explicitly modeled so changes are safe to make, and enforced by machines so human effort goes toward the decisions that require judgment. ClojureDocs is not just an open source project. It's an oppertunity to exercise and practice the skills the industry needs most.

---

## We will instrument analytics and establish baseline metrics for how people use ClojureDocs today.

**Why:** We can't improve what we don't measure. Right now we don't know which vars drive the most traffic, where users enter and leave, or which examples get used. A validated understanding of current usage patterns — entry points, popular vars, dead zones, session depth — is the foundation for every prioritization decision that follows. This work comes first because it de-risks everything else.

## We will make the data model explicit and extensible, decoupled from business logic.

**Why:** The current system encodes its data model implicitly across business logic. Every new feature risks breaking something unrelated. This is the keystone investment — without a clean, explicit schema, multi-library support, quality scoring, REPL integration, and every other improvement requires scattered changes across the codebase. An explicit model unlocks safe, parallel iteration and lowers the barrier for contributors.

## We will build a verification pipeline that structurally rejects incorrect examples.

**Why:** AI made content generation free; the bottleneck is now verification. If every example must execute correctly against its documented var, broken contributions are eliminated at submission time — not caught by reviewers after the fact. We reserve human judgment for the harder question: is this example *useful*, not just *correct*?

## We will introduce content quality signals to preserve human-first, high-trust documentation.

**Why:** Even with executable validation, not all correct examples are good examples. Author reputation, freshness, and engagement metrics surface examples that come from real experience and teach effectively — signal that no volume of AI-generated content can replicate.

## We will embed an interactive REPL on every var page with executable examples.

**Why:** Clojure is learned at the REPL, but ClojureDocs is static text. That's a fundamental mismatch. Executable, modifiable examples collapse the distance between reading documentation and writing code. In a world where AI threatens to eliminate the reps needed to build skills, the REPL is where developers still put in the work.

## We will support documentation for libraries beyond `clojure.core`.

**Why:** ClojureDocs assumes a single-library world, hardcoded into the data model and UI. The ecosystem has grown far beyond core. Multi-library support transforms ClojureDocs from a reference for one namespace into a documentation platform for the ecosystem.

## We will connect documentation to people and community resources.

**Why:** A var page with only a docstring and examples is a dead end. Developers learning a function need context that only comes from humans — when to use it, when not to, how it composes. By linking vars to blog posts, talks, GitHub usage, and live sessions like Clojure Camp, each page becomes a hub in a knowledge graph rather than an isolated leaf.

## We will surface cross-dialect compatibility and serve as a hub between Clojure dialects.

**Why:** The Clojure ecosystem now spans multiple dialects — Clojure, ClojureScript, babashka, jank — each with overlapping but not identical standard libraries. Developers moving between dialects need to know what works where. By linking vars to cross-dialect test suites like [jank-lang/clojure-test-suite](https://github.com/jank-lang/clojure-test-suite) and marking dialect compatibility on var pages, ClojureDocs becomes the place where the ecosystem's shared surface area is visible. This makes it easier to adopt new dialects and grows the ecosystem as a whole.

---

## Strategic Bets

1. **Human signal over volume.** Our moat is trust, not content generation. Companies that [replaced human judgment with AI volume reversed course](https://customerservicemanager.com/the-truth-about-klarnas-backtrack-on-ai-and-the-rehiring-of-humans/). We bet on the opposite direction.

2. **Enforce, don't document.** Quality rules belong in the system, not in style guides people must read and remember. Guidelines that depend on humans reading them will be ignored — especially as AI makes it cheap to generate plausible-looking content at volume.

3. **REPL as the interface.** Docs should behave like code. The REPL is both learning tool and verification mechanism.

4. **Docs as a graph, not pages.** Knowledge about a function is distributed across the ecosystem. We model it that way:
   - **Var → Var:** See-also relationships, enriched by co-occurrence in real codebases.
   - **Var → Namespace → Library:** Fluid navigation up and down the hierarchy.
   - **Var → People:** Make human expertise visible — who wrote the best example, the definitive blog post, the recurring mob session.
   - **Var → Source:** Link to source code for every var, not just core. Currently source links exist only for `clojure.core`.
   - **Var → Usage in the wild:** Real-world usage in open source codebases (e.g. via [grep.app](https://grep.app)) shows how a function is used in context, not just in isolated examples. Both have value.
   - **Var → Tests:** Link to relevant tests, especially cross-dialect efforts like [jank-lang/clojure-test-suite](https://github.com/jank-lang/clojure-test-suite). Tests are executable specifications — they show edge cases and expected behavior that docstrings and examples often miss.
   - **Var → Dialects:** Mark which dialects support a given var (Clojure, ClojureScript, babashka, jank). Surface where behavior is shared and where it diverges. This makes ClojureDocs a hub between dialects and helps grow the ecosystem by lowering the cost of moving between them.
   - **Var → Resources:** Blog posts, talks, threads. Each var page is a curated entry point.

   This enables relationship-based discovery ("functions related to sequence transformation") instead of requiring users to already know the name they're searching for.

5. **Cross-dialect hub.** The ecosystem grows when it's easy to move between dialects. ClojureDocs is uniquely positioned to be the connective tissue — the place where shared and divergent behavior across Clojure, ClojureScript, babashka, and jank is visible at a glance.

6. **Abstraction-first development.** No feature work without a stable model underneath. The graph requires the explicit data model; everything else builds on it.

---

## Risks

- **Refactor stall:** Investing in the data model without shipping visible user value for too long. The 11th-hour insight applies here — weeks of understanding should produce concrete enforcement, not just more documents.
- **Overengineering:** Building more schema than current use cases require.
- **Moderation friction:** Quality enforcement that creates false positives and discourages real contributors. The line between "structurally rejected" and "frustratingly pedantic" requires ongoing calibration.
- **Low engagement:** Improvements that don't move contributor or user behavior.
- **Industry uncertainty:** The AI landscape is shifting fast. Bets we make about what developers need — REPL-based learning, human curation, executable verification — may be overtaken by capabilities or workflows we can't predict today.


---

## Success Metrics

- ↓ Time to first REPL interaction on a var page
- ↑ Examples that are executable and verified
- ↑ Pages with executable examples
- ↑ Repeat usage and session depth
- ↑ Contributions from known community members
- ↓ Incorrect examples reaching users
- ↓ Low-quality submissions surviving the pipeline
- ↑ Libraries documented beyond `clojure.core`
- ↑ Vars with cross-dialect compatibility information


<br>

>AI Disclaimer: The ideas, direction, and strategic framing in this document were generated, posed, and drafted by Jordan Miller. Claude (Opus 4.6) edited, structured, and refined the prose.
