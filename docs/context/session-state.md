# Session state (update at the end of every session)

## 2026-10-29 — session 1
**Done**
- Scoped the project, scaffolded relay + dashboard + Android TV app, Dockerfile, README.
- Relay smoke-tested end to end with a simulated TV (message, limit, session/usage). App and dashboard untested.
- Pushed to https://github.com/authenticTyagi/TV-Controller (public, `main`).
- Researched hosting; picked Northflank free first (D-08).
- Browser: user signed up to Northflank; "Create service" form is open in Chrome (service name `controller`, Combined build).
- Wrote CLAUDE.md and docs/.

**Stopped at**
T-06. Northflank needs GitHub linked (user must approve the OAuth screen, repo `TV-Controller` only) and a card (user must enter).
I had asked the user to reply "go" for me to click the GitHub button; they chose to pause instead.

**Resume tomorrow**
1. Ask the user to finish GitHub link + card on Northflank (or do it together), then continue per docs/context/relay-hosting.md.
2. User generates `TV_TOKEN`/`VIEWER_PASSWORD` and gives them to the host form themselves (never into the repo or chat logs).
3. Open `android/` in Android Studio, put real RELAY_URL/TV_TOKEN into `~/.gradle/gradle.properties`, build, paste any errors.
4. Get the TV's IP, `adb connect`, install, do permission steps, run the camera pre-check (docs/context/camera.md).

**Environment notes**
- Windows 11, Git Bash + PowerShell; Node v26, npm 11; `gh` logged in as authenticTyagi.
- A Chrome tab (Northflank form) was left open.
