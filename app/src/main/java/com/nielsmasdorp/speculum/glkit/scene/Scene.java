package com.nielsmasdorp.speculum.glkit.scene;

import com.nielsmasdorp.speculum.glkit.render.GeometryRenderNode;
import com.nielsmasdorp.speculum.glkit.render.RenderNode;

/**
 * Created by vfierce on 12/4/17.
 */

public class Scene {
    SceneNode m_root;
    RenderNode m_rootRender = null;

    public void setRoot(SceneNode root) {
        m_root = root;
    }

    public void render() {
        m_rootRender = m_root.updateRenderNode(m_rootRender);
        m_rootRender.render();
    }

    public void reset() {
        m_rootRender = null;
    }
}
