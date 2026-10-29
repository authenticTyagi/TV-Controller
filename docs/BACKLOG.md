# Backlog (unscheduled)

**Monitoring**
- Per-video titles via MediaSession listener / accessibility service.
- Timeline view and per-day history chart; weekly summary.
- Offline-event persistence on the TV across app restarts (currently in-memory queue).
- Detect HDMI-input viewing (set-top box, console) as a distinct "HDMI" source.

**Control**
- Per-app time limits and bedtime schedule; app block list.
- "Pause TV now" remote lock; extra-time grant from the dashboard.
- Queue messages while TV is offline and deliver on reconnect.

**Camera**
- WebRTC for smoother video (needs TURN or direct path).
- UVCCamera library fallback; motion-triggered snapshots.
- Snapshot button.

**Platform**
- Rate-limit / lock out repeated bad dashboard passwords; logout button; session tokens instead of password header.
- Per-viewer accounts (two parents).
- Phone push notifications (e.g. TV used outside allowed hours).
- Multiple TVs.
- Move storage from JSON file to SQLite if data grows.
- CI: build APK and run relay smoke test on push.
