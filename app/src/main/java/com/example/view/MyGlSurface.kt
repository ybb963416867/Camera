package com.example.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.rander.MyRender

class MyGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private var multipleRender: MyRender
    init {
        setEGLContextClientVersion(2)
        multipleRender = MyRender(this)
        setRenderer(multipleRender)
        renderMode = RENDERMODE_WHEN_DIRTY
//        holder.setFormat(PixelFormat.TRANSLUCENT)
    }


    fun setViewInfo(rootView: FrameLayout, viewWidth: Int, viewHeight: Int){
        multipleRender.setViewInfo(rootView, viewWidth, viewHeight)
    }

    fun updateViewTexture(){
        multipleRender.updateViewTexture()
    }


}





