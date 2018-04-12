package com.sprylab.xar;

import java.io.IOException;

/**
 * Exception that gets thrown if there is an error with an {@link XarSource}.
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
