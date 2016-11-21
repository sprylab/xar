package com.sprylab.xar;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sprylab.xar.toc.model.Encoding;

import okio.Source;

public abstract class XarSource {

    private static final Logger LOG = LoggerFactory.getLogger(XarFile.class);

    static boolean DEBUG;

    /**
     * @return the {@link XarHeader} of this file
     */
    public abstract XarHeader getHeader() throws XarException;

    public abstract XarToc getToc() throws XarException;

    public abstract Source getRange(long offset, long length) throws IOException;

    /**
     * Gets access to the underlying byte data of this file's table of content.
     * <p>
     * If the data is encoded (see {@link Encoding}), then it will be decompressed while reading from the returned {@link Source}.
     *
     * @return the {@link Source} to read the table of contents from
     * @throws IOException when an I/O error occurred while reading
     */
    public abstract Source getToCSource() throws IOException;

    /**
     * @return the size of this files in bytes.
     * @see File#length()
     */
    public abstract long getSize();

    /**
     * Gets all {@link XarEntry}s contained in this file.
     *
     * @return a list of all {@link XarEntry}s
     */
    public abstract List<XarEntry> getEntries() throws XarException;

    /**
     * Retrieves exactly the entry denoted by {@code entryName} or {@code null} if such an entry does not exist.
     *
     * @param entryName the name of the entry, e.g. {@code my-file.txt} or {@code directory/sub-directory/image.png}
     * @return the corresponding {@link XarEntry} or {@code null} if it does not exist
     */
    public abstract XarEntry getEntry(String entryName) throws XarException;

    /**
     * Checks if an entry denoted by {@code entryName} exists in this file.
     *
     * @param entryName the name of the entry, e.g. {@code my-file.txt} or {@code directory/sub-directory/image.png}
     * @return {@code true} if it does exist, {@code false} otherwise
     */
    public abstract boolean hasEntry(String entryName) throws XarException;

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
