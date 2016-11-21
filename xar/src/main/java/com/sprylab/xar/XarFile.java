package com.sprylab.xar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.Inflater;

import com.sprylab.xar.utils.FileAccessUtils;

import okio.InflaterSource;
import okio.Okio;
import okio.Source;

/**
 * Represents an eXtensible ARchiver file.
 */
public class XarFile extends XarSource {

    private final File file;

    private XarHeader header;

    private XarToc toc;

    /**
     * Creates a new {@link XarFile} from {@code file}.
     *
     * @param file the archive file
     * @throws XarException when the file to be opened is not a valid xar archive, i.e. the header is damaged or similiar
     */
    public XarFile(final File file) throws XarException {
        this.file = file;

        checkXarFileHeader();

        createToc();
    }

    @Override
    public Source getToCSource() throws IOException {
        return new InflaterSource(getRange(header.getSize().longValue(), header.getTocLengthCompressed().longValue()), new Inflater());
    }

    @Override
    public List<XarEntry> getEntries() {
        return toc.getEntries();
    }

    @Override
    public XarEntry getEntry(final String entryName) {
        return toc.getEntry(entryName);
    }

    @Override
    public boolean hasEntry(final String entryName) {
        return toc.hasEntry(entryName);
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public XarHeader getHeader() {
        return header;
    }

    @Override
    public XarToc getToc() {
        return toc;
    }

    @Override
    public Source getRange(final long offset, final long length) throws IOException {
        return FileAccessUtils.createLimitedBufferedSource(getFile(), offset, length);
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    /**
     * Gets access to the underlying byte data of this file's table of content as an {@link InputStream}.
     *
     * @return an (uncompressed) {@link InputStream} to read the table of contents from
     * @throws IOException when an I/O error occurred while reading
     */
    public InputStream getToCStream() throws IOException {
        return Okio.buffer(getToCSource()).inputStream();
    }

    /**
     * @return the underlying {@link File}
     */
    public File getFile() {
        return file;
    }

    private void checkXarFileHeader() throws XarException {
        this.header = XarHeader.createHeader(this.file);
    }

    private void createToc() throws XarException {
        this.toc = XarToc.createToc(this);
    }
}
