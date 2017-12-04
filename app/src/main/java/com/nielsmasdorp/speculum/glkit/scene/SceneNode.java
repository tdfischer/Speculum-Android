package com.nielsmasdorp.speculum.glkit.scene;

import com.nielsmasdorp.speculum.glkit.render.RenderNode;

/**
 * Created by vfierce on 12/4/17.
 */

abstract public class SceneNode {
    public abstract RenderNode updateRenderNode(RenderNode renderNode);
}