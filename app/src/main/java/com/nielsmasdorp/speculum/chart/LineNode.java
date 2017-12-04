package com.nielsmasdorp.speculum.chart;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;

import com.nielsmasdorp.speculum.glkit.data.Buffer;
import com.nielsmasdorp.speculum.glkit.data.Bufferable;
import com.nielsmasdorp.speculum.glkit.data.BufferableBuffer;
import com.nielsmasdorp.speculum.glkit.data.Geometry;
import com.nielsmasdorp.speculum.glkit.data.Material;
import com.nielsmasdorp.speculum.glkit.data.Shader;
import com.nielsmasdorp.speculum.glkit.data.ShaderProgram;
import com.nielsmasdorp.speculum.glkit.data.VertexAttribute;
import com.nielsmasdorp.speculum.glkit.data.VertexFormat;
import com.nielsmasdorp.speculum.glkit.render.GeometryRenderNode;
import com.nielsmasdorp.speculum.glkit.render.RenderNode;
import com.nielsmasdorp.speculum.glkit.scene.SceneNode;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by vfierce on 12/4/17.
 */
class LineNode extends SceneNode {

    class Coordinate implements Bufferable {
        public float x;
        public float y;

        public Coordinate(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void writeToBuffer(ByteBuffer buf) {
            buf.putFloat(x);
            buf.putFloat(y);
        }

        @Override
        public int bufferedSize() {
            return 4 * 2;
        }
    }

    int cursorX = 0;
    final int X_RANGE = 100;
    Coordinate[] m_plotData;

    AssetManager m_assets;
    public LineNode(AssetManager assets) {
        m_assets = assets;
        m_plotData = new Coordinate[X_RANGE];
        for (int i = 0; i < X_RANGE; i++) {
            float xPos = scaleX(i);
            m_plotData[i] = new Coordinate(xPos, (float) Math.sin((xPos * 5) * Math.PI));
        }
    }

    public void push(float val) {
        float renderX = scaleX(cursorX);
        m_plotData[cursorX].y = val;
        cursorX++;
        cursorX %= X_RANGE;
        /*if (m_renderNode != null) {
            float renderX = scaleX(cursorX);
            m_renderNode.getGeometry().vertexData().setData(new Coordinate(renderX, val), cursorX);
            float max = 0;
            float min = 0;
            for (Coordinate coord : m_renderNode.getGeometry().vertexData().raw()) {
                max = Math.max(coord.y, max);
                min = Math.min(coord.y, min);
            }
            float range = (float)Math.max(1.0, Math.max(Math.abs(max), Math.abs(min)));
            m_renderNode.getMaterial().setUniform("range", range);
            m_renderNode.getMaterial().setUniform("hue", 0.0f);
        }*/
    }

    float scaleX(int value) {
        return ((float)value / (float)X_RANGE) * 2 - 1;
    }

    @Override
    public RenderNode updateRenderNode(RenderNode renderNode) {
        GeometryRenderNode<Coordinate> geomRenderNode = (GeometryRenderNode<Coordinate>) renderNode;
        if (geomRenderNode == null) {
            geomRenderNode = new GeometryRenderNode<>();
            geomRenderNode.setDrawType(GLES20.GL_LINE_STRIP);
        }

        Material renderMat = geomRenderNode.getMaterial();
        if (renderMat == null) {
            Shader[] shaders = null;
            try {
                shaders = new Shader[]{
                        new Shader(m_assets.open("shaders/chart.vert"), GLES20.GL_VERTEX_SHADER),
                        new Shader(m_assets.open("shaders/chart.frag"), GLES20.GL_FRAGMENT_SHADER)
                };
            } catch (IOException e) {
                Log.e("glchart", "Could not load shaders", e);
                shaders = new Shader[]{
                        new Shader("uniform vec3 vPosition; void main() { gl_Position = vec4(vPosition.x, log(vPosition.x), 0.0, 1.0); }", GLES20.GL_VERTEX_SHADER),
                        new Shader("void main() { gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); }", GLES20.GL_FRAGMENT_SHADER)
                };
            }

            ShaderProgram chartShader = new ShaderProgram(shaders);
            VertexAttribute[] lineAttribs = {
                    new VertexAttribute("vPosition", new VertexFormat(2, GLES20.GL_FLOAT, false, 0, 0))
            };

            renderMat = new Material(chartShader, lineAttribs);
            geomRenderNode.setMaterial(renderMat);
        }
        renderMat.setUniform("offset", scaleX(cursorX) + 1);

        Geometry<Coordinate> renderGeom = geomRenderNode.getGeometry();
        if (renderGeom == null) {
            Buffer<Coordinate> vertBuf = new BufferableBuffer<>(GLES20.GL_ARRAY_BUFFER, m_plotData);
            renderGeom = new Geometry<>(vertBuf);
            geomRenderNode.setGeometry(renderGeom);
        }

        renderGeom.vertexData().setData(m_plotData);
        float max = 0;
        float min = 0;
        for (Coordinate coord : m_plotData) {
            max = Math.max(coord.y, max);
            min = Math.min(coord.y, min);
        }
        float range = (float)Math.max(1.0, Math.max(Math.abs(max), Math.abs(min)));
        renderMat.setUniform("range", range);

        return geomRenderNode;
    }
}
