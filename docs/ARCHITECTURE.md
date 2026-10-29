# Architecture

```
TV app (Kotlin, foreground service)  --wss /tv?token-->  Relay (Node: express + ws)  <--wss /viewer?password--  Dashboard (static page)
                                                               ^                                                     |
                                                               +-------- HTTPS /api/* (x-password header) <---------+
```

## Components
**TV app** (`android/app/src/main/java/com/home/tvwatch/`)
- `MainActivity` — 3-step permission setup (usage access, overlay, camera), then starts the service.
- `MonitorService` — every 3 s reads `UsageStatsManager` events (ACTIVITY_RESUMED/PAUSED) to find the foreground app;
  ignores launcher packages, own package and screen-off; closes a session on app change (sessions < 5 s dropped);
  daily total in SharedPreferences; enforces limit; WebSocket to relay with 5 s reconnect and offline queue (500 events).
- `Overlay` — TYPE_APPLICATION_OVERLAY banner (text + optional image), auto-dismiss.
- `CameraStreamer` — Camera2, prefers LENS_FACING_EXTERNAL, YUV_420_888 <= 800 px wide -> NV21 -> JPEG q60, max ~2.5 fps.
- `BootReceiver` — starts the service on BOOT_COMPLETED.

**Relay** (`server/server.js`) — holds one TV socket and N viewer sockets; REST for commands/reads; stores sessions and the
limit in `DATA_FILE` (JSON; last 20,000 sessions). Serves `public/index.html`.

**Dashboard** (`server/public/index.html`) — single file, no build. Password kept in `sessionStorage`.

## Protocol
TV -> relay (text JSON): `session {pkg,label,start,end}`, `now {pkg|null,label,since}`, `camera-status {ok,error}`.
TV -> relay (binary): one JPEG per frame, forwarded unchanged to viewers.
Relay -> TV: `message {text,image(base64 JPEG|null),seconds}`, `limit {minutes}`, `camera {on}`.
Relay -> viewer: `status {tvOnline,now}`, `camera-status`, binary JPEG frames.
On TV connect the relay replays current `limit` and `camera` state.

## REST (all require header `x-password`)
`GET /api/status`, `GET /api/usage?days=1..30`, `POST /api/message {text,image,seconds}`, `POST /api/limit {minutes}`,
`POST /api/camera {on}`.

## Auth
TV: shared `TV_TOKEN` query param. Viewer: `VIEWER_PASSWORD` (header for REST, query param for WebSocket). Single user. HTTPS/WSS required in production.

## Data
`data.json`: `{ sessions: [{pkg,label,start,end}], limitMinutes }`. Times are epoch ms. Daily "today" boundary on the dashboard is the
browser's local midnight; on the TV it is the TV's local date.

## Deployment
Docker image from `server/Dockerfile` (node:20-alpine, port 8080, `DATA_FILE=/data/data.json`, mount a volume at `/data`).
Target host: Northflank free plan (not yet deployed). See `context/relay-hosting.md`.

## Known limits
- Relay state is in memory for `tv`, `now`, `cameraWanted`; a relay restart drops them until the TV reconnects.
- Messages are not queued when the TV is offline (dashboard says "TV is offline").
- Single TV; a second TV connection replaces the first.
