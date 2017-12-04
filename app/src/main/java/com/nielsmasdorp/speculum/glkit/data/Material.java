package com.nielsmasdorp.speculum.glkit.data;

import android.opengl.GLES20;

import com.nielsmasdorp.speculum.glkit.GLError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vfierce on 11/26/17.
 */

public class Material {
    ShaderProgram m_shader;
    VertexAttribute[] m_vertAttributes;
    HashMap<String, VertexFormat> m_attributes;
    HashMap<String, Float> m_uniforms;

    public Material(ShaderProgram shader, VertexAttribute[] attributes) {
        m_attributes = new HashMap<>();
        m_shader = shader;
        m_uniforms = new HashMap<>();
        m_vertAttributes = attributes;
    }

    public void setUniform(String name, float val) {
        synchronized (m_uniforms) {
            m_uniforms.put(name, val);
        }
    }

    public void bind() throws GLError {
        m_shader.bind();
        synchronized (m_uniforms) {
            for (Map.Entry<String, Float> kv : m_uniforms.entrySet()) {
                GLES20.glUniform1f(m_shader.uniformLocation(kv.getKey()), kv.getValue());
            }
        }
        enableAttributes();
    }

    public void enableAttributes() {
        synchronized (m_vertAttributes) {
            for (VertexAttribute attr : m_vertAttributes) {
                VertexFormat fmt = attr.format;
                int location = m_shader.attributeLocation(attr.name);
                assert(location != -1);
                GLES20.glEnableVertexAttribArray(location);
                GLES20.glVertexAttribPointer(location, fmt.size, fmt.type, fmt.normalized, fmt.stride, fmt.offset);
            }
        }
    }

    public void unbind() {
        m_shader.unbind();
        synchronized (m_attributes) {
            for (String idx : m_attributes.keySet()) {
                GLES20.glDisableVertexAttribArray(m_shader.attributeLocation(idx));
            }
        }
    }
}