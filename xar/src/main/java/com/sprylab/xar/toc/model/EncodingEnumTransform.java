package com.sprylab.xar.toc.model;

import java.io.UnsupportedEncodingException;

import org.simpleframework.xml.transform.Transform;

public class EncodingEnumTransform implements Transform<Encoding> {

    public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";

    public static final String MIME_TYPE_GZIP = "application/x-gzip";

    public static final String MIME_TYPE_BZIP2 = "application/x-bzip";

    @Override
    public Encoding read(final String value) throws Exception {
        if (value.equals(MIME_TYPE_OCTET_STREAM)) {
            return Encoding.NONE;
        }
        if (value.equals(MIME_TYPE_GZIP)) {
            return Encoding.GZIP;
        }
        if (value.equals(MIME_TYPE_BZIP2)) {
            return Encoding.BZIP2;
        }
        return Encoding.NONE;
    }

    @Override
    public String write(final Encoding value) throws Exception {
        switch (value) {
            case GZIP:
                return MIME_TYPE_GZIP;
            case BZIP2:
                return MIME_TYPE_BZIP2;
            case NONE:
                return MIME_TYPE_OCTET_STREAM;
            default:
                throw new UnsupportedEncodingException("Encoding not supported: " + value.name());
        }
    }
}
