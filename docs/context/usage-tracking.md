# Usage tracking

- Source: `UsageStatsManager.queryEvents`, polled every 3 s; ACTIVITY_RESUMED sets the foreground package, ACTIVITY_PAUSED for the same package clears it.
- Seeded on start by scanning the last 2 hours of events.
- Not counted: launcher/home packages (resolved via CATEGORY_HOME), the app itself, and time when `PowerManager.isInteractive` is false.
- Sessions shorter than 5 s are dropped. A session is sent to the relay when the foreground app changes or the service stops.
  The currently open session is not in the totals until it closes (dashboard "Right now" shows it).
- Daily total on the TV: SharedPreferences `day` (yyyyMMdd) + `ms`; resets on date change. Limit and today's total survive app restarts.
- Needs user-granted "Usage access" (cannot be granted by adb on all builds; `adb shell appops set com.home.tvwatch GET_USAGE_STATS allow` may work).
- Unknowns: HDMI input and some live-TV apps may not appear as an app; some system apps share packages (label shows package name if lookup fails).
