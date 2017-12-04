package com.nielsmasdorp.speculum.glkit.data;

import java.nio.ByteBuffer;

/**
 * Created by vfierce on 11/26/17.
 */

public class BufferableBuffer<T extends Bufferable> extends Buffer<T> {
    public BufferableBuffer(int target) {
        super(target);
    }
    public BufferableBuffer(int target, T[] data) {
        super (target, data);
    }

    @Override
    protected void writeToBuffer(ByteBuffer buf) {
        for(Bufferable b : m_data) {
            b.writeToBuffer(buf);
        }
    }

    @Override
    protected int dataSize() {
        return m_data[0].bufferedSize();
    }
}
