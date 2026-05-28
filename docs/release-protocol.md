# Release & Announcement Protocol

How we ship and communicate changes for the public Nubank fork of ClojureDocs.

This document is an audit describing the **status quo** of our release and deploy
process. Infrastructure is inherited from the upstream
[zk/clojuredocs](https://github.com/zk/clojuredocs) project and has not
yet been verified or modernized. Sections marked
**[goal]** describe where we intend to converge — toward Clojure ecosystem
conventions and tooling. 

## 1. Versioning

### Status quo

The project version in `project.clj` is `"0.1.0-SNAPSHOT"` — inherited
from upstream and never updated. There are no git tags.

### Goal

Adopt straightforward incremental versioning consistent with Clojure
projects:

```
YYYY.MM.DD   (date-based, for deploys)
```

Date-based tags are simple, monotonic, and avoid the semantic debates
that semver introduces for a web application that has no public API
contract. Each deploy gets a tag like `2026.03.13`.

When a tag is cut, bump `project.clj` from `SNAPSHOT` to the release
version.

## 2. Release Checklist

Before every release:

1. **Update `CHANGELOG.md`** — move items from `[Unreleased]` into a new
   version section with today's date.
2. **Run the full build locally**:
   ```bash
   bin/build   # CLJS compile → tests → AOT compile
   ```
3. **Tag the release**:
   ```bash
   git tag -a <version> -m "Release <version>"
   git push origin <version>
   ```
4. **Deploy** (see §3 below).

## 3. Deploy

### Status quo

Production runs on an **AWS EC2 instance** with a single JVM process
managed by **systemd** (`clojuredocs.service`). Nginx reverse-proxies
port 80 to `127.0.0.1:4000`.

> **Important:** The app defaults to port 8080 (hardcoded in
> `clojuredocs.main`). `PORT=4000` must be set in `~/ClojureDocs/.env`
> on the server so the app listens where nginx expects it. If this
> variable is missing, the site will return 502.

> **Note:** The two-process Upstart rolling-restart procedure previously
> documented here was outdated; the server uses a single systemd service.
> `bin/ship` and `system.properties` (Heroku leftovers from upstream)
> have been removed. `~/ClojureDocs/provision.sh` on the server reflects
> the original provisioning but does not match the running config exactly
> (e.g. it assumed port 4000 without setting `PORT` in `.env`).

#### SSH access

SSH config (in `~/.ssh/config`):

```
Host clojuredocs
  HostName <prod-ip>
  User ubuntu
  IdentityFile ~/.ssh/ClojureDocs.pem
  IdentitiesOnly yes
```
The PEM key is shared out-of-band (ask the team lead).

#### Deploy steps

The deploy script lives on the server at `~/deploy.sh`. It pulls the
latest code, builds, and restarts the service:

```bash
# 1. SSH into the production box
ssh clojuredocs

# 2. Run the deploy script
~/deploy.sh
```

`deploy.sh` does the following:
```bash
cd /home/ubuntu/ClojureDocs
git pull
sudo -u ubuntu /usr/local/bin/lein deps
sudo -u ubuntu /usr/local/bin/lein compile
sudo -u ubuntu /usr/local/bin/lein cljsbuild once prod
sudo systemctl restart clojuredocs
```

> **Note:** This restarts the service in-place — there is a brief
> period of downtime (~30s) while the JVM starts. During this window
> users will see a 502 from nginx.

To check status after deploy:
```bash
sudo systemctl status clojuredocs
```

### Smoke Test Checklist

After deploy, verify manually:

- [ ] Homepage loads (`/`)
- [ ] Search works (`/search?q=map`)
- [ ] Var page renders (`/clojure.core/map`)
- [ ] Examples section loads on a var page
- [ ] Mobile layout — search bar visible and sticky
- [ ] newly added namespace page loads (example `/clojure.tools.build.api`)

## 5. Rollback

```bash
ssh clojuredocs
cd ~/ClojureDocs
git checkout <previous-tag-or-commit>
sudo -u ubuntu /usr/local/bin/lein deps
sudo -u ubuntu /usr/local/bin/lein compile
sudo -u ubuntu /usr/local/bin/lein cljsbuild once prod
sudo systemctl restart clojuredocs
```

## 4. Build Tooling

### Status quo

- **Leiningen** (`project.clj`) — all builds, REPL, CLJS compilation (dev build broken)
- **cljsbuild** — ClojureScript prod builds (`:simple` optimization)
- **foreman** — local dev process management (`Procfile.dev`)

### [goal] Alignment with Clojure ecosystem

- [ ] Migrate from Leiningen to **deps.edn + tools.build** (consistent
  with Clojure core and contrib libraries)
- [ ] Modernize cljsbuild (figwheel or shadow)
- [ ] Add GitHub Actions CI to run `bin/build` on every PR

---
> **AI Disclaimer**: Initial draft and research conducted and drafted by Claude (Opus 4.6) via GitHub Copilot in VS Code. Information has been reviewed by a human, corrected where applicable and noted in Version History below.
>


## Version History

| Date | Summary |
|------|--------|
| 2026-03-18 | §3 Deploy: replaced outdated two-process Upstart rolling-restart with verified single-service systemd deploy (from server `deploy.sh` and `provision.sh`). §5 Rollback: updated to match actual deploy commands. Removed `system.properties` migration goal (file deleted). Removed `bin/ship` and `system.properties` (Heroku vestiges). |
| 2026-03-13 | Rewrite: document status quo honestly, flag AI-introduced errors, surface AWS vs Heroku ambiguity, propose date-based versioning. |
| 2026-03-13 | Initial draft created (AI-generated). Contained invented version scheme and unverified deploy procedure. |
