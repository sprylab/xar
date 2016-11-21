package com.sprylab.xar;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.InflaterSource;
import okio.Okio;
import okio.Source;

/**
 * User: pschiffer
 * Date: 21.11.2016
 * Time: 22:21
 */
public class HttpXarSource extends XarSource {

    private final String url;
    private final OkHttpClient okHttpClient;

    private XarHeader header;
    private XarToc toc;

    public HttpXarSource(final String url) {
        this.url = url;
        this.okHttpClient = new OkHttpClient();
    }

    public HttpXarSource(final String url, final OkHttpClient okHttpClient) {
        this.url = url;
        this.okHttpClient = okHttpClient;
    }

    @Override
    public XarHeader getHeader() throws XarException {
        ensureHeader();

        return header;
    }

    @Override
    public XarToc getToc() throws XarException {
        ensureHeader();
        ensureToc();
        return toc;
    }

    @Override
    public BufferedSource getRange(final long offset, final long length) throws IOException {
        return createRangeRequest(offset, length);
    }

    @Override
    public Source getToCSource() throws IOException {
        final long headerSize = header.getSize().longValue();
        final long compressedTocSize = header.getTocLengthCompressed().longValue();
        return getRange(headerSize, compressedTocSize);
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public List<XarEntry> getEntries() throws XarException {
        return getToc().getEntries();
    }

    @Override
    public XarEntry getEntry(final String entryName) throws XarException {
        return getToc().getEntry(entryName);
    }

    @Override
    public boolean hasEntry(final String entryName) throws XarException {
        return getToc().hasEntry(entryName);
    }

    private void ensureToc() throws XarException {
        if (toc == null) {
            toc = createToc();
        }
    }

    private XarToc createToc() throws XarException {
        try (final BufferedSource tocSource = Okio.buffer(new InflaterSource(getToCSource(), new Inflater()))) {
            return new XarToc(this, tocSource);
        } catch (final IOException e) {
            throw new XarException("Error creating toc", e);
        }
    }

    private void ensureHeader() throws XarException {
        if (header == null) {
            header = createHeader();
        }
    }

    private XarHeader createHeader() throws XarException {
        try (final BufferedSource headerSource = getRange(0, 28)) {
            return new XarHeader(headerSource);
        } catch (final IOException e) {
            throw new XarException("Error creating header", e);
        }
    }

    private BufferedSource createRangeRequest(final long offset, final long length) throws XarException {
        try {
            final Request.Builder requestBuilder = new Request.Builder()
                .get()
                .header("Range", String.format("bytes=%d-%d", offset, offset + length - 1))
                .url(this.url);
            final Request request = requestBuilder.build();
            final Call call = okHttpClient.newCall(request);

            final Response response = call.execute();
            if (response.isSuccessful()) {
                return Okio.buffer(response.body().source());
            } else {
                throw new IOException(format("Error doing request: %d %s", response.code(), response.message()));
            }
        } catch (final IOException e) {
            throw new XarException("Error reading contents", e);
        }
    }
}
