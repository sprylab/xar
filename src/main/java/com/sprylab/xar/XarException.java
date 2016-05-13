package com.sprylab.xar;

import java.io.IOException;

/**
 * User: Philip Date: 28.10.2014 Time: 16:20
 */
public class XarException extends IOException {
    public XarException() {
    }

    public XarException(final String message) {
        super(message);
    }

    public XarException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public XarException(final Throwable cause) {
        super(cause);
    }
}
