# PRD — TV Watch

## Problem
The owner will be away from home for a period. Their children use the living-room iFFALCON Android TV. The owner wants
to know what is watched and for how long, to put a message or picture on the screen on demand, to cap daily viewing,
and to look at the room through a USB camera attached to the TV.

## Users
- **Parent (owner):** uses a phone browser. Non-developer comfort level: can install Android Studio, enable TV developer mode.
- **Children:** watch the TV; see overlays and the time-limit behaviour. Informed that monitoring and a camera exist.

## Goals
1. See what app is on the TV now and per-app viewing time today and over 7 days.
2. Send text and/or a picture that appears over any running app for 10-60 s.
3. Set a daily limit in minutes; TV warns at 5 min left, then returns to the home screen.
4. Live camera view from a USB webcam on the TV, on demand.
5. Works from anywhere (no home-router changes), free or very low cost to run.

## Non-goals (v1)
- Per-video titles, content filtering, remote control of playback, multiple TVs/users, audio, video recording/history,
  push notifications, a native phone app.

## Constraints
- TV: iFFALCON/TCL, Android 9 (PPR1.180610.011, patch 2021-01-05), USB debugging enabled, sideloaded app (see context/tv-device.md).
- Always-on cloud relay is required; free tiers that sleep break the TV's persistent connection (context/relay-hosting.md).
- Camera depends on TV firmware exposing the webcam (context/camera.md) — unproven.

## Success criteria
- Parent sees the correct foreground app within ~10 s of a change.
- Message appears on TV within ~2 s while TV is online.
- Limit enforced: at 0 minutes left the TV returns to home within ~6 s of any app launch.
- Camera: a frame stream appears on the dashboard, or a clear "not supported" message.
- App survives TV reboot and standby cycles without manual restart.

## Risks
| Risk | Impact | Mitigation |
|---|---|---|
| TV firmware hides USB webcam from apps | No live camera | Test first (T-08); fallback UVCCamera library or separate phone/Pi camera |
| Vendor power-saving kills background service | Gaps in tracking | Boot receiver, foreground service, battery exemption (T-10) |
| Free host sleeps | TV shows offline | Always-on host (Northflank free / small VPS) |
| Children bypass (uninstall, unplug) | Monitoring lost | Out of scope v1; dashboard shows "TV offline" |
| Privacy/trust | Family friction | Tell children; on-TV camera indicator (T-12) |

## Status
Relay tested locally. Android app and dashboard written, not yet built/run on the TV. See `TASKS.md`.
