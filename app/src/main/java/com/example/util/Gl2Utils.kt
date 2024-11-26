/*
 *
 * FastDrawerHelper.java
 *
 * Created by Wuwang on 2016/11/17
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.example.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Arrays
import javax.microedition.khronos.opengles.GL10

/**
 * Description:
 */
object Gl2Utils {
    const val TAG: String = "Gl2Utilss"
    var DEBUG: Boolean = true
    private const val SIZEOF_FLOAT = 4

    /**
     * 最大
     */
    const val TYPE_FITXY: Int = 0

    /**
     * 中间裁剪
     */
    const val TYPE_CENTERCROP: Int = 1

    /**
     * 居中
     */
    const val TYPE_CENTERINSIDE: Int = 2

    /**
     * 从开始裁剪
     */
    const val TYPE_FITSTART: Int = 3

    /**
     *
     */
    const val TYPE_FITEND: Int = 4

    @JvmStatic
    fun getPicOriginMatrix(
        matrix: FloatArray?, imgWidth: Int, imgHeight: Int, viewWidth: Int, viewHeight: Int
    ) {
        val bitmapAspectRatio = imgWidth / imgHeight.toFloat()
        val viewAspectRatio = viewWidth / viewHeight.toFloat()
        val mProjectMatrix = FloatArray(16)
        val mViewMatrix = FloatArray(16)
        if (viewWidth > viewHeight) {
            if (bitmapAspectRatio > viewAspectRatio) {
                Matrix.orthoM(
                    mProjectMatrix,
                    0,
                    -1f,
                    1f,
                    -1 / (viewAspectRatio / bitmapAspectRatio),
                    1 / (viewAspectRatio / bitmapAspectRatio),
                    3f,
                    7f
                )
            } else {
                Matrix.orthoM(
                    mProjectMatrix,
                    0,
                    -viewAspectRatio / bitmapAspectRatio,
                    viewAspectRatio / bitmapAspectRatio,
                    -1f,
                    1f,
                    3f,
                    7f
                )
            }
        } else {
            if (bitmapAspectRatio > viewAspectRatio) {
                Matrix.orthoM(
                    mProjectMatrix,
                    0,
                    -1f,
                    1f,
                    -1 / viewAspectRatio * bitmapAspectRatio,
                    1 / viewAspectRatio * bitmapAspectRatio,
                    3f,
                    7f
                )
            } else {
                Matrix.orthoM(
                    mProjectMatrix,
                    0,
                    -1 / bitmapAspectRatio * viewAspectRatio,
                    1 / bitmapAspectRatio * viewAspectRatio,
                    -1f,
                    1f,
                    3f,
                    7f
                )
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(matrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    /**
     * 左上对齐
     *
     * @param matrix
     * @param imgWidth
     * @param imgHeight
     * @param viewWidth
     * @param viewHeight
     * @param type       0 center  1 左上 2 右上 3左下  4 右下  5 中上 6 中下
     */
    @JvmStatic
    fun getPicOriginMatrix(
        matrix: FloatArray?,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
        type: Int
    ) {
        val bitmapAspectRatio = imgWidth / imgHeight.toFloat()
        val viewAspectRatio = viewWidth / viewHeight.toFloat()
        val mProjectMatrix = FloatArray(16)
        val mViewMatrix = FloatArray(16)
        if (type == 0) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1 / (viewAspectRatio / bitmapAspectRatio),
                        1 / (viewAspectRatio / bitmapAspectRatio),
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -viewAspectRatio / bitmapAspectRatio,
                        viewAspectRatio / bitmapAspectRatio,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1 / viewAspectRatio * bitmapAspectRatio,
                        1 / viewAspectRatio * bitmapAspectRatio,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1 / bitmapAspectRatio * viewAspectRatio,
                        1 / bitmapAspectRatio * viewAspectRatio,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else if (type == 1) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        1 - (1 / (viewAspectRatio / bitmapAspectRatio)) * 2,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        (viewAspectRatio / bitmapAspectRatio) * 2 - 1,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        1 - (1 / viewAspectRatio * bitmapAspectRatio) * 2,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        (1 / bitmapAspectRatio * viewAspectRatio) * 2 - 1,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else if (type == 2) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        1 - (1 / (viewAspectRatio / bitmapAspectRatio)) * 2,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        1 - (viewAspectRatio / bitmapAspectRatio) * 2,
                        1f,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        1 - (1 / viewAspectRatio * bitmapAspectRatio) * 2,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        1 - (1 / bitmapAspectRatio * viewAspectRatio) * 2,
                        1f,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else if (type == 3) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1f,
                        (1 / (viewAspectRatio / bitmapAspectRatio)) * 2 - 1,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        (viewAspectRatio / bitmapAspectRatio) * 2 - 1,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1f,
                        (1 / viewAspectRatio * bitmapAspectRatio) * 2 - 1,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        (1 / bitmapAspectRatio * viewAspectRatio) * 2 - 1,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else if (type == 4) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1f,
                        (1 / (viewAspectRatio / bitmapAspectRatio)) * 2 - 1,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        1 - (viewAspectRatio / bitmapAspectRatio) * 2,
                        1f,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1f,
                        (1 / viewAspectRatio * bitmapAspectRatio) * 2 - 1,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        1 - (1 / bitmapAspectRatio * viewAspectRatio) * 2,
                        1f,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else if (type == 5) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        1 - 1 / (viewAspectRatio / bitmapAspectRatio) * 2,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -viewAspectRatio / bitmapAspectRatio,
                        viewAspectRatio / bitmapAspectRatio,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        1 - 1 / viewAspectRatio * bitmapAspectRatio * 2,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1 / bitmapAspectRatio * viewAspectRatio,
                        1 / bitmapAspectRatio * viewAspectRatio,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else if (type == 6) {
            if (viewWidth > viewHeight) {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1f,
                        1 / (viewAspectRatio / bitmapAspectRatio) * 2 - 1,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -viewAspectRatio / bitmapAspectRatio,
                        viewAspectRatio / bitmapAspectRatio,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (bitmapAspectRatio > viewAspectRatio) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1f,
                        1 / viewAspectRatio * bitmapAspectRatio * 2 - 1,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1 / bitmapAspectRatio * viewAspectRatio,
                        1 / bitmapAspectRatio * viewAspectRatio,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            }
        } else {
            throw IllegalArgumentException("please check argument type")
        }

        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(matrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    /**
     * @param matrix     4*4的浮点矩阵
     * @param type
     * @param imgWidth
     * @param imgHeight
     * @param viewWidth
     * @param viewHeight
     */
    fun getMatrix(
        matrix: FloatArray?,
        type: Int,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            val projection = FloatArray(16)
            val camera = FloatArray(16)
            if (type == TYPE_FITXY) {
                Matrix.orthoM(projection, 0, -1f, 1f, -1f, 1f, 1f, 3f)
                Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                //矩阵乘法Matrix.multiplyMM();
                /**
                 * result
                 * The float array that holds the result. 保存结果的浮点数数组。
                 * resultOffset
                 * The offset into the result array where the result is stored.
                 * lhs
                 * The float array that holds the left-hand-side matrix. 保存左侧矩阵的浮点数数组。
                 * lhsOffset
                 * The offset into the lhs array where the lhs is stored
                 * rhs
                 * The float array that holds the right-hand-side matrix. 包含右侧矩阵的浮点数数组
                 * rhsOffset
                 * The offset into the rhs array where the rhs is stored.
                 * ————————————————
                 */
                Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
            }
            val bitmapAspectRatioView = viewWidth.toFloat() / viewHeight
            val bitmapAspectRatioImg = imgWidth.toFloat() / imgHeight
            if (bitmapAspectRatioImg > bitmapAspectRatioView) {
                when (type) {
                    TYPE_CENTERCROP -> Matrix.orthoM(
                        projection,
                        0,
                        -bitmapAspectRatioView / bitmapAspectRatioImg,
                        bitmapAspectRatioView / bitmapAspectRatioImg,
                        -1f,
                        1f,
                        1f,
                        3f
                    )

                    TYPE_CENTERINSIDE -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        -bitmapAspectRatioImg / bitmapAspectRatioView,
                        bitmapAspectRatioImg / bitmapAspectRatioView,
                        1f,
                        3f
                    )

                    TYPE_FITSTART -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        1 - 2 * bitmapAspectRatioImg / bitmapAspectRatioView,
                        1f,
                        1f,
                        3f
                    )

                    TYPE_FITEND -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        -1f,
                        2 * bitmapAspectRatioImg / bitmapAspectRatioView - 1,
                        1f,
                        3f
                    )
                }
            } else {
                when (type) {
                    TYPE_CENTERCROP -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        1f,
                        -bitmapAspectRatioImg / bitmapAspectRatioView,
                        bitmapAspectRatioImg / bitmapAspectRatioView,
                        1f,
                        3f
                    )

                    TYPE_CENTERINSIDE -> Matrix.orthoM(
                        projection,
                        0,
                        -bitmapAspectRatioView / bitmapAspectRatioImg,
                        bitmapAspectRatioView / bitmapAspectRatioImg,
                        -1f,
                        1f,
                        1f,
                        3f
                    )

                    TYPE_FITSTART -> Matrix.orthoM(
                        projection,
                        0,
                        -1f,
                        2 * bitmapAspectRatioView / bitmapAspectRatioImg - 1,
                        -1f,
                        1f,
                        1f,
                        3f
                    )

                    TYPE_FITEND -> Matrix.orthoM(
                        projection,
                        0,
                        1 - 2 * bitmapAspectRatioView / bitmapAspectRatioImg,
                        1f,
                        -1f,
                        1f,
                        1f,
                        3f
                    )
                }
            }
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
    }

    /**
     * @param matrix
     * @param imgWidth
     * @param imgHeight
     * @param viewWidth
     * @param viewHeight
     */
    fun getCenterInsideMatrix(
        matrix: FloatArray?, imgWidth: Int, imgHeight: Int, viewWidth: Int, viewHeight: Int
    ) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            val bitmapAspectRatioView = viewWidth.toFloat() / viewHeight
            val bitmapAspectRatioImg = imgWidth.toFloat() / imgHeight
            val projection = FloatArray(16)
            val camera = FloatArray(16)
            if (bitmapAspectRatioImg > bitmapAspectRatioView) {
                Matrix.orthoM(
                    projection,
                    0,
                    -1f,
                    1f,
                    -bitmapAspectRatioImg / bitmapAspectRatioView,
                    bitmapAspectRatioImg / bitmapAspectRatioView,
                    1f,
                    3f
                )
            } else {
                Matrix.orthoM(
                    projection,
                    0,
                    -bitmapAspectRatioView / bitmapAspectRatioImg,
                    bitmapAspectRatioView / bitmapAspectRatioImg,
                    -1f,
                    1f,
                    1f,
                    3f
                )
            }
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
    }


    @JvmStatic
    fun rotate(m: FloatArray, angle: Float): FloatArray {
        Matrix.rotateM(m, 0, angle, 0f, 0f, 1f)
        return m
    }

    @JvmStatic
    fun flip(m: FloatArray, x: Boolean, y: Boolean): FloatArray {
        if (x || y) {
            Matrix.scaleM(m, 0, (if (x) -1 else 1).toFloat(), (if (y) -1 else 1).toFloat(), 1f)
        }
        return m
    }

    @JvmStatic
    val originalMatrix: FloatArray
        /**
         * @return 原始矩阵|| 单位矩阵
         */
        get() = floatArrayOf(
            1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f
        )

    //通过路径加载Assets中的文本内容
    @JvmStatic
    fun uRes(mRes: Resources, path: String): String? {
        val result = StringBuilder()
        try {
            val `is` = mRes.assets.open(path)
            var ch: Int
            val buffer = ByteArray(1024)
            while (-1 != (`is`.read(buffer).also { ch = it })) {
                result.append(String(buffer, 0, ch))
            }
        } catch (e: Exception) {
            return null
        }
        return result.toString().replace("\\r\\n".toRegex(), "\n")
    }

    fun createGlProgramByRes(res: Resources, vert: String, frag: String): Int {
        return createGlProgram(uRes(res, vert), uRes(res, frag))
    }

    //创建GL程序
    @JvmStatic
    fun createGlProgram(vertexSource: String?, fragmentSource: String?): Int {
        val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertex == 0) return 0
        val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragment == 0) return 0
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                glError(1, "Could not link program:" + GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    //加载shader
    fun loadShader(shaderType: Int, source: String?): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (0 != shader) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                glError(1, "Could not compile shader:$shaderType")
                glError(1, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    fun glError(code: Int, index: Any) {
        if (DEBUG && code != 0) {
            Log.e(TAG, "glError:$code---$index")
        }
    }

    fun scale(m: FloatArray, x: Float, y: Float): FloatArray {
        Matrix.scaleM(m, 0, x, y, 1f)
        return m
    }

    @JvmStatic
    fun createTextureId(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE
        )
        return texture[0]
    }


    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     *
     *
     * Throws a RuntimeException if the location is invalid.
     */
    @JvmStatic
    fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Unable to locate '$label' in program")
        }
    }

    /**
     * Creates a texture from raw data.
     *
     * @param data   Image data, in a "direct" ByteBuffer.
     * @param width  Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    fun createImageTexture(data: ByteBuffer?, width: Int, height: Int, format: Int): Int {
        val textureHandles = IntArray(1)

        GLES20.glGenTextures(1, textureHandles, 0)
        val textureHandle = textureHandles[0]
        checkGlError("glGenTextures")

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        checkGlError("loadImageTexture")

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, data
        )
        checkGlError("loadImageTexture")

        return textureHandle
    }

    /**
     * @param bmp 图片的bitmap
     * @return 纹理
     */
    @JvmStatic
    fun createTexture(bmp: Bitmap?): Int {
        val texture = IntArray(1)
        if (bmp != null && !bmp.isRecycled) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0)
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat()
            )
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
            Log.e(TAG, "有纹理")
            return texture[0]
        }
        Log.e(TAG, "没有纹理")
        return 0
    }


    /**
     * @param count 数量
     * @return 创建一个纹理数组
     * 这个纹理用于视频
     */
    @JvmStatic
    fun createOESTextureID(count: Int): IntArray {
        val texture = IntArray(count)
        GLES20.glGenTextures(count, texture, 0)
        for (i in 0 until count) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[i])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat()
            )
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
            )
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE
            )
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE
            )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }
        return texture
    }

    @JvmStatic
    fun create2DTexture(count: Int): IntArray {
        val texture = IntArray(count)
        //生成纹理
        GLES20.glGenTextures(count, texture, 0)
        for (i in 0 until count) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i])
            //生成纹理
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat()
            )
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }
        return texture
    }


    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }


    /**
     * @param count 数量
     * @return 创建一个纹理数组
     * 这个纹理用于视频
     */
    fun createTextureId(count: Int): IntArray {
        val texture = IntArray(count)
        GLES30.glGenTextures(count, texture, 0)
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat()
        )
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE
        )
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE
        )
        return texture
    }

    fun getFramebufferPixels(
        fboId: Int, width: Int, height: Int
    ): Triple<ByteArray, Int, Int> {
        // 绑定 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
        val buffer = readPixelsToByteBuffer(0, 0, width, height)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return Triple(buffer, width, height)
    }

    fun readPixelsToByteBuffer(x: Int = 0, y: Int = 0, width: Int, height: Int): ByteArray {
        // 计算像素数据的大小（RGBA格式，每个像素4字节）
        val byteCount = width * height * 4

        // 创建一个 ByteBuffer 来存储像素数据
        val pixelBuffer = ByteBuffer.allocateDirect(byteCount)
        pixelBuffer.order(ByteOrder.nativeOrder())

        // 将 OpenGL 上下文中的帧缓冲区内容读取到 ByteBuffer 中
        GLES30.glReadPixels(
            x, y, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixelBuffer
        )
        flipVertical(pixelBuffer, width, height)
        val byteArray = ByteArray(pixelBuffer.remaining())
        pixelBuffer.get(byteArray)
//        Log.d("ybb", "width: $width  height: $height length: ${byteArray.size}")
        return byteArray
    }

    private fun flipVertical(buffer: ByteBuffer, width: Int, height: Int, bytesPerPixel: Int = 4) {
        buffer.rewind() // 重置缓冲区位置
        val rowBuffer = ByteArray(width * bytesPerPixel) // 临时存储每行数据的缓冲区
        // 逐行翻转
        for (i in 0 until height / 2) {
            // 计算当前行和对应的对称行的索引
            val rowIndex1 = i * width * bytesPerPixel
            val rowIndex2 = (height - 1 - i) * width * bytesPerPixel

            // 将当前行数据复制到临时缓冲区
            buffer.position(rowIndex1)
            buffer[rowBuffer]

            // 将对称行数据复制到当前行位置
            buffer.position(rowIndex1)
            buffer.put(buffer.array(), rowIndex2, width * bytesPerPixel)

            // 将临时缓冲区中的数据复制到对称行位置
            buffer.position(rowIndex2)
            buffer.put(rowBuffer)
        }
        buffer.rewind() // 重置缓冲区位置
    }


    /**
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    fun genColorImage(width: Int, height: Int, color: String): IntArray {
        val pixels = IntArray(width * height)
        val colorPixel = Color.parseColor(color)
        val red = Color.red(colorPixel)
        val green = Color.green(colorPixel)
        val blue = Color.blue(colorPixel)
        val alpha = Color.alpha(colorPixel)
//        val openGLColor = (red shl 24) or (green shl 16) or (blue shl 8) or alpha
//        val openGLColor = Color.argb(alpha, red, green, blue)
        val openGLColor = (alpha shl 24) or (blue shl 16) or (green shl 8) or red
        Arrays.fill(pixels, openGLColor)
        return pixels
    }


    /**
     *
     */
    fun Triple<ByteArray, Int, Int>.toBitmap(): Bitmap {
        return Bitmap.createBitmap(this.second, this.third, Bitmap.Config.ARGB_8888).also {
            it.copyPixelsFromBuffer(ByteBuffer.wrap(this.first))
        }
    }

    fun Bitmap.savaFile(path: String): Boolean = run {
        val result = FileUtils.saveImage(this, path)
        this.recycle()
        result
    }
}
