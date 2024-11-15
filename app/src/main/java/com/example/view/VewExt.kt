package com.example.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

fun View.toBitmap(viewWidth: Int, viewHeight: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}