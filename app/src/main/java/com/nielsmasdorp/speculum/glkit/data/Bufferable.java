package com.nielsmasdorp.speculum.glkit.data;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by vfierce on 11/26/17.
 */

public interface Bufferable {
    void writeToBuffer(ByteBuffer buf);
    int bufferedSize();
};