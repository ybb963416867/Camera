package com.example.gpengl.multiple

import android.view.MotionEvent

interface TextureTouchEvent {
    fun acceptTouchEvent(event: MotionEvent):Boolean
    fun onTouch(baseTexture: IBaseTexture, event: MotionEvent): Boolean
}