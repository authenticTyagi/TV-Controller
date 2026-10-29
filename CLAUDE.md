# CLAUDE.md — binding rules for TV Watch

Read this first every session. Then read `docs/context/session-state.md` for where we stopped.

## Project
Parent-facing monitor for a home iFFALCON (TCL) Android TV 9: what is watched and for how long, on-screen messages,
daily limit, live USB-camera view. Three parts: `android/` (TV app), `server/` (relay + dashboard), `docs/`.
Details: `docs/ARCHITECTURE.md`. Index of all docs: `docs/README.md`.

## Rules (binding)
1. **Public repo, no secrets.** `github.com/authenticTyagi/TV-Controller` is public. Never commit real `TV_TOKEN`,
   `VIEWER_PASSWORD`, relay URLs, TV IP, or card/account data. Real values live in env vars on the host and in
   `~/.gradle/gradle.properties` (overrides the placeholders in `android/gradle.properties`).
2. **Never create accounts, enter card details, or grant OAuth permissions in the browser.** Stop and ask the user to do it.
   Ask before any outward action (push to a new place, publishing, sending, deploying with new credentials).
3. **Report status honestly.** Only the relay is tested. Do not describe the Android app, dashboard in a browser, or the
   camera as working until `docs/RELEASE_SMOKE_CHECKLIST.md` items for them pass on the real TV. Keep the "Status" line in
   `README.md` and `docs/TASKS.md` truthful.
4. **Protocol changes touch three places in one change:** `server/server.js`, `android/.../MonitorService.kt`
   (+ `CameraStreamer.kt`/`Overlay.kt` if relevant), `server/public/index.html`, and the protocol table in
   `docs/ARCHITECTURE.md`.
5. **Privacy is a feature, not an afterthought.** Camera frames are relayed live and never stored. No audio. Camera only
   runs when the parent turns it on. Keep the on-TV "camera on" indicator once built (T-12). The children are told about monitoring.
6. **Keep the relay dependency-light** (`express`, `ws` only; JSON file storage). Add a dependency only with a decision
   entry in `docs/DECISIONS.md`.
7. **Android constraints:** `targetSdk = 28` and `minSdk = 24` on purpose (TV is Android 9; see D-05). Do not raise targetSdk
   without re-testing background start, overlay and camera on the real TV.
8. **Docs are part of the change.** Update `docs/TASKS.md` (status), `docs/DECISIONS.md` (new choices) and
   `docs/context/session-state.md` (end of every session) when you change behaviour or stop work.
9. **Code style:** match surrounding code, minimal comments (one-line header per file/class explaining purpose; comment only
   non-obvious why). No speculative abstractions.
10. **Shell on this machine is Windows (Git Bash/PowerShell).** Multi-line heredocs containing quotes failed once; use the
    Write tool for files. Prefer absolute paths. The project directory contains a space: quote it.

## Commands
- Relay: `cd server && npm install && TV_TOKEN=x VIEWER_PASSWORD=y npm start` (port 8080, `PORT` to override).
- TV app: open `android/` in Android Studio, sync, build debug APK.
- Install: `adb connect <TV-IP>:5555 && adb install app-debug.apk && adb shell am start -n com.home.tvwatch/.MainActivity`.
