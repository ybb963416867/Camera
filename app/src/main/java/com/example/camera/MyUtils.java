package com.example.camera;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class MyUtils
{
	// 定数
	private final static String TAG          = "Util";
	public static final  int    SIZEOF_FLOAT = Float.SIZE / 8;    // Float.SIZEで、float型のビット数が得られるので、8で割って、バイト数を得る

	// floatバッファーの作成
	public static FloatBuffer makeFloatBuffer( float[] arr )
	{
		ByteBuffer bb = ByteBuffer.allocateDirect( arr.length * SIZEOF_FLOAT );
		bb.order( ByteOrder.nativeOrder() );
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put( arr );
		fb.position( 0 );
		return fb;
	}

	// GLESシェーダーの読み込み
	private static int loadShader( int iShaderType, String strShaderCode )
	{
		// シェーダーの作成
		int iShader = GLES20.glCreateShader( iShaderType );
		if( 0 == iShader )
		{
			Log.e( TAG, "Cound not create shader. Shader Type = " + Integer.toString( iShader ) );
			return 0;
		}

		// シェーダーにシェーダーコードをセットし、コンパイル。
		GLES20.glShaderSource( iShader, strShaderCode );
		GLES20.glCompileShader( iShader );

		// コンパイル結果の確認
		int[] aiCompiled = new int[1];
		GLES20.glGetShaderiv( iShader, GLES20.GL_COMPILE_STATUS, aiCompiled, 0 );
		if( GLES20.GL_FALSE == aiCompiled[0] )
		{
			Log.e( TAG, "Cound not compile shader. Shader Type = " + Integer.toString( iShader ) );
			Log.e( TAG, "  " + GLES20.glGetShaderInfoLog( iShader ) );
			GLES20.glDeleteShader( iShader );
			return 0;
		}

		return iShader;
	}

	// GLESシェーダープログラムの作成
	public static int createProgram( String strVertexShaderCode, String strFragmentShaderCode )
	{
		// シェーダーの読み込み
		int iVertexShader = loadShader( GLES20.GL_VERTEX_SHADER, strVertexShaderCode );
		if( 0 == iVertexShader )
		{
			return 0;
		}
		// シェーダーの読み込み
		int iFragmentShader = loadShader( GLES20.GL_FRAGMENT_SHADER, strFragmentShaderCode );
		if( 0 == iFragmentShader )
		{
			return 0;
		}

		// シェーダープログラムの作成
		int iProgram = GLES20.glCreateProgram();
		if( 0 == iProgram )
		{
			return 0;
		}

		// シェーダープログラムにシェーダーを割り付け
		GLES20.glAttachShader( iProgram, iVertexShader );
		checkGlError( "glAttachShader" );
		GLES20.glAttachShader( iProgram, iFragmentShader );
		checkGlError( "glAttachShader" );

		// シェーダープログラムのリンク
		GLES20.glLinkProgram( iProgram );

		// リンク結果の確認
		int[] aiLinkStatus = new int[1];
		GLES20.glGetProgramiv( iProgram, GLES20.GL_LINK_STATUS, aiLinkStatus, 0 );
		if( GLES20.GL_FALSE == aiLinkStatus[0] )
		{
			Log.e( TAG, "Cound not link program." );
			Log.e( TAG, "  " + GLES20.glGetProgramInfoLog( iProgram ) );
			GLES20.glDeleteProgram( iProgram );
			return 0;
		}

		return iProgram;
	}

	// GLESエラーチェック
	public static void checkGlError( String str )
	{
		int iError;
		while( true )
		{
			iError = GLES20.glGetError();
			if( GLES20.GL_NO_ERROR == iError )
			{
				break;
			}

			Log.e( "checkGlError", str + " : Error Code = " + Integer.toString( iError ) );
			//throw new RuntimeException( str + " : Error Code = " + Integer.toString( iError ) );
		}
	}
}
