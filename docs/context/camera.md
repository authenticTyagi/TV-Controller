# Camera

- Hardware: USB webcam, believed to be an iBall model (user not sure). UVC class assumed.
- Approach in code: Camera2, pick `LENS_FACING_EXTERNAL`, else first camera; YUV_420_888 up to 800 px wide; JPEG q60 about every 400 ms.
- Works only if TV firmware's camera HAL lists the USB webcam. If not, dashboard shows "No camera exposed by the TV".
- Quick pre-check (needs adb, TV connected): `adb shell dumpsys media.camera` after plugging the webcam in; look for an external camera entry.
  Also `adb shell ls /dev/video*`.
- Fallbacks, in order: UVCCamera library (saki4510t/UVCCamera) inside `CameraStreamer`; a separate cheap Android phone running the
  same streaming code; Raspberry Pi with the webcam posting JPEGs to the relay.
- Frames are forwarded live by the relay and never stored. No audio.
- Not tested yet (T-11).
