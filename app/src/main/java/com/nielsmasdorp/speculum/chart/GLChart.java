package com.nielsmasdorp.speculum.chart;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.nielsmasdorp.speculum.glkit.GLKitView;
import com.nielsmasdorp.speculum.glkit.data.Bufferable;
import com.nielsmasdorp.speculum.glkit.GLError;
import com.nielsmasdorp.speculum.glkit.scene.Scene;

import java.nio.ByteBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by vfierce on 11/26/17.
 */

public class GLChart extends GLKitView {
    LineNode m_lineNode;

    /*
    Random rnd = new Random();
            push(rnd.nextFloat());
     */

    public GLChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_lineNode = new LineNode(getResources().getAssets());
        getScene().setRoot(m_lineNode);
    }

    public GLChart(Context context) {
        super(context);
        m_lineNode = new LineNode(getResources().getAssets());
        getScene().setRoot(m_lineNode);
    }

    public void push(float val) { m_lineNode.push(val); }

}