package com.nielsmasdorp.speculum.glkit;

import android.opengl.GLES20;

/**
 * Created by vfierce on 12/3/17.
 */

public class GLError extends RuntimeException {
    public int errno;

    GLError(int errno) {
        super(GLES20.glGetString(errno));
        this.errno = errno;
    }

    GLError(String message) {
        super(message);
        this.errno = GLES20.glGetError();
    }

}
