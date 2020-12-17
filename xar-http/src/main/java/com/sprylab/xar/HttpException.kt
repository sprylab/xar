package com.sprylab.xar;

import java.io.IOException;

import okhttp3.Response;

public class HttpException extends IOException {

    private final Response mResponse;

    public HttpException(final String message, final Response response) {
        super(message);
        mResponse = response;
    }

    public Response getResponse() {
        return mResponse;
    }
}
