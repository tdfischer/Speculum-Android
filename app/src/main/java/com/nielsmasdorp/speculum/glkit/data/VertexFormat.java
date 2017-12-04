package com.nielsmasdorp.speculum.glkit.data;

/**
 * Created by vfierce on 11/26/17.
 */

public class VertexFormat {
    public int size;
    public int type;
    public boolean normalized;
    public int stride;
    public int offset;

    public VertexFormat(int size, int type, boolean normalized, int stride, int offset) {
        this.size = size;
        this.type = type;
        this.normalized = normalized;
        this.stride = stride;
        this.offset = offset;
    }
}