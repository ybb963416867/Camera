package com.example.libffmpeg

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import androidx.core.graphics.createBitmap
import java.io.FileOutputStream
import java.nio.ByteBuffer

fun Triple<ByteArray, Int, Int>.toBitmap(): Bitmap {
    return createBitmap(this.second, this.third).also {
        it.copyPixelsFromBuffer(ByteBuffer.wrap(this.first))
    }
}

fun Bitmap.savaFile(path: String): Boolean = run {
    val result = saveImage(this, path)
    this.recycle()
    result
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