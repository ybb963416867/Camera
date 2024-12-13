//package com.example.util
//
//import android.content.Context
//import android.util.Log
//import com.arthenica.ffmpegkit.FFmpegKit
//import com.example.gpengl.first.util.FileUtils
//import java.io.OutputStream
//import java.util.Locale
//
//
//class VideoEncoder(var context: Context) {
//
//    private var outputFilePath: String? = null
//    private var width = 0
//    private var height = 0
//    private var frameRate = 0
//    private val pixelFormat = "rgba"
//    private var isEncoding = false
//    private var ffmpegProcess: Process? = null
//    private var ffmpegInput: OutputStream? = null
//
//    fun create(width: Int, height: Int, frameRate: Int) {
//        this.width = width
//        this.height = height
//        this.frameRate = frameRate
//    }
//
//    /**
//     * 开始编码，启动FFmpeg进程。
//     */
//    fun start() {
//        if (isEncoding) return
//        outputFilePath = FileUtils.getStorageMp4(context, System.currentTimeMillis().toString());
//        try {
//
//            val ffmpegCmd = String.format(
//                Locale.US,
//                "ffmpeg -y -f rawvideo -pixel_format rgba -video_size %dx%d -framerate 30 -i - -c:v libx264 %s",
//                width, height, outputFilePath
//            )
//
//            ffmpegProcess = Runtime.getRuntime().exec(ffmpegCmd)
//            ffmpegInput = ffmpegProcess?.outputStream
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//
//        isEncoding = true
//    }
//
//    /**
//     * 编码一帧图像。
//     */
//    fun encodeFrame(rgbaBuffer: ByteArray) {
//        if (!isEncoding) return
//        ffmpegInput?.write(rgbaBuffer)
//        ffmpegInput?.flush()
//    }
//
//    /**
//     * 停止编码，结束FFmpeg进程。
//     */
//    fun stop() {
//        ffmpegInput?.close()
//        ffmpegProcess?.destroy()
//        isEncoding = false
//        Log.e(TAG, "编码已停止。")
//    }
//
//    companion object {
//        const val TAG = "VideoEncoder"
//    }
//}
//
