package com.nielsmasdorp.speculum.glkit.data;

import android.opengl.GLES20;
import android.util.Log;

import com.nielsmasdorp.speculum.glkit.GLError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;

/**
 * Created by vfierce on 11/26/17.
 */

public class Shader {
    int m_id = 0;
    String m_source;
    int m_type;
    String m_sourceName;

    public Shader(String src, int type) {
        m_source = src;
        m_type = type;
        m_sourceName = "<inline>";
    }

    public Shader(InputStream input, int type) throws IOException {
        m_type = type;

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }

        m_source = sb.toString();
        m_sourceName = input.toString();
    }

    public void create() throws GLError {
        assert(m_id == 0);
        m_id = GLES20.glCreateShader(m_type);
        GLES20.glShaderSource(m_id, m_source);
        assert(GLES20.glGetError() == GLES20.GL_NO_ERROR);
        GLES20.glCompileShader(m_id);
        IntBuffer buf = IntBuffer.allocate(1);
        GLES20.glGetShaderiv(m_id, GLES20.GL_COMPILE_STATUS, buf);
        if (buf.get(0) == GLES20.GL_FALSE) {
            throw new GLError("Failed to compile shader " + m_sourceName + ": " + GLES20.glGetShaderInfoLog(m_id));
        }
    }
}
