package com.sprylab.xar.writer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.utils.HashUtils;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.DeflaterSink;
import okio.Okio;
import okio.Source;

public class XarFileSource implements XarSource {

    private Buffer buffer = new Buffer();

    private File file;

    private Encoding encoding;

    private ChecksumAlgorithm checksumStyle = ChecksumAlgorithm.NONE;

    private String extractedChecksum;

    private String archivedChecksum;

    public XarFileSource(final File file) throws IOException {
        this(file, Encoding.NONE);
    }

    public XarFileSource(final File file, final Encoding encoding) throws IOException {
        this(file, encoding, ChecksumAlgorithm.NONE);
    }

    public XarFileSource(final File file, final Encoding encoding, final ChecksumAlgorithm checksumStyle) throws IOException {
        this.file = file;
        this.encoding = encoding;
        this.checksumStyle = checksumStyle;
        try (final BufferedSource fileSource = Okio.buffer(Okio.source(file))) {
	        fileSource.require(file.length());
	        this.extractedChecksum = HashUtils.hashHex(fileSource, checksumStyle);
	        switch (encoding) {
	            case NONE:
	                this.buffer.writeAll(fileSource);
	                this.archivedChecksum = extractedChecksum;
	                break;
	            case GZIP:
	                try (final BufferedSink output = Okio.buffer(new DeflaterSink(this.buffer, new Deflater(Deflater.BEST_COMPRESSION)))) {
	                    output.writeAll(fileSource);
	                }
                    this.archivedChecksum = HashUtils.hashHex(this.buffer, checksumStyle);
	                break;
	            case BZIP2:
	                throw new UnsupportedEncodingException("Encoding not supported: " + encoding.name());
	        }
        }
    }

    @Override
    public long getLength() {
        return buffer.size();
    }

    @Override
    public ChecksumAlgorithm getChecksumAlgorithm() {
        return checksumStyle;
    }

    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    @Override
    public Source getSource() {
        return buffer;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public long getLastModified() {
        return file.lastModified();
    }

    @Override
    public String getExtractedChecksum() {
        return extractedChecksum;
    }

    @Override
    public String getArchivedChecksum() {
        return archivedChecksum;
    }
}
