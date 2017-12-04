package com.nielsmasdorp.speculum.glkit.data;

import android.opengl.GLES20;
import android.util.Log;

import com.nielsmasdorp.speculum.glkit.GLError;

import java.nio.IntBuffer;
import java.util.HashMap;

/**
 * Created by vfierce on 11/26/17.
 */

public class ShaderProgram {
    public ShaderProgram(Shader[] shaders) {
        m_shaders = shaders;
    }

    private Shader[] m_shaders;
    private int m_id = 0;
    private boolean m_needsCreated = true;

    private HashMap<String, Integer> m_attribLocations;
    private HashMap<String, Integer> m_uniformLocations;

    private void create() throws GLError {
        assert(m_id == 0);
        m_attribLocations = new HashMap<>();
        m_uniformLocations = new HashMap<>();
        m_id = GLES20.glCreateProgram();
        assert(GLES20.glGetError() == GLES20.GL_NO_ERROR);
        for(Shader shader : m_shaders) {
            shader.create();
            GLES20.glAttachShader(m_id, shader.m_id);
            assert(GLES20.glGetError() == GLES20.GL_NO_ERROR);
        }

        IntBuffer buf = IntBuffer.allocate(1);
        GLES20.glLinkProgram(m_id);
        GLES20.glGetProgramiv(m_id, GLES20.GL_LINK_STATUS, buf);
        if (buf.get(0) != GLES20.GL_TRUE) {
            throw new GLError("Failed to link shader program: " + GLES20.glGetProgramInfoLog(m_id));
        }
    }

    public int attributeLocation(String name) {
        if (!m_attribLocations.containsKey(name)) {
            int location = GLES20.glGetAttribLocation(m_id, name);
            m_attribLocations.put(name, location);
            if (location == -1) {
                Log.w("glkit", "Invalid attribute name: " + name);
            }
        }
        return m_attribLocations.get(name);
    }

    public void bind() throws GLError {
        if (m_needsCreated) {
            create();
            m_needsCreated = false;
        }
        GLES20.glUseProgram(m_id);
        assert(GLES20.glGetError() == GLES20.GL_NO_ERROR);
    }

    public void unbind() {
        GLES20.glUseProgram(0);
    }

    public int uniformLocation(String name) {
        if (!m_uniformLocations.containsKey(name)) {
            int location = GLES20.glGetUniformLocation(m_id, name);
            m_uniformLocations.put(name, location);
            if (location == -1) {
                Log.w("glkit", "Invalid uniform name: " + name);
            }
        }
        return m_uniformLocations.get(name);
    }
}
