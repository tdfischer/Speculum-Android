package com.nielsmasdorp.speculum.glkit.data;

import android.opengl.GLES20;
import android.opengl.GLES31;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by vfierce on 11/26/17.
 */

public abstract class Buffer<T> {
    public Buffer(int target) {
        m_target = target;
    }

    public Buffer(int target, T[] buf) {
        m_target = target;
        m_data = buf;
    }

    protected int m_id;
    private int m_target;
    boolean m_needsCreated = true;

    private void create() {
        IntBuffer buf = IntBuffer.allocate(1);
        GLES31.glGenBuffers(1, buf);
        m_id = buf.get(0);
    }

    public void bind() {
        if (m_needsCreated) {
            create();
            GLES20.glGetError();
            m_needsCreated = false;
        }
        GLES20.glBindBuffer(m_target, m_id);
        GLES20.glGetError();
        if (m_needsRebuild) {
            loadData();
            m_needsRebuild = false;
        }
    }

    public void unbind() {
        GLES20.glBindBuffer(m_target, 0);
    }

    protected T[] m_data = null;
    boolean m_needsRebuild = true;

    public void setData(T[] data) {
        synchronized (m_data) {
            m_data = data;
            m_needsRebuild = true;
        }
    }

    public void setData(T data, int idx) {
        synchronized (m_data) {
            m_data[idx] = data;
            m_needsRebuild = true;
        }
    }

    public T[] raw() {
        synchronized (m_data) {
            return m_data;
        }
    }

    protected abstract void writeToBuffer(ByteBuffer buf);
    protected abstract int dataSize();

    private void loadData() {
        synchronized (m_data) {
            if (m_data != null) {
                ByteBuffer buf = ByteBuffer.allocateDirect(m_data.length * dataSize()).order(ByteOrder.nativeOrder());
                writeToBuffer(buf);
                buf.position(0);
                Log.d("glkit", "Uploading " + buf.capacity() + " " + m_data[0].getClass().getName() + " (" + m_data.length + " items, " + dataSize() + "B each)");
                GLES20.glBufferData(m_target, buf.capacity(), buf, GLES20.GL_DYNAMIC_DRAW);
            }
        }
    }
}