package com.example.util

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.example.gpengl.first.util.FileUtils
import com.example.gpengl.third.record.EGLBase

class FFmpegMediaRecode(
    var context: Context,
    var width: Int,
    var height: Int,
    var eglContext: EGLContext
) {
    private var mPath: String? = null
    private var handler: Handler? = null
    private var surface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var mEglBase: EGLBase? = null

    fun start() {
        mPath = FileUtils.getStorageMp4(context, System.currentTimeMillis().toString())
        Log.d("ybb", mPath ?: "")

        /**
         * 配置EGL环境
         */
        //Handler ： 线程通信
        // Handler: 子线程通知主线程
//        Looper.loop();
        val handlerThread = HandlerThread("VideoCodec")
        handlerThread.start()
        val looper = handlerThread.looper
        if (surfaceTexture == null){
            surfaceTexture = SurfaceTexture(0)
        }

        if (surface == null){
            surface = Surface(surfaceTexture)
        }


        // 用于其他线程 通知子线程
        handler = Handler(looper)

        //子线程： EGL的绑定线程 ，对我们自己创建的EGL环境的opengl操作都在这个线程当中执行
        handler = Handler(looper)
        handler?.post { //创建我们的EGL环境 (虚拟设备、EGL上下文等)
            mEglBase = EGLBase(context, width, height, surface, eglContext)
            //启动编码器
        }
    }

    fun stop(){
        handler?.post{
            surface?.release()
            surface = null
            surfaceTexture?.release()
            surfaceTexture = null
            mEglBase?.release()
        }

    }

}