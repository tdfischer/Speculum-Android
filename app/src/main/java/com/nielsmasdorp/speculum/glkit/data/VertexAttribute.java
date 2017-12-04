package com.nielsmasdorp.speculum.glkit.data;

/**
 * Created by vfierce on 12/4/17.
 */
public class VertexAttribute {
    public String name;
    public VertexFormat format;

    public VertexAttribute(String name, VertexFormat format) {
        this.name = name;
        this.format = format;
    }
}
