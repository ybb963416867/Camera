package com.example.libffmpeg

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES30
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Kotlin层 - PBO管理器
class PBOFrameCapture(private val width: Int, private val height: Int, private val context: Context) {
    private val pboIds = IntArray(2)
    private var pboIndex = 0
    private val frameSize = width * height * 4 // RGBA
    private var isInitialized = false

    // 用于异步编码的线程池
    private val encodingExecutor = Executors.newSingleThreadExecutor()
    private var ffmpegRecorder: FFmpegRecorder? = null

    fun initialize() {
        // 创建双PBO
        GLES30.glGenBuffers(2, pboIds, 0)

        for (i in 0..1) {
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds[i])
            GLES30.glBufferData(
                GLES30.GL_PIXEL_PACK_BUFFER,
                frameSize,
                null,
                GLES30.GL_STREAM_READ
            )
        }

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        isInitialized = true

        Log.i("PBOCapture", "PBO initialized with size: ${frameSize / (1024 * 1024)}MB")
    }

    fun startRecording(outputPath: String, fps: Int = 30, bitrate: Int = 2000000) {
        if (!isInitialized) {
            throw IllegalStateException("PBO not initialized")
        }
        Log.i("PBOCapture", "startRecording path = $outputPath")
        ffmpegRecorder = FFmpegRecorder()
        ffmpegRecorder?.startRecord(outputPath, width, height, fps, bitrate)
    }

    fun captureFrame(): Boolean {
//        if (!isInitialized || ffmpegRecorder == null) return false

        // 当前PBO异步读取像素数据
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds[pboIndex])
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0)

        // 从另一个PBO读取上一帧数据进行编码
        val readIndex = (pboIndex + 1) % 2
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds[readIndex])

        // 映射PBO内存
        val mappedBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            frameSize,
            GLES30.GL_MAP_READ_BIT
        )

        if (mappedBuffer != null) {
            val pixelBuffer = mappedBuffer as ByteBuffer
            // 复制数据到Java数组（这里会有一次拷贝开销）
            val pixels = ByteArray(frameSize)
            pixelBuffer.position(0)
            pixelBuffer.get(pixels)
//            val storagePicture = getStoragePicture(context, "b")
//            Log.i("ybb", "storagePicture = $storagePicture")
//            Triple(pixels, width, height).toBitmap().savaFile(storagePicture)

            // 异步编码避免阻塞渲染线程
            encodingExecutor.execute {
                ffmpegRecorder?.encodeFrame(pixels)
            }

            GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        }

        // 切换PBO
        pboIndex = readIndex
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)

        return mappedBuffer != null
    }

    fun stopRecording() {
        encodingExecutor.execute {
            ffmpegRecorder?.stopRecord()
            ffmpegRecorder = null
        }
    }

    fun release() {
        if (isInitialized) {
            GLES30.glDeleteBuffers(2, pboIds, 0)
            isInitialized = false
        }
        encodingExecutor.shutdown()
    }


    /**
     * 获取Cache目录
     *
     * @param context context
     * @return File
     */
    fun getCacheDir(context: Context): File? {
        return context.externalCacheDir
    }

    /**
     * 获取Cache目录 Movie
     *
     * @param context context
     * @return File
     */
    fun getCacheMovieDir(context: Context): File {
        val dir = Environment.DIRECTORY_MOVIES
        return File(getCacheDir(context), dir)
    }

    fun getStoragePicture(context: Context, s: String?): String {
        val dirFile = getCacheMovieDir(context)
        dirFile.mkdirs()
        // 创建保存文件
        val mediaFile = File(dirFile, getDateName(s) + ".png")

        return mediaFile.path
    }

    /**
     * 返回带日期的名称
     *
     * @param prefix 文件名前缀(会自动拼接 _ )
     * @return String
     */
    fun getDateName(prefix: String?): String {
        date.time = System.currentTimeMillis()
        val dateStr = dateFormat.format(date)
        return if (!TextUtils.isEmpty(prefix)) {
            prefix + "_" + dateStr
        } else {
            dateStr
        }
    }

    private val date = Date()

    @SuppressLint("ConstantLocale")
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
}