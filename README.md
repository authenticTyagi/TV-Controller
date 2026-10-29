# TV Watch

Monitor and message an iFFALCON (TCL) Android TV from your phone while away.

```
TV app (Kotlin)  <--WebSocket-->  Relay (Node, cloud)  <--HTTPS/WS-->  Dashboard (phone browser)
```

| Feature | How |
|---|---|
| What / how long | TV app polls UsageStatsManager, sends finished sessions to relay; dashboard shows today / 7 days per app |
| Message or picture on screen | Dashboard -> relay -> TV overlay (SYSTEM_ALERT_WINDOW) |
| Daily limit | Set in dashboard; TV app warns at 5 min left, then returns to home screen |
| Live camera | Camera2 external camera -> JPEG frames (~2.5 fps) over WebSocket -> `<img>` on dashboard |

## 1. Deploy the relay
```
cd server
npm install
TV_TOKEN=<long random> VIEWER_PASSWORD=<your password> npm start
```
Host it anywhere with HTTPS (Render, Fly.io, Railway, a VPS). Persist `DATA_FILE` on a volume if the host wipes disk.
Dashboard = the relay's URL. Use HTTPS only; the password travels in a header.

## 2. Build the TV app
Needs Android Studio (or JDK 17 + Android SDK). Edit `android/gradle.properties`:
`RELAY_URL=wss://your-relay` and the same `TV_TOKEN`. Build the debug APK.

## 3. Install on the TV
TV: Settings > Device Preferences > About > tap Build 7 times; then Developer options > enable USB/Network debugging.
```
adb connect <TV-IP>:5555
adb install app-debug.apk
adb shell am start -n com.home.tvwatch/.MainActivity
```
Follow the 3 on-screen steps (usage access, overlay, camera). Then power-save: in TV settings exempt TV Watch
from battery optimisation if available; the app restarts itself on boot.

## 4. Test camera FIRST
Plug in the webcam, press "Start camera" on the dashboard. If it reports "No camera exposed", the TV firmware
doesn't expose UVC cameras to apps. Fallback: add the UVCCamera library (saki4510t/UVCCamera) in `CameraStreamer`, or
use a cheap Android phone/Raspberry Pi as the camera instead.

## Status
Relay: smoke-tested locally. Android app and dashboard flows: written but NOT built or run on a real TV yet.
Tell your children the TV is monitored and a camera is present.
