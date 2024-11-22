package com.example.util

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.ViewGroup

class RenderThread<T : ViewGroup> : HandlerThread("RenderThread") {
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var handler: Handler? = null

    fun init(viewWidth: Int, viewHeight: Int, surfaceTexture: SurfaceTexture, surface: Surface) {
        start()
        handler = Handler(looper)
        this.surfaceTexture = surfaceTexture
        surfaceTexture.setDefaultBufferSize(viewWidth, viewHeight)
        this.surface = surface

        Log.e("ybb", "init")
    }

    fun renderView(view: T, listener: (() -> Unit)? = null) {
        handler?.post {
            surface?.let { surface ->
                val canvas = surface.lockCanvas(null)
                try {
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR) // 清空画布
                    view.draw(canvas) // 绘制 rootView
                    Log.e("ybb", "view.draw")
                } finally {
                    surface.unlockCanvasAndPost(canvas)
                }
                listener?.invoke()
            }
        }
    }

    fun release() {
        handler?.post {
            surface?.release()
            surface = null
            surfaceTexture?.release()
            surfaceTexture = null
        }
        quitSafely()
    }
}