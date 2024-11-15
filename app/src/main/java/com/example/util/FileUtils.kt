package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * description:
 * Created by aserbao on 2018/5/14.
 */
object FileUtils {
    private val date = Date()

    @SuppressLint("ConstantLocale")
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())

    val dateName: String
        /**
         * 返回带日期的名称
         *
         * @return String
         */
        get() = getDateName(null)

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

    @JvmStatic
    fun getStorageMp4(context: Context, s: String?): String {
        val dirFile = getCacheMovieDir(context)
        dirFile.mkdirs()
        // 创建保存文件
        val mediaFile = File(dirFile, getDateName(s) + ".mp4")

        return mediaFile.path
    }

    fun getStoragePicture(context: Context, s: String?): String {
        val dirFile = getCacheMovieDir(context)
        dirFile.mkdirs()
        // 创建保存文件
        val mediaFile = File(dirFile, getDateName(s) + ".png")

        return mediaFile.path
    }


    fun saveImage(oldBitmap: Bitmap, sNewImagePath: String?): Boolean {
        try {
            val fileOut = FileOutputStream(sNewImagePath)
            oldBitmap.compress(CompressFormat.JPEG, 80, fileOut)
            fileOut.flush()
            fileOut.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            System.gc()
            return false
        }
    }
}
