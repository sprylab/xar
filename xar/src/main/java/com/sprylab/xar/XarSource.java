package com.sprylab.xar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sprylab.xar.toc.model.Encoding;

import okio.BufferedSource;
import okio.InflaterSource;
import okio.Okio;
import okio.Source;

public abstract class XarSource {

    private static final Logger LOG = LoggerFactory.getLogger(XarSource.class);

    static boolean DEBUG;

    public XarTocParser customParser;

    private XarHeader header;

    private XarToc toc;

    /**
     * @return the {@link XarHeader} of this source
     * @throws XarException when there is an error while reading
     */
    public XarHeader getHeader() throws XarException {
        ensureHeader();
        return header;
    }

    /**
     *
     * @return the {@link XarToc} of this source
     * @throws XarException when there is an error while reading
     */
    public XarToc getToc() throws XarException {
        ensureHeader();
        ensureToc();
        return toc;
    }

    /**
     * Gets access to the underlying byte data of this file's table of content.
     * <p>
     * If the data is encoded (see {@link Encoding}), then it will be decompressed while reading from the returned {@link Source}.
     *
     * @return the {@link Source} to read the table of contents from
     * @throws IOException when an I/O error occurred while reading
     */
    public BufferedSource getToCSource() throws IOException {
        final long headerSize = getHeader().getSize().longValue();
        final long compressedTocSize = getHeader().getTocLengthCompressed().longValue();
        return Okio.buffer(new InflaterSource(getRange(headerSize, compressedTocSize), new Inflater()));
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
     * Creates a limited {@link BufferedSource} accessing the raw binary data from this file.
     * Use {@link #getEntry(String)} or {@link #extractAll(File, boolean, OnEntryExtractedListener)} for structured access.
     *
     * @param offset the offset to start
     * @param length the number of bytes to read counting from offset
     * @return a {@link BufferedSource} for accessing {@code file} constrained to {@code offset} and {@code length}
     * @throws IOException when an I/O error occurred while reading or opening the file
     */
    public abstract BufferedSource getRange(long offset, long length) throws IOException;

    /**
     * Gets the file size in bytes.
     *
     * @return the size in bytes
     *
     * @throws XarException when there is an error while reading
     */
    public abstract long getSize() throws XarException;

    /**
     * Gets all {@link XarEntry}s contained in this file.
     *
     * @return a list of all {@link XarEntry}s
     * @throws XarException when there is an error while reading
     */
    public List<XarEntry> getEntries() throws XarException {
        return getToc().getEntries();
    }

    /**
     * Retrieves exactly the entry denoted by {@code entryName} or {@code null} if such an entry does not exist.
     *
     * @param entryName the name of the entry, e.g. {@code my-file.txt} or {@code directory/sub-directory/image.png}
     * @return the corresponding {@link XarEntry} or {@code null} if it does not exist
     * @throws XarException when there is an error while reading
     */
    public XarEntry getEntry(final String entryName) throws XarException {
        return getToc().getEntry(entryName);
    }

    /**
     * Checks if an entry denoted by {@code entryName} exists in this file.
     *
     * @param entryName the name of the entry, e.g. {@code my-file.txt} or {@code directory/sub-directory/image.png}
     * @return {@code true} if it does exist, {@code false} otherwise
     * @throws XarException when there is an error while reading
     */
    public boolean hasEntry(final String entryName) throws XarException {
        return getToc().hasEntry(entryName);
    };

    /**
     * Convenience method for extracting all files bypassing integrity check.
     *
     * @param directory destination directory for all extracted files
     * @throws IOException when an I/O error occurred while extracting
     * @see #extractAll(File, boolean, OnEntryExtractedListener)
     */
    public void extractAll(final File directory) throws IOException {
        extractAll(directory, false);
    }

    /**
     * Convenience method for extracting all files with optional integrity check.
     *
     * @param directory       destination directory for all extracted files
     * @param verifyIntegrity if {@code true}, the integrity of the extracted files will be verified after extraction
     * @throws IOException when an I/O error occurred while extracting
     * @see #extractAll(File, boolean, OnEntryExtractedListener)
     */
    public void extractAll(final File directory, final boolean verifyIntegrity) throws IOException {
        extractAll(directory, verifyIntegrity, null);
    }

    /**
     * Extracts all files of to the directory denoted by {@code directory}.
     *
     * @param directory       destination directory for extracted files
     * @param verifyIntegrity if {@code true}, the integrity of the extracted files will be verified after extraction
     * @param listener        the listener that gets notified after an entry was extracted (successfully or not)
     * @throws IOException when an I/O error occurred while extracting
     */
    public void extractAll(final File directory, final boolean verifyIntegrity, final OnEntryExtractedListener listener) throws IOException {
        final List<XarEntry> entries = getToc().getEntries();
        for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
            final XarEntry entry = entries.get(i);
            if (!entry.isDirectory()) {
                if (DEBUG) {
                    LOG.debug("Extracting entry '{}'.", entry);
                }
                entry.extract(directory, verifyIntegrity, listener);
            }
        }
    }

    /**
     * Creates the {@link XarToc} if necessary.
     *
     * @throws XarException when there is an error while reading
     */
    private void ensureToc() throws XarException {
        if (toc == null) {
            toc = createToc();
        }
    }

    /**
     * Creates the {@link XarHeader} if necessary.
     *
     * @throws XarException when there is an error while reading
     */
    private void ensureHeader() throws XarException {
        if (header == null) {
            header = createHeader();
        }
    }

    /**
     * Creates the {@link XarToc}.
     *
     * @return the corresponding {@link XarToc}
     * @throws XarException when there is an error while reading
     */
    private XarToc createToc() throws XarException {
        return new XarToc(this, customParser);
    }

    /**
     * Creates the {@link XarHeader}.
     *
     * @return the corresponding {@link XarHeader}
     * @throws XarException when there is an error while reading
     */
    private XarHeader createHeader() throws XarException {
        return new XarHeader(this);
    }

    /**
     * Listens for {@link XarEntry} extraction events.
     */
    public interface OnEntryExtractedListener {

        /**
         * Gets called after a {@link XarEntry} was extracted.
         *
         * @param entry the extracted {@link XarEntry}
         */
        void onEntryExtracted(final XarEntry entry);
    }
}
