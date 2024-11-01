//package com.example.rander
//
//import android.content.Context
//import android.graphics.SurfaceTexture
//import android.opengl.EGL14
//import android.opengl.EGLConfig
//import android.opengl.EGLContext
//import android.opengl.EGLDisplay
//import android.opengl.EGLSurface
//import android.opengl.GLES20
//import android.view.TextureView
//import com.example.gpengl.multiple.CoordinateRegion
//import com.example.gpengl.multiple.IBaseTexture
//import com.example.gpengl.multiple.PicTexture
//import com.example.gpengl.multiple.PicTextureT
//import com.example.gpengl.multiple.generateBitmapTexture
//import com.example.gpengl.multiple.generateCoordinateRegion
//import com.example.gpengl.multiple.getHeight
//import com.example.gpengl.multiple.getWidth
//import com.example.gpengl.multiple.offSet
//import com.example.util.Gl2Utils
//
//class MultipleTextureRender (var context: Context, var textureView: TextureView) :
//    TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {
//        private var shaderProgram = 0
//        private var baseTextureList = listOf<IBaseTexture>(
//            PicTextureT(context),
//            PicTexture(context),
//            PicTexture(context),
//            PicTextureT(context),
//            PicTextureT(context),
//            PicTexture(context),
//        )
//        private lateinit var eglContext: EGLContext
//        private lateinit var eglSurface: EGLSurface
//        private lateinit var surfaceTexture: SurfaceTexture
//        private var eglDisplay: EGLDisplay? = null
//        private var eglConfig: EGLConfig? = null
//
//        init {
//            textureView.surfaceTextureListener = this
//        }
//
//        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
//            surfaceTexture = surface
//            surfaceTexture.setOnFrameAvailableListener(this)
//
//            // 创建 EGL 环境
//            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
//            val version = IntArray(2)
//            EGL14.eglInitialize(eglDisplay!!, version, 0, version, 1)
//
//            val configAttribs = intArrayOf(
//                EGL14.EGL_RED_SIZE, 8,
//                EGL14.EGL_GREEN_SIZE, 8,
//                EGL14.EGL_BLUE_SIZE, 8,
//                EGL14.EGL_ALPHA_SIZE, 8,
//                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
//                EGL14.EGL_NONE
//            )
//
//            val numConfigs = IntArray(1)
//            val configs = arrayOfNulls<EGLConfig>(1)
//            EGL14.eglChooseConfig(
//                eglDisplay, configAttribs, 0, configs, 0, configs.size, numConfigs, 0
//            )
//            eglConfig = configs[0]
//
//            val contextAttribs = intArrayOf(
//                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
//                EGL14.EGL_NONE
//            )
//            eglContext = EGL14.eglCreateContext(
//                eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0
//            )
//
//            eglSurface = EGL14.eglCreateWindowSurface(
//                eglDisplay, eglConfig, surfaceTexture, null, 0
//            )
//
//            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
//
//            GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)
//
//            shaderProgram = Gl2Utils.createGlProgram(
//                Gl2Utils.uRes(context.resources, "shader/base_vert.glsl"),
//                Gl2Utils.uRes(context.resources, "shader/base_frag.glsl")
//            )
//
//            GLES20.glLinkProgram(shaderProgram)
//            baseTextureList.forEach {
//                it.onSurfaceCreated()
//            }
//        }
//
//        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
//            GLES20.glViewport(0, 0, width, height)
//            baseTextureList.forEach {
//                it.onSurfaceChanged(width, height)
//            }
//        }
//
//        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//            // 释放 EGL 资源
//            EGL14.eglDestroySurface(eglDisplay, eglSurface)
//            EGL14.eglDestroyContext(eglDisplay, eglContext)
//            eglDisplay = null
//            eglConfig = null
//            return true
//        }
//
//        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//            // 渲染到 TextureView
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//            // 使用着色器程序
//            GLES20.glUseProgram(shaderProgram)
//            baseTextureList.forEach {
//                it.onDrawFrame(shaderProgram)
//            }
//            EGL14.eglSwapBuffers(eglDisplay, eglSurface)
//        }
//
//        override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
//            // 更新 TextureView 的 SurfaceTexture
//            textureView.updateTexImage()
//        }
//
//        // ... 其他方法
//        fun updateTexCord(coordinateRegion: CoordinateRegion) {
//            baseTextureList.forEachIndexed { index, iBaseTexture ->
//                when (index) {
//                    2 -> {
//                        iBaseTexture.updateTexCord(
//                            coordinateRegion.offSet(
//                                0f,
//                                coordinateRegion.getHeight() + 50f
//                            )
//                        )
//                    }
//
//                    3 -> {
//                        iBaseTexture.updateTexCord(
//                            coordinateRegion.offSet(
//                                0f,
//                                coordinateRegion.getHeight() * 2 + 100f
//                            )
//                        )
//                    }
//
//                    0 -> {
//                        iBaseTexture.updateTexCord(coordinateRegion)
//                    }
//
//                    4, 5 -> {
//                        iBaseTexture.updateTexCord(
//                            CoordinateRegion().generateCoordinateRegion(
//                                (iBaseTexture.getScreenWidth() - coordinateRegion.getWidth() - 200),
//                                200f,
//                                coordinateRegion.getWidth().toInt(),
//                                coordinateRegion.getHeight().toInt()
//                            )
//                        )
//                    }
//
//                    else -> {
//                        iBaseTexture.updateTexCord(coordinateRegion)
//                    }
//                }
//            }
//        }
//
//
//        fun loadTexture(resourceId: Int) {
//            baseTextureList.forEachIndexed { index, iBaseTexture ->
//                when (index) {
//                    4, 5 -> iBaseTexture.updateTextureInfo(
//                        iBaseTexture.getTextureInfo().generateBitmapTexture(context, resourceId),
//                        true
//                    )
//
//                    else -> iBaseTexture.updateTextureInfo(
//                        iBaseTexture.getTextureInfo().generateBitmapTexture(context, resourceId)
//                    )
//                }
//
//            }
//        }
//
//
//
//}