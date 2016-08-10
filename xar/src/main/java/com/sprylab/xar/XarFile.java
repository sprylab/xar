package com.sprylab.xar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.joou.UInteger;
import org.joou.ULong;
import org.joou.UShort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sprylab.xar.XarEntry.OnEntryExtractedListener;
import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.utils.FileAccessUtils;
import com.sprylab.xar.utils.FilePath;
import com.sprylab.xar.utils.StringUtils;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * Represents an eXtensible ARchiver file.
 */
public class XarFile {

    private static final Logger LOG = LoggerFactory.getLogger(XarFile.class);

    static boolean DEBUG;

    private final File file;

    private final List<XarEntry> entries = new ArrayList<>();

    private final Map<String, XarEntry> nameToEntryMap = new HashMap<>();

    private Header header;

    /**
     * Creates a new {@link XarFile} from {@code file}.
     *
     * @param file the archive file
     * @throws XarException when the file to be opened is not a valid xar archive, i.e. the header is damaged or similiar
     */
    public XarFile(final File file) throws XarException {
        this.file = file;

        checkXarFileHeader();

        createEntries();
    }

    private void checkXarFileHeader() throws XarException {
        this.header = new Header(this.file);
    }

    private void createEntries() throws XarException {
        // Unfortunately simple-xml throws Exceptions
        //noinspection OverlyBroadCatchBlock
        try (InputStream inputStream = getToCStream()) {
            final ToC toC = ToCFactory.fromInputStream(inputStream);

            final Stack<FilePath> fileStack = new Stack<>();
            fileStack.addAll(FilePath.fromFileList(toC.getFiles()));

            while (!fileStack.isEmpty()) {
                final FilePath currentFile = fileStack.pop();
                final com.sprylab.xar.toc.model.File fileEntry = currentFile.getFile();
                final XarEntry xarEntry = XarEntry.createFromFile(this, fileEntry, currentFile.getParentPath());

                if (xarEntry.isDirectory()) {
                    final List<com.sprylab.xar.toc.model.File> children = fileEntry.getChildren();
                    if (children != null && !children.isEmpty()) {
                        fileStack.addAll(FilePath.fromFileList(children, xarEntry.getName()));
                    }
                }
                addEntry(xarEntry);
                addToParentEntry(xarEntry, currentFile.getParentPath());
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new XarException("Error creating entries for " + file.toString(), e);
        }
    }

    /**
     * Gets access to the underlying byte data of this file's table of content.
     * <p>
     * If the data is encoded (see {@link Encoding}), then it will be decompressed while reading from the returned {@link Source}.
     *
     * @return the {@link Source} to read the table of contents from
     * @throws IOException when an I/O error occurred while reading
     */
    public Source getToCSource() throws IOException {
        return FileAccessUtils.createLimitedInflaterSource(file, header.getSize().longValue(),
            header.getTocLengthCompressed().longValue());
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

    private void addEntry(final XarEntry xarEntry) {
        entries.add(xarEntry);
        nameToEntryMap.put(xarEntry.getName(), xarEntry);
    }

    private void addToParentEntry(final XarEntry xarEntry, final String parentPath) {
        if (StringUtils.isEmpty(parentPath)) {
            // the entry itself is in the root entry
            return;
        }
        final XarEntry parentEntry = getEntry(parentPath);
        parentEntry.addChild(xarEntry);
    }

    /**
     * Gets all {@link XarEntry}s contained in this file.
     *
     * @return a list of all {@link XarEntry}s
     */
    public List<XarEntry> getEntries() {
        return entries;
    }

    /**
     * Retrieves exactly the entry denoted by {@code entryName} or {@code null} if such an entry does not exist.
     *
     * @param entryName the name of the entry, e.g. {@code my-file.txt} or {@code directory/sub-directory/image.png}
     * @return the corresponding {@link XarEntry} or {@code null} if it does not exist
     */
    public XarEntry getEntry(final String entryName) {
        return nameToEntryMap.get(entryName);
    }

    /**
     * Checks if an entry denoted by {@code entryName} exists in this file.
     *
     * @param entryName the name of the entry, e.g. {@code my-file.txt} or {@code directory/sub-directory/image.png}
     * @return {@code true} if it does exist, {@code false} otherwise
     */
    public boolean hasEntry(final String entryName) {
        return nameToEntryMap.containsKey(entryName);
    }

    /**
     * @return the size of this files in bytes.
     * @see File#length()
     */
    public long getSize() {
        return file.length();
    }

    /**
     * @return the {@link Header} of this file
     */
    public Header getHeader() {
        return header;
    }

    /**
     * @return the underlying {@link File}
     */
    public File getFile() {
        return file;
    }

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
    public void extractAll(final File directory, final boolean verifyIntegrity, final OnEntryExtractedListener listener) throws
        IOException {
        for (final XarEntry entry : entries) {
            if (!entry.isDirectory()) {
                if (DEBUG) {
                    LOG.debug("Extracting entry '{}'.", entry);
                }
                entry.extract(directory, verifyIntegrity, listener);
            }
        }
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    /**
     * Describes the file header of an eXtensible ARchiver file
     * (see <a href="https://github.com/mackyle/xar/wiki/xarformat#The_Header">specification</a>).
     */
    public static class Header {

        /**
         * Magic number for xar files: 'xar!'
         */
        private static final UInteger MAGIC = UInteger.valueOf(0x78617221);

        private static final short HEADER_SIZE = 28;

        private static final short VERSION = 1;

        // 32bit
        private final UInteger magic;

        // 16bit
        private final UShort size;

        // 16bit
        private final UShort version;

        // 64bit
        private final ULong tocLengthCompressed;

        // 64bit
        private final ULong tocLengthUncompressed;

        // 32bit
        private final UInteger cksumAlg;

        /**
         * Creates a header as byte array used for writing xar files.
         *
         * @param tocLengthCompressed   the length of the table of contents (compressed)
         * @param tocLengthUncompressed the length of the table of contents (uncompressed)
         * @param checksumAlgorithm     the {@link ChecksumAlgorithm} to use
         * @return the header as a byte array
         */
        public static byte[] createHeader(final long tocLengthCompressed, final long tocLengthUncompressed,
                                          final ChecksumAlgorithm checksumAlgorithm) {
            final ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
            bb.putInt(0, MAGIC.intValue());
            bb.putShort(4, HEADER_SIZE);
            bb.putShort(6, VERSION);
            bb.putLong(8, tocLengthCompressed);
            bb.putLong(16, tocLengthUncompressed);
            bb.putInt(24, checksumAlgorithm.ordinal());
            return bb.array();
        }

        public Header(final File file) throws XarException {
            try (BufferedSource source = Okio.buffer(Okio.source(file))) {

                this.magic = UInteger.valueOf(source.readInt());
                checkMagic();

                this.size = UShort.valueOf(source.readShort());

                this.version = UShort.valueOf(source.readShort());

                this.tocLengthCompressed = ULong.valueOf(source.readLong());
                this.tocLengthUncompressed = ULong.valueOf(source.readLong());

                this.cksumAlg = UInteger.valueOf(source.readInt());
            } catch (final IOException e) {
                throw new XarException("Error opening XarFile", e);
            }
        }

        private void checkMagic() {
            if (!hasValidMagic()) {
                throwNoValidHeaderError();
            }
        }

        private void throwNoValidHeaderError() {
            throw new IllegalArgumentException("No valid xar header found.");
        }

        public boolean hasValidMagic() {
            return this.magic.equals(MAGIC);
        }

        public UInteger getMagic() {
            return magic;
        }

        public UShort getSize() {
            return size;
        }

        public UShort getVersion() {
            return version;
        }

        public ULong getTocLengthCompressed() {
            return tocLengthCompressed;
        }

        public ULong getTocLengthUncompressed() {
            return tocLengthUncompressed;
        }

        public UInteger getCksumAlg() {
            return cksumAlg;
        }
    }
}
