package com.example.util

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.sqrt

class HandlerTouchDrag {
    var touchListener: TouchDragListener? = null
    var touchDragListenerHV: TouchDragListenerHV? = null
    var touchStateListenerL: TouchStateListener? = null

    private val zoomCenterPoint = floatArrayOf(0.0f, 0.0f)
    private val translateStartPoint = floatArrayOf(0.0f, 0.0f)

    private var lastDistance = 0.0f
    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    private var isHorizontalZoom = false

    /**
     * 移动和放缩是否互斥
     */
    var mutexMoveOrZoom: Boolean = false
    private var isZoom: Boolean = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStateListenerL?.onDown(event)
                event.actionIndex.also { pointerIndex ->
                    translateStartPoint[0] = event.getX(pointerIndex)
                    translateStartPoint[1] = event.getY(pointerIndex)
                }

                mActivePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                isZoom = true
                if (event.pointerCount >= 2) {
                    touchStateListenerL?.onMultipleFingersAction()
                }

                calculateCenter(event).also {
                    zoomCenterPoint[0] = it[0]
                    zoomCenterPoint[1] = it[1]
                    translateStartPoint[0] = it[0]
                    translateStartPoint[1] = it[1]
                }

                lastDistance = spacing(event)

            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount <= 2) {
                    touchStateListenerL?.onSingleFingersAction()
                    isZoom = false
                }
                event.actionIndex.also { pointerIndex ->
                    val pointerId = event.getPointerId(pointerIndex)
                    if (pointerId == mActivePointerId) {
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        translateStartPoint[0] = event.getX(newPointerIndex)
                        translateStartPoint[1] = event.getY(newPointerIndex)
                        mActivePointerId = event.getPointerId(newPointerIndex)
                    } else {
                        translateStartPoint[0] =
                            event.getX(event.findPointerIndex(mActivePointerId))
                        translateStartPoint[1] =
                            event.getY(event.findPointerIndex(mActivePointerId))
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                touchStateListenerL?.onMove()
                val translateEndPoint = floatArrayOf(0.0f, 0.0f)
                event.findPointerIndex(mActivePointerId).let { pointerIndex ->
                    translateEndPoint[0] = event.getX(pointerIndex)
                    translateEndPoint[1] = event.getY(pointerIndex)
                }

                if (event.pointerCount >= 2) {
                    calculateCenter(event).also {
                        zoomCenterPoint[0] = it[0]
                        zoomCenterPoint[1] = it[1]

                        translateEndPoint[0] = it[0]
                        translateEndPoint[1] = it[1]
                    }

                    val currentDistance = spacing(event)
                    val distanceDiff = currentDistance - lastDistance
                    if (abs(distanceDiff) > MIN_THRESHOLD) {

                        touchListener?.onZoom(
                            zoomCenterPoint[0],
                            zoomCenterPoint[1],
                            distanceDiff,
                            startPoint = translateStartPoint,
                            endPoint = translateEndPoint
                        )

                        if (isHorizontalZoom) {
                            touchDragListenerHV?.onZoomX(
                                zoomCenterPoint[0],
                                zoomCenterPoint[1],
                                distanceDiff,
                                startPoint = translateStartPoint,
                                endPoint = translateEndPoint
                            )
                        } else {
                            touchDragListenerHV?.onZoomY(
                                zoomCenterPoint[0],
                                zoomCenterPoint[1],
                                distanceDiff,
                                startPoint = translateStartPoint,
                                endPoint = translateEndPoint
                            )
                        }

                        lastDistance = currentDistance
                    }

                }

                if (mutexMoveOrZoom){

                    if (!isZoom){
                        val xDiff = translateEndPoint[0] - translateStartPoint[0]
                        val yDiff = translateEndPoint[1] - translateStartPoint[1]

                        if (abs(xDiff) > MIN_THRESHOLD || abs(yDiff) > MIN_THRESHOLD) {
                            touchListener?.onDrag(translateStartPoint, translateEndPoint, xDiff, yDiff)
                            touchDragListenerHV?.onDrag(
                                translateStartPoint,
                                translateEndPoint,
                                xDiff,
                                yDiff
                            )
                        }

                        translateStartPoint[0] = translateEndPoint[0]
                        translateStartPoint[1] = translateEndPoint[1]
                    }
                }else {
                    val xDiff = translateEndPoint[0] - translateStartPoint[0]
                    val yDiff = translateEndPoint[1] - translateStartPoint[1]

                    if (abs(xDiff) > MIN_THRESHOLD || abs(yDiff) > MIN_THRESHOLD) {
                        touchListener?.onDrag(translateStartPoint, translateEndPoint, xDiff, yDiff)
                        touchDragListenerHV?.onDrag(
                            translateStartPoint,
                            translateEndPoint,
                            xDiff,
                            yDiff
                        )
                    }
                    translateStartPoint[0] = translateEndPoint[0]
                    translateStartPoint[1] = translateEndPoint[1]
                }
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                isZoom = false
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                touchStateListenerL?.onUp()
            }
        }
        return true
    }

    /**
     * 计算连个手指连线中心点
     */
    private fun calculateCenter(event: MotionEvent): FloatArray {
        //计算起点中心坐标
        val x0 = event.getX(0)
        val y0 = event.getY(0)

        val x1 = event.getX(1)
        val y1 = event.getY(1)

        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            isHorizontalZoom = abs(x1 - x0) >= abs(abs(y1 - y0))
        }

        return floatArrayOf(
            x0 + (x1 - x0) / 2.0f,
            y0 + (y1 - y0) / 2.0f
        )
    }


    /**
     * 计算两个点的距离
     *
     * @param event
     * @return
     */
    private fun  spacing(event: MotionEvent): Float {
        return if (event.pointerCount == 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            sqrt((x * x + y * y))
        } else 0.0f
    }

    companion object {
        private const val MIN_THRESHOLD = 1
    }

    /**
     * 大小的放缩
     */
    interface TouchDragListener {
        fun onDrag(startPoint: FloatArray, endPoint: FloatArray, xDiff: Float, yDiff: Float)

        /**
         * @param centerX 手指中心的坐标x
         * @param centerY 手指中心的坐标y
         * @param zoomDist 放缩比例 正直是放大，负值是缩小
         */
        fun onZoom(
            centerX: Float,
            centerY: Float,
            zoomDist: Float,
            startPoint: FloatArray,
            endPoint: FloatArray
        )
    }

    interface TouchStateListener {
        fun onDown(event: MotionEvent)
        fun onMove()
        fun onMultipleFingersAction()
        fun onSingleFingersAction()
        fun onUp()
    }

    /**
     * 水平和垂直反向的放缩
     */
    interface TouchDragListenerHV {
        /**
         * @param
         */
        fun onDrag(startPoint: FloatArray, endPoint: FloatArray, xDiff: Float, yDiff: Float)

        /**
         * @param centerX 手指中心的坐标x
         * @param centerY 手指中心的坐标y
         * @param zoomDist 放缩比例 正直是放大，负值是缩小
         */
        fun onZoomX(
            centerX: Float,
            centerY: Float,
            zoomDist: Float,
            startPoint: FloatArray,
            endPoint: FloatArray
        )

        /**
         * @param centerX 手指中心的坐标x
         * @param centerY 手指中心的坐标y
         * @param zoomDist 放缩比例 正直是放大，负值是缩小
         */
        fun onZoomY(
            centerX: Float,
            centerY: Float,
            zoomDist: Float,
            startPoint: FloatArray,
            endPoint: FloatArray
        )
    }
}