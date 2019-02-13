package com.sprylab.xar;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;

/**
 * Represents an eXtensible ARchiver using a URL.
 */
public class HttpXarSource extends XarSource {

    private final String url;

    private final OkHttpClient okHttpClient;

    /**
     * Create a new {@link XarSource} from a {@code url}. This constructor uses a default, non-shared {@link OkHttpClient}.
     *
     * @param url the URL referencing the archive file
     */
    public HttpXarSource(final String url) {
       this(url, null);
    }

    /**
     * Create a new {@link XarSource} from a {@code url}. It is recommended to create and manage a global, shared {@link OkHttpClient} instance for
     * optimal overall performance. If {@code okHttpClient} is {@code null}, a default, non-shared {@link OkHttpClient} is used.
     *
     * @param url the URL referencing the archive file
     * @param  okHttpClient the {@link OkHttpClient} to use or {@code null} to use the default one
     */
    public HttpXarSource(final String url, final OkHttpClient okHttpClient) {
        this.url = url;
        if (okHttpClient == null) {
            this.okHttpClient = new OkHttpClient();
        } else {
            this.okHttpClient = okHttpClient;
        }
    }

    @Override
    public BufferedSource getRange(final long offset, final long length) throws IOException {
        return executeRangeRequest(offset, length);
    }

    @Override
    public long getSize() throws XarException {
        return executeContentLengthRequest();
    }

    @Override
    public String toString() {
        return String.format("HttpXarSource{url='%s'}", url);
    }

    /**
     * @return the underlying URL
     */
    public String getUrl() {
        return url;
    }

    private BufferedSource executeRangeRequest(final long offset, final long length) throws XarException {
        try {
            final Request request = new Request.Builder()
                .get()
                .header("Range", String.format("bytes=%d-%d", offset, offset + length - 1L))
                .url(this.url)
                .build();
            final Call call = okHttpClient.newCall(request);

            final Response response = call.execute();
            if (response.isSuccessful()) {
                return Okio.buffer(response.body().source());
            } else {
                response.close();
                throw new IOException(String.format("Error executing request: %d %s", response.code(), response.message()));
            }
        } catch (final IOException e) {
            throw new XarException("Error reading contents", e);
        }
    }

    private long executeContentLengthRequest() throws XarException {
        try {
            final Request request = new Request.Builder()
                .head()
                .url(this.url)
                .build();
            final Call call = okHttpClient.newCall(request);

            final Response response = call.execute();
            if (response.isSuccessful()) {
                final String contentLength = response.header("Content-Length");
                return Long.valueOf(contentLength);
            } else {
                throw new IOException(String.format("Error executing request: %d %s", response.code(), response.message()));
            }
        } catch (final IOException | NumberFormatException e) {
            throw new XarException("Error reading content length", e);
        }
    }
}
