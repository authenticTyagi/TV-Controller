# Tasks

Status: `done` · `next` · `todo` · `blocked`. Last updated 2026-10-29.

| ID | Task | Status | Notes |
|---|---|---|---|
| T-01 | Relay server (REST + WS) | done | smoke-tested locally with a fake TV |
| T-02 | Dashboard page | done | written; not opened in a browser yet (T-15) |
| T-03 | Android TV app source | done | written; never compiled |
| T-04 | Dockerfile for relay | done | not built/run yet |
| T-05 | Push to GitHub | done | public repo authenticTyagi/TV-Controller, branch main |
| T-06 | Deploy relay on Northflank free | **next** | logged in; "Create service" form open; blocked on user linking GitHub (OAuth) and adding card. Steps in context/relay-hosting.md |
| T-07 | Set TV_TOKEN, VIEWER_PASSWORD, `/data` volume on host | todo | user generates values; never commit |
| T-08 | Compile app in Android Studio, fix errors | todo | set RELAY_URL/TV_TOKEN in `~/.gradle/gradle.properties` |
| T-09 | Install on TV via adb, finish 3 permission steps | todo | need TV IP (Settings > About > Network) |
| T-10 | Battery/background survival check, reboot test | todo | |
| T-11 | Camera feasibility test with iBall webcam | todo | highest risk; fallback in context/camera.md |
| T-12 | On-TV "camera on" indicator | todo | privacy rule 5 |
| T-13 | Verify usage tracking vs reality (YouTube, Netflix, HDMI input) | todo | HDMI inputs may not register as an app |
| T-14 | Verify limit enforcement | todo | |
| T-15 | Run RELEASE_SMOKE_CHECKLIST end to end | todo | |

## Next session starts at
T-06: user signs in to Northflank (done), links GitHub for `TV-Controller` only, adds card; then continue the form
(build context `/server`, Dockerfile, port 8080 HTTP, env vars, `/data` volume).
