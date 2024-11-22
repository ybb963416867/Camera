package com.example.camera

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.view.MyGlSurface

class Test2Activity : AppCompatActivity() {
    private lateinit var m_glsurfaceview: GLSurfaceView
    private lateinit var m_surfacetexturerenderer: SurfaceTextureRenderer
    private lateinit var myGlSurface: MyGlSurface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // GLSurfaceViewの取得
        m_glsurfaceview = findViewById<View>(R.id.glsurfaceview) as GLSurfaceView
        myGlSurface = findViewById(R.id.glsurfaceview2)


        // GLESバージョンとしてGLES 2.0 を指定
        m_glsurfaceview.setEGLContextClientVersion(2)


        // Rendererの作成
        m_surfacetexturerenderer = SurfaceTextureRenderer(m_glsurfaceview)


        // Rendererの作成と、GLSurfaceViewへのセット
        m_glsurfaceview.setRenderer(m_surfacetexturerenderer)


        //  絶え間ないレンダリング
        m_glsurfaceview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        val fl = findViewById<FrameLayout>(R.id.fl)
        findViewById<View>(R.id.but_start).setOnClickListener { v: View? ->
            Log.d("ybb", " width = " + fl.width + " height = " + fl.height)
            m_surfacetexturerenderer.startDrawInSurface(fl, fl.width, fl.height)
            myGlSurface.setViewInfo(fl, fl.width, fl.height)
            myGlSurface.updateViewTexture()

        }




    }

}