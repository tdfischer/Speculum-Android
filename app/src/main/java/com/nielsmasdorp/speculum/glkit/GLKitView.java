package com.nielsmasdorp.speculum.glkit;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.nielsmasdorp.speculum.chart.GLChart;
import com.nielsmasdorp.speculum.glkit.scene.Scene;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by vfierce on 12/4/17.
 */

public class GLKitView extends GLSurfaceView {
    Scene m_scene;

    private class Renderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glLineWidth(2.0f);
            m_scene.reset();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_ALPHA);

            try {
                m_scene.render();
            } catch (GLError e) {
                Log.e("opengl", "Render failure", e);
            }
        }
    }

    public void setScene(Scene scene) {
        m_scene = scene;
    }

    public GLKitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_scene = new Scene();
        setEGLContextClientVersion(2);
        setRenderer(new Renderer());
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public GLKitView(Context context) {
        super(context);
        m_scene = new Scene();
        setEGLContextClientVersion(2);
        setRenderer(new Renderer());
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    protected Scene getScene() {
        return m_scene;
    }
}
