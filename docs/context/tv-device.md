# TV device

- iFFALCON (TCL), purchased ~2020. Exact model unknown (not yet read from Settings).
- Android 9, security patch 2021-01-05, build `PPR1.180610.011 release-keys`.
- USB ports present (used for movies/pictures). USB debugging and OEM-related developer options enabled by the user.
- Sideload route: Settings > Device Preferences > About > tap Build 7x > Developer options > network debugging, then `adb connect <TV-IP>:5555`.
- TV IP not recorded (and not to be committed). Find in Settings > About > Network.
- Android 9 supports: UsageStatsManager events, `TYPE_APPLICATION_OVERLAY`, `startForegroundService`. Camera2 external cameras
  exist on Android 9 but depend on the vendor HAL.
- Open questions: does firmware kill background services in standby? Do HDMI inputs show as an app in usage stats?
