# Design

## Dashboard (phone, single column, max 640 px)
Cards top to bottom:
1. **Right now** — status dot (green = TV online) and "App · since HH:MM", or "TV offline / app not running", or "home screen or standby".
2. **Screen time** — Today / 7 days selector, large total, per-app horizontal bars sorted descending.
3. **Send to TV** — text area, optional picture (downscaled in-browser to <= 1280 px JPEG), duration 10/20/60 s, "Show on TV".
4. **Daily limit** — minutes (0 = none), Save.
5. **Live camera** — image area, status text, Start/Stop.
Sign-in card first; light/dark via `prefers-color-scheme`. Reconnects WebSocket every 3 s; usage refreshes every 60 s and on status change.
Known gaps: no per-day history view, no timeline, no logout button, no password-attempt limiting (see BACKLOG).

## On-TV UI
- Setup screen: large text, three sequential steps.
- Overlay: full-width banner at top, dark translucent background, 34 sp white text, optional image (max 600 px high),
  not focusable and not touchable so it never steals the remote.
- Limit behaviour: banner "5 minutes of TV time left today" (10 s) once; at zero, banner (8 s) and return to home on every poll while an app is active.
- Planned: persistent small "camera on" indicator while streaming (T-12).

## Copy tone
Plain, kind, short. Time-up message: "Daily TV time is over. Time to do something else!"
