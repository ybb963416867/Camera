package com.example.camera;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ShaderUtils {
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static int createProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(coords);
        buffer.position(0);
        return buffer;
    }

    public static FloatBuffer createFloatBuffer(float[] coords, int offset) {
        ByteBuffer bb = ByteBuffer.allocateDirect((coords.length - offset) * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        for (int i = offset; i < coords.length; i += 5) {
            buffer.put(coords, i, 2);  // Only the texture coordinates
        }
        buffer.position(0);
        return buffer;
    }

    public static ShortBuffer createShortBuffer(short[] indices) {
        ByteBuffer bb = ByteBuffer.allocateDirect(indices.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = bb.asShortBuffer();
        buffer.put(indices);
        buffer.position(0);
        return buffer;
    }
}

