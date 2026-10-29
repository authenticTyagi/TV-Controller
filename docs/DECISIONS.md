# Decisions

| ID | Date | Decision | Why | Revisit if |
|---|---|---|---|---|
| D-01 | 2026-10-29 | Custom companion app on the TV, not ADB-only | Needs overlay, camera, and tracking while the owner is away; ADB would require home network access | — |
| D-02 | 2026-10-29 | Cloud relay (user choice) rather than home server + VPN | No always-on home device; no router changes | Relay hosting becomes costly or unreliable |
| D-03 | 2026-10-29 | Camera frames as JPEG over WebSocket (~2.5 fps), not WebRTC | Works through the relay, no TURN/STUN, no native WebRTC library on the TV | Frame rate too poor in use |
| D-04 | 2026-10-29 | Dashboard = single static web page; password via header | Works on any phone, no install; single user | More than one viewer |
| D-05 | 2026-10-29 | `targetSdk 28`, `minSdk 24` | TV runs Android 9; avoids newer background-start and foreground-service-type rules | Moving to a newer TV |
| D-06 | 2026-10-29 | Daily limit counted on the TV, not the relay | Keeps enforcing while the TV is offline | Multi-device limits |
| D-07 | 2026-10-29 | Relay stores sessions in a JSON file | Zero dependencies, tiny data | Data growth or concurrent writers |
| D-08 | 2026-10-29 | Try Northflank free first; fallback small VPS (~$4-5/mo) | Free tiers that sleep break the persistent TV connection; Northflank reported as non-sleeping | Northflank sleeps or removes the free plan |
| D-09 | 2026-10-29 | Public GitHub repo with placeholders only | User supplied a public repo; secrets kept out via global gradle.properties and host env vars | Make repo private if desired |
| D-10 | 2026-10-29 | Claude does not create accounts, enter card details or grant OAuth in the browser | Platform safety rules | — |
