# Changelog

All notable changes to the Nubank fork of [ClojureDocs](https://clojuredocs.org) are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Changes are relative to the upstream [zk/clojuredocs](https://github.com/zk/clojuredocs) `master` branch.

---

## [Unreleased]

## [2026.04.29]

### Added
- **Cross-dialect compatibility indicators**: var pages now show compatibility
  badges for ClojureScript and babashka, backed by a static EDN index generated
  from dialect API metadata
  ([#30](https://github.com/nubank/clojuredocs/issues/30),
   [#37](https://github.com/nubank/clojuredocs/pull/37))

## [2026.04.07a]

### Added
- **Clojure/Conj 2026 banner**: site-wide announcement banner promoting
  Clojure/Conj 2026 (Charlotte, NC — Sept 30 – Oct 2) with Matomo click tracking
  ([#26](https://github.com/nubank/clojuredocs/issues/26),
   [#33](https://github.com/nubank/clojuredocs/pull/33))

## [2026.04.07]

### Added
- **Universal source links**: var pages for all namespaces in the `clojure/clojure`
  repository now show "Source" links to GitHub, not just `clojure.core`
  ([#28](https://github.com/nubank/clojuredocs/issues/28))

---

*Changes below are inherited from the fork baseline (pre-`2026.04.07`) and
are not individually versioned.*

### Fixed
- Fixed `test-paths` in `project.clj` (`"test/clj"` → `"test"`) — this
  was a Leiningen scaffolding default from the initial commit, not an AI
  error
- Replaced deliberately-failing placeholder test (`(= 0 1)`) — also a
  `lein new` default from the initial commit

### Added
- **tools.build library support**: new namespace `clojure.tools.build.api` with
  documentation, landing page, and link guide
  ([#3](https://github.com/nubank/clojuredocs/pull/3))
- **Sticky search bar**: search bar is now sticky and visible on mobile
  ([#15](https://github.com/nubank/clojuredocs/pull/15))
- **Developer docs**: VS Code / Calva dev setup guide, general dev setup guide
  ([#6](https://github.com/nubank/clojuredocs/pull/6))
- **Glossary**: 21-term glossary for codebase analysis (issue #4)
  ([#10](https://github.com/nubank/clojuredocs/pull/10))

### Removed
- `bin/ship` — Heroku deploy script (unused; production deploys via `~/deploy.sh` on the EC2 box)
- `system.properties` — Heroku buildpack file specifying Java 1.7 (server runs JDK 17)
- **Research**: data-model coupling audit
  ([#19](https://github.com/nubank/clojuredocs/pull/19))
- Clojure survey banner (later removed)
- New namespace docs: `clojure.java.process`, `clojure.repl.deps`,
  `clojure.tools.deps.interop`

### Changed
- Clojure version updates: 1.12.1 → 1.12.2 → 1.12.3 → 1.12.4
- Nubank branding assets
- Replaced Google Analytics with Matomo
- Updated MongoDB data export
- Upgraded nrepl 1.6.0, cider-nrepl 0.58.0 to fix middleware errors
- Updated Ring 1.13.0, commons-io 2.21.0 (CVE-free)
- Correct gravatar size in "Recently Updated" section
  ([#17](https://github.com/nubank/clojuredocs/pull/17))

### Removed
- Unused `.less` files (16k+ lines)
  ([#16](https://github.com/nubank/clojuredocs/pull/16))
- Third-party Mailgun integration
- Survey banner (after survey ended)
- Generated `app.css` from version control

### Fixed
- Landing page search uses `q` param to match backend handler
  ([#15](https://github.com/nubank/clojuredocs/pull/15))
- Navbar search hidden on landing page to avoid duplication
- Stray quote in CSS `position` value
- Mobile push-wrapper `:right 0` placement
- `.gitignore` for `.calva/` and generated assets

### Security
- Ring reverted from 1.15.3 to 1.13.0 (breaking API change), then
  commons-io upgraded to 2.21.0 to resolve CVEs independently

### AI cleanup

Items suggested or introduced by AI tooling that were incorrect or premature and
have been corrected:

- Removed invented `1.0.0-nu` version tag — no such tag exists in git
  and the versioning scheme has not been decided yet
- Removed invented `-nu` semver suffix convention — not a Clojure
  ecosystem pattern
- Rewrote `docs/release-protocol.md` deploy section — AI presented the
  upstream AWS procedure as the current deploy path without verifying it,
  while `bin/ship` and `system.properties` referenced Heroku. Both are
  now documented; AWS has since been confirmed as current.
- Reverted Copilot Autofix commit `8e5f827` on PR #20 — the reviewer
  suggested requiring `clojuredocs.core` and testing `(find-ns
  'clojuredocs.core)`, but that namespace does not exist. This was the
  same error present in the original test file that the PR was fixing.
---

<!-- Links -->
[Unreleased]: https://github.com/nubank/clojuredocs/compare/31d4bd2...HEAD
