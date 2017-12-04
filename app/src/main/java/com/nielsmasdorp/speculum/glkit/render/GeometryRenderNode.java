package com.nielsmasdorp.speculum.glkit.render;

import android.opengl.GLES20;

import com.nielsmasdorp.speculum.glkit.GLError;
import com.nielsmasdorp.speculum.glkit.data.Geometry;
import com.nielsmasdorp.speculum.glkit.data.Material;
import com.nielsmasdorp.speculum.glkit.data.VertexAttribute;
import com.nielsmasdorp.speculum.glkit.data.VertexFormat;

import java.util.Set;

/**
 * Created by vfierce on 12/3/17.
 */

public class GeometryRenderNode<T> extends RenderNode {
    Material m_material = null;
    Geometry<T> m_geometry = null;
    int m_type;

    public GeometryRenderNode() {
    }

    public void setDrawType(int glType) {
        m_type = glType;
    }

    public GeometryRenderNode(Material material, Geometry<T> geometry, int glType) {
        m_material = material;
        m_geometry = geometry;
        m_type = glType;
    }

    public Geometry<T> getGeometry() {
        return m_geometry;
    }

    public void setGeometry(Geometry<T> geom) {
        m_geometry = geom;
    }

    public Material getMaterial() {
        return m_material;
    }

    public void setMaterial(Material material) {
        m_material = material;
    }

    public void render() throws GLError {
        m_material.bind();
        m_geometry.bind();
        GLES20.glDrawArrays(m_type, 0, m_geometry.vertexData().raw().length);
        m_geometry.unbind();
        m_material.unbind();
    }
}
