package com.nielsmasdorp.speculum.glkit.data;

import java.nio.ByteBuffer;

/**
 * Created by vfierce on 11/26/17.
 */

public class FloatBuffer extends Buffer<Float> {

    public FloatBuffer(int target) {
        super(target);
    }

    public FloatBuffer(int target, float[] buf) {
        super(target, toFloatArray(buf));
    }

    private static Float[] toFloatArray(float[] val) {
        Float[] floatBuf = new Float[val.length];
        for(int i = 0; i < val.length;i++) {
            floatBuf[i] = val[i];
        }
        return floatBuf;
    }

    public void setData(float[] v) {
        m_data = toFloatArray(v);
    }

    @Override
    protected void writeToBuffer(ByteBuffer buf) {
        float[] d = new float[m_data.length];
        for(int i = 0; i < d.length; i++) {
            d[i] = m_data[i];
        }
        buf.asFloatBuffer().put(d);
    }

    @Override
    protected int dataSize() {
        return 4;
    }
}