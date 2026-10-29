package com.home.tvwatch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import java.io.ByteArrayOutputStream

/**
 * Streams JPEG frames (~2.5 fps) from a USB camera via Camera2.
 * Works only if the TV firmware exposes the USB webcam as an external camera.
 */
class CameraStreamer(
    private val ctx: Context,
    private val onFrame: (ByteArray) -> Unit,
    private val onStatus: (ok: Boolean, error: String?) -> Unit,
) {
    private var thread: HandlerThread? = null
    private var device: CameraDevice? = null
    private var reader: ImageReader? = null
    private var lastSent = 0L

    @SuppressLint("MissingPermission")
    fun start() {
        if (device != null) return
        val mgr = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = mgr.cameraIdList.firstOrNull {
            mgr.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_EXTERNAL
        } ?: mgr.cameraIdList.firstOrNull()
        if (id == null) { onStatus(false, "No camera exposed by the TV (USB webcam not supported by firmware?)"); return }

        val sizes = mgr.getCameraCharacteristics(id).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(ImageFormat.YUV_420_888)
        val size = sizes.filter { it.width <= 800 }.maxByOrNull { it.width * it.height } ?: sizes.minByOrNull { it.width * it.height }!!

        val t = HandlerThread("cam").also { it.start() }; thread = t
        val h = Handler(t.looper)
        reader = ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 2).apply {
            setOnImageAvailableListener({ r ->
                val img = r.acquireLatestImage() ?: return@setOnImageAvailableListener
                try {
                    val now = System.currentTimeMillis()
                    if (now - lastSent >= 400) { lastSent = now; onFrame(toJpeg(img)) }
                } finally { img.close() }
            }, h)
        }
        mgr.openCamera(id, object : CameraDevice.StateCallback() {
            override fun onOpened(cam: CameraDevice) {
                device = cam
                cam.createCaptureSession(listOf(reader!!.surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(s: CameraCaptureSession) {
                        val req = cam.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply { addTarget(reader!!.surface) }
                        s.setRepeatingRequest(req.build(), null, h)
                        onStatus(true, null)
                    }
                    override fun onConfigureFailed(s: CameraCaptureSession) { onStatus(false, "Session config failed"); stop() }
                }, h)
            }
            override fun onDisconnected(cam: CameraDevice) { stop() }
            override fun onError(cam: CameraDevice, e: Int) { onStatus(false, "Camera error $e"); stop() }
        }, h)
    }

    fun stop() {
        runCatching { device?.close() }; device = null
        runCatching { reader?.close() }; reader = null
        thread?.quitSafely(); thread = null
    }

    private fun toJpeg(img: android.media.Image): ByteArray {
        val w = img.width; val h = img.height
        val y = img.planes[0]; val u = img.planes[1]; val v = img.planes[2]
        val nv21 = ByteArray(w * h * 3 / 2)
        var p = 0
        for (row in 0 until h) for (col in 0 until w) nv21[p++] = y.buffer.get(row * y.rowStride + col * y.pixelStride)
        for (row in 0 until h / 2) for (col in 0 until w / 2) {
            nv21[p++] = v.buffer.get(row * v.rowStride + col * v.pixelStride)
            nv21[p++] = u.buffer.get(row * u.rowStride + col * u.pixelStride)
        }
        val out = ByteArrayOutputStream()
        YuvImage(nv21, ImageFormat.NV21, w, h, null).compressToJpeg(Rect(0, 0, w, h), 60, out)
        return out.toByteArray()
    }
}
