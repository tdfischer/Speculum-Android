package com.nielsmasdorp.speculum.glkit.data;

import android.opengl.GLES20;

/**
 * Created by vfierce on 12/3/17.
 */

public class Geometry<T> {
    Buffer<T> m_vertBuf;

    public Geometry(Buffer<T> vertBuf) {
        m_vertBuf = vertBuf;
    }

    public Buffer<T> vertexData() {
        return m_vertBuf;
    }

    public void bind() {
        m_vertBuf.bind();
    }

    public void unbind() {
        m_vertBuf.unbind();
    }
}
