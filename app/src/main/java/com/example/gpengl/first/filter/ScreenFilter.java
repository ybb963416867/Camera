package com.example.gpengl.first.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;


import com.example.camera.R;
import com.example.gpengl.first.util.OpenUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * 负责往屏幕上渲染
 */
public class ScreenFilter {

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;
    private int vTexture;
    private int vMatrix;
    private int vCoord;
    private int vPosition;
    private int mProgram;
    private int mWidth;
    private int mHeight;

    public ScreenFilter(Context context) {
        //camera_vertex 中的内容读出字符串
        String vertexSource = OpenUtils.readRawTextFile(context, R.raw.camera_vertex);
        String fragSource = OpenUtils.readRawTextFile(context, R.raw.camera_frag);

        //通过字符串(代码)创建着色器程序
        //使用opengl
        //1、创建顶点着色器
        // 1.1
        int vShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        // 1.2 绑定代码到着色器中去
        GLES20.glShaderSource(vShaderId, vertexSource);
        // 1.3 编译着色器代码
        GLES20.glCompileShader(vShaderId);
        //主动获取成功、失败 (如果不主动查询，只输出 一条 GLERROR之类的日志，很难定位到到底是那里出错)
        int[] status = new int[1];
        GLES20.glGetShaderiv(vShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 顶点着色器配置失败!");
        }
        //2、创建片元着色器
        int fShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShaderId, fragSource);
        GLES20.glCompileShader(fShaderId);
        GLES20.glGetShaderiv(fShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 片元着色器配置失败!");
        }
        //3、创建着色器程序 (GPU上的小程序)
        mProgram = GLES20.glCreateProgram();
        //把着色器塞到程序当中
        GLES20.glAttachShader(mProgram, vShaderId);
        GLES20.glAttachShader(mProgram, fShaderId);

        //链接着色器
        GLES20.glLinkProgram(mProgram);

        //获得程序是否配置成功
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("ScreenFilter 着色器程序配置失败!");
        }

        //因为已经塞到着色器程序中了，所以删了没关系
        GLES20.glDeleteShader(vShaderId);
        GLES20.glDeleteShader(fShaderId);


        //获得着色器程序中的变量的索引， 通过这个索引来给着色器中的变量赋值
        /**
         * 顶点
         */
        vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgram, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //片元
        vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");


        //创建一个数据缓冲区
        //4个点 每个点两个数据(x,y) 数据类型float
        //顶点坐标
        mVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.clear();
        float[] v = {-1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f};
        mVertexBuffer.put(v);


        mTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.clear();
//        float[] t = {0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f};
        //旋转
//        float[] t = {1.0f, 1.0f,
//                1.0f, 0.0f,
//                0.0f, 1.0f,
//                0.0f, 0.0f};
        //镜像
        float[] t = {1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 1.0f
        };
        mTextureBuffer.put(t);
    }


    public void onDrawFrame(int texture, float[] mtx) {
        //1、设置窗口大小
        //画画的时候 你的画布可以看成 10x10，也可以看成5x5 等等
        //设置画布的大小，然后画画的时候， 画布越大，你画上去的图像就会显得越小
        // x与y 就是从画布的哪个位置开始画
        GLES20.glViewport(0, 0, mWidth, mHeight);

        //使用着色器程序
        GLES20.glUseProgram(mProgram);

        // 怎么画？ 其实就是传值
        //2：xy两个数据 float的类型
        //1、将顶点数据传入，确定形状
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        //传了数据之后 激活
        GLES20.glEnableVertexAttribArray(vPosition);

        //2、将纹理坐标传入，采样坐标
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        //3、变换矩阵
        GLES20.glUniformMatrix4fv(vMatrix,1,false,mtx,0);

        //片元 vTexture 绑定图像数据到采样器
        //激活图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // 图像数据
        // 正常：GLES20.GL_TEXTURE_2D
        // surfaceTexure的纹理需要
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,texture);
        //传递参数 0：需要和纹理层GL_TEXTURE0对应
        GLES20.glUniform1i(vTexture,0);

        //参数传完了 通知opengl 画画 从第0点开始 共4个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

    }

    public void onReady(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
}
