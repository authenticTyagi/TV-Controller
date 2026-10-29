# Release smoke checklist

Run in order after any change to server or TV app. Record date and result in `context/session-state.md`. Not yet run on a real TV.

## Relay (automatable)
- [ ] `npm start` boots; `GET /api/status` without password returns 401, with password returns JSON.
- [ ] Fake TV connects with `?token=`; wrong token is rejected.
- [ ] `POST /api/message` returns `delivered:true` and the TV client receives `message`.
- [ ] `POST /api/limit` persists across relay restart (data file).
- [ ] A `session` event appears in `GET /api/usage`.
- [ ] Docker image builds and runs with `/data` volume; data survives container restart.

## Dashboard (phone browser, over HTTPS)
- [ ] Wrong password is rejected; right password opens the page; light and dark both readable.
- [ ] "Right now" shows TV online/offline correctly when the TV app is started/stopped.
- [ ] Screen-time totals and bars match a stopwatch for a 5-minute viewing test.
- [ ] Text-only, picture-only, and text+picture messages send; "TV is offline" shown when the TV is off.

## TV app (real iFFALCON TV)
- [ ] APK installs via adb; the 3 setup steps complete; service notification present.
- [ ] Opening YouTube/Netflix is reported within ~10 s; going to home screen ends the session.
- [ ] TV standby ends the session; wake resumes tracking.
- [ ] Message overlay appears over a playing video, dismisses on time, does not steal remote focus.
- [ ] Limit: set to 2 min; warning at "5 min left" logic verified with a 6-min limit; at zero the TV returns home and an overlay shows.
- [ ] Reboot TV: service restarts without opening the app; relay shows TV online.
- [ ] Wi-Fi drop for 2 min: sessions queue and arrive after reconnect.

## Camera
- [ ] iBall webcam plugged in; "Start camera" shows frames within ~5 s, or a clear "not supported" error.
- [ ] "Stop camera" ends the stream and releases the device.
- [ ] On-TV "camera on" indicator visible while streaming (after T-12).
- [ ] Nothing is written to disk on the relay or TV.

## Security/privacy
- [ ] No real tokens/URLs in the repo (`git grep` for the token and relay hostname returns nothing).
- [ ] Children have been told about monitoring and the camera.
