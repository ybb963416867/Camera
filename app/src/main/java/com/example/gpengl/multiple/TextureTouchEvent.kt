package com.example.gpengl.multiple

import android.view.MotionEvent

interface TextureTouchEvent {
    fun onTouch(baseTexture: IBaseTexture, event: MotionEvent): Boolean
}