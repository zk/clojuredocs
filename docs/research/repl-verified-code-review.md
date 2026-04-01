# Trust Nothing — REPL-Verified Code Review

## Motivation

AI coding agents produce claims about code at every step: "this namespace contains N vars," "this function handles edge case X," "this change is safe because Y." Human code reviewers read these claims and, under time pressure, accept most of them. But AI-generated content has a measurable error rate — roughly 1 in 6 atomic claims may be wrong.

A Clojure REPL connected to a running application can verify these claims against the actual runtime artifacts. This demo shows how, using the [clojuredocs.org](https://clojuredocs.org) webapp as the subject.

## Research context

This demo draws on three research documents from [stu-ai-projects](https://github.com/nubank/stu-ai-projects). Each identifies a different facet of the same underlying problem: AI tools operating on prose representations of information that could be accessed directly from runtime artifacts.

### [Napkins All the Way Down](https://github.com/nubank/stu-ai-projects/blob/main-no-review-ceremony/essays/napkins-all-the-way-down.md)

**Core argument**: When AI tools read human-written prose *about* runtime artifacts instead of reflecting on the artifacts themselves, every layer of the pipeline introduces lossiness. A changelog becomes a reference document becomes generated code — "napkins all the way down." Rich Hickey's observation from [Language of the System](https://github.com/matthiasn/talk-transcripts/blob/773d2992d7/Hickey_Rich/LanguageSystem.md) holds: when schema information lives out of band, "you go back to the napkin."

**Key finding**: In one studied system, 46 of 481 function names (9.6%) in an LLM-maintained reference document did not match any actual `defn` in the source code. The mismatches were invisible until someone checked against the runtime.

**Connection to this demo**: An AI agent analyzing clojuredocs will make claims about namespaces, vars, and metadata by reading source files — i.e., prose about the runtime. The REPL lets the reviewer check those claims against actual loaded vars, just as `ns-publics` replaced changelogs in the essay's bdc-api-map example.

### [Bulldozing vs. Eliminating Complexity](https://github.com/nubank/stu-ai-projects/blob/main-no-review-ceremony/essays/bulldozing-vs-eliminating-complexity.md)

**Core argument**: AI makes it cheap to bulldoze through complexity — teaching agents rules in natural language, copying instructions across repos, accepting AI output at face value. But this is different from *eliminating* complexity, which means hardening conventions into deterministic enforcement (lint rules, macros, type checks) so neither humans nor agents need to know the rules at all.

**Key finding**: 51 CLAUDE.md files across 51 Nubank repositories teach AI agents the same BDC staging rules in varying natural-language descriptions. The rules could instead be enforced by a macro, eliminating the entire class of errors.

**Connection to this demo**: Accepting AI claims without REPL verification is the bulldozing path — you get through the review faster but silently pass errors. REPL verification is a step toward elimination: you replace trust in prose with evidence from the runtime.

### [Enforcing the REPL](https://github.com/nubank/stu-ai-projects/blob/main-no-review-ceremony/enforcing-the-repl/README.md)

**Core argument**: AI agents default to Bash for computation because shell commands are overrepresented in training data. But a persistent, stateful REPL with rich data structures is the superior environment for the data processing and analysis work agents routinely perform. Persistent state, structural data, composable querying, and error recovery without restart compound across a session.

**Key finding**: The REPL's flywheel effect — each evaluation deposits something useful, and subsequent evaluations draw on it — means the 50th query in a REPL session is cheap, while the 50th Bash command is exactly as expensive as the first.

**Connection to this demo**: The verification expressions below are REPL-native. They build on each other within a session. A reviewer who loads the app's namespaces once can verify dozens of AI claims cheaply. This is the flywheel in action.

### The shared principle

All three documents converge on the [reliability ratchet](https://github.com/nubank/stu-ai-projects/blob/main-no-review-ceremony/claude-perf-analyses/reliability-ratchet.md): a workflow where each step makes the process more deterministic. Freeform LLM exploration → REPL extraction → library → enforcement. This demo lives at the "REPL extraction" click — showing how a human reviewer uses the REPL to move from trusting AI prose to verifying against runtime data.

## Why clojuredocs is the right vehicle

The clojuredocs.org webapp *already* introspects Clojure vars at runtime to power its search and display. From the [README](../../README.md):

> Most vars are picked up from Clojure at runtime, using core namespace and var introspection utilities. Special forms and namespaces to include on the site are specified explicitly in the `clojuredocs.search.static` namespace.

This means the REPL verification technique is not an external imposition — it uses the same runtime reflection the application itself uses. Verifying AI claims about clojuredocs via the REPL is doing exactly what clojuredocs does to serve its pages.

## Demo plan

### Phase 1 — Set the trap: AI makes claims

Ask an AI agent to analyze the clojuredocs codebase. Choose tasks that naturally produce verifiable claims:

1. **"What namespaces does clojuredocs track?"** — The AI reads `clojuredocs.search.static` source and lists them.
2. **"How many searchable vars does the site expose?"** — The AI estimates from source code.
3. **"Which vars are special forms vs. regular fns?"** — The AI categorizes from source reading.
4. **"How many vars lack docstrings?"** — The AI estimates scope (relevant to [issue #8](https://github.com/nubank/clojuredocs/issues/8)).
5. **"Does function X work the way the AI says it does?"** — Pick any claim about behavior.

The goal is not to stack the deck. The AI will get most claims right. The interesting part is which ones it gets wrong, and how the REPL catches them.

### Phase 2 — Verify at the REPL: human catches errors

Connect to a running clojuredocs nREPL. For each AI claim, write a short expression that checks it against the runtime:

| AI claim | REPL verification |
|---|---|
| "clojuredocs tracks N namespaces" | `(count clojuredocs.search.static/clojure-namespaces)` |
| "there are ~X searchable vars" | `(->> clojuredocs.search.static/clojure-namespaces (map find-ns) (mapcat ns-publics) count)` |
| "these are the special forms" | `clojuredocs.search.static/special-forms` — compare to AI's list |
| "N vars lack docstrings" | `(->> (mapcat ns-publics (map find-ns clojuredocs.search.static/clojure-namespaces)) (filter #(nil? (:doc (meta (val %))))) count)` |
| "function X works like Y" | Call it and observe |

Each verification is a single REPL expression. The reviewer builds up a session: load namespaces once, then query cheaply. This is the compounding effect from the Enforcing the REPL research — the first eval pays the setup cost, every subsequent eval is nearly free.

### Phase 3 — Show the ratchet: harden one finding

Take a concrete mismatch from Phase 2 and harden it:

1. **Napkin → Blueprint**: Show the AI's prose claim side-by-side with the REPL data. The prose is the napkin; the data is the blueprint.
2. **Save the verification**: Capture the REPL expression as a rich comment form in a `.clj` file or as a test. Now any future reviewer (human or AI) can re-run the check.
3. **Discuss the ratchet**: The AI's analysis was step 1 (freeform exploration). The REPL verification was step 2 (extraction). Saving it as a test is step 3 (library/enforcement). Each click makes the next review more reliable.

### Deliverable format

A **rich comment form** (`.clj` file with `(comment ...)` blocks) is the most Clojure-native format:

- The reviewer evaluates each form in sequence in their connected REPL
- Results appear inline, next to the claim being tested
- The file is both the demo script and the verification tool
- It lives in the repo and can be re-run by anyone with a REPL

Supplement with a short narrative doc (this file) that connects findings to the research themes.

## Suggested workflow

1. Start the clojuredocs app and connect a REPL (`bin/prod-local`, then `lein repl :connect`)
2. Ask an AI agent to analyze the codebase — capture its claims
3. Build verification expressions in a rich comment form at `dev/repl_verified_review.clj`
4. Identify 3–5 concrete mismatches
5. Write up the narrative connecting findings to the three research documents
6. Present: claims → verification → findings → ratchet

## Version History

| Date | Change |
|------|--------|
| 2026-04-01 | Initial plan. Research context, demo structure, and connection to three source essays. |

---

> **AI Disclaimer**: L. Jordan Miller supplied the source research documents drafted by Stu Halloway, LJM posed the hypothesis (that a REPL can catch errors in AI-generated code review claims), and designed the experiment. Claude Code (Claude Opus 4.6) drafted the prose and structured the plan. [Trust nothing](https://github.com/nubank/stu-ai-projects/blob/main-no-review-ceremony/essays/trust-nothing.md) — AI-generated content may contain false statements.
