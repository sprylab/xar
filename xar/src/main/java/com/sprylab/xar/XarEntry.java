package com.sprylab.xar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import com.sprylab.xar.XarSource.OnEntryExtractedListener;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Data;
import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.toc.model.SimpleChecksum;
import com.sprylab.xar.toc.model.Type;
import com.sprylab.xar.utils.HashUtils;
import com.sprylab.xar.utils.StringUtils;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.InflaterSource;
import okio.Okio;
import okio.Source;

/**
 * Represents an entry in a {@link XarSource}.
 * <p>
 * An entry may correspond to a directory or a file when extracted (see {@link #isDirectory()}).
 */
public class XarEntry {

    private String id;

    private String name;

    private boolean isDirectory;

    private List<XarEntry> children;

    private String mode;

    private String uid;

    private String user;

    private String gid;

    private String group;

    private Date time;

    private ChecksumAlgorithm checksumAlgorithm;

    private String checksum;

    private long size;

    private long offset;

    private long length;

    private Encoding encoding;

    private XarSource xarSource;

    /**
     * Creates a new entry linked to the given {@code xarSource}.
     *
     * @param xarSource  the {@link XarSource} this entry is linked to
     * @param file       the corresponding file model
     * @param parentPath the path of the parent directory, may be {@code null}
     * @return the newly created entry
     * @throws XarException when there is an error while reading
     */
    public static XarEntry createFromXarSource(final XarSource xarSource, final com.sprylab.xar.toc.model.File file,
                                               final String parentPath) throws XarException {
        final XarEntry xarEntry = new XarEntry();
        xarEntry.id = file.getId();

        String name = file.getName();
        if (StringUtils.isNotEmpty(parentPath)) {
            name = parentPath + "/" + name;
        }
        xarEntry.name = name;
        xarEntry.isDirectory = file.getType() == Type.DIRECTORY;
        xarEntry.mode = file.getMode();
        xarEntry.uid = file.getUid();
        xarEntry.user = file.getUser();
        xarEntry.gid = file.getGid();
        xarEntry.group = file.getGroup();
        xarEntry.time = file.getMtime();
        xarEntry.xarSource = xarSource;

        final Data data = file.getData();
        if (data != null) {
            SimpleChecksum extractedChecksum = null;
            if (data.getExtractedChecksum() != null) {
                extractedChecksum = data.getExtractedChecksum();
            } else if (data.getUnarchivedChecksum() != null) {
                extractedChecksum = data.getUnarchivedChecksum();
            }

            if (extractedChecksum != null) {
                xarEntry.checksumAlgorithm = extractedChecksum.getStyle();
                xarEntry.checksum = extractedChecksum.getValue();
            } else {
                xarEntry.checksumAlgorithm = ChecksumAlgorithm.NONE;
                xarEntry.checksum = null;
            }
            xarEntry.size = data.getSize();
            final XarHeader header = xarSource.getHeader();
            xarEntry.offset = header.getSize().longValue() + header.getTocLengthCompressed().longValue() + data.getOffset();
            xarEntry.length = data.getLength();
            xarEntry.encoding = data.getEncoding();
        }

        return xarEntry;
    }

    private XarEntry() {
        // protected constructor
    }

    /**
     * @return the ID of this entry
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name of this entry - this corresponds to the path of this entry inside the {@link XarSource}
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@code true} if entry corresponds to a directory, {@code false} if it corresponds to a file
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * @return the UNIX permission mode for this entry
     */
    public String getMode() {
        return mode;
    }

    /**
     * @return the UNIX user ID for this entry
     */
    public String getUid() {
        return uid;
    }

    /**
     * @return the UNIX user name for this entry
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the UNIX group ID for this entry
     */
    public String getGid() {
        return gid;
    }

    /**
     * @return the UNIX group name for this entry
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the last modification time of this entry
     */
    public Date getTime() {
        return time;
    }

    /**
     * @return the uncompressed checksum of this entry
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return the {@link ChecksumAlgorithm} used for this entry
     */
    public ChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    /**
     * @return the uncompressed size of entry
     */
    public long getSize() {
        return size;
    }

    /**
     * @return children of this xar entry, or {@code null} if this entry is a file or has no children
     */
    public List<XarEntry> getChildren() {
        return children;
    }

    /**
     * Adds a child to the list set of children.
     *
     * @param childEntry the child to add
     */
    public void addChild(final XarEntry childEntry) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(childEntry);
    }

    /**
     * Gets access to the underlying byte data of this entry.
     * <p>
     * This allows direct streaming from the archive file without extracting and writing the corresponding file to disk beforehand.
     * If the data is encoded (see {@link Encoding}), then it will be decompressed while reading from the returned {@link Source}.
     * <p>
     * When trying to call this method on an directory entry (i.e. {@link #isDirectory()} returns {@code true}),
     * an {@link IllegalStateException} is thrown.
     *
     * @return the {@link Source} to read from
     * @throws IOException when an I/O error occurred while reading
     */
    public Source getSource() throws IOException {
        if (isDirectory) {
            throw new IllegalStateException("Cannot retrieve source for entries of type directory.");
        }

        if (encoding == null) {
            // file is empty
            return new Buffer();
        }

        switch (encoding) {
            case NONE:
                return xarSource.getRange(offset, length);
            case GZIP:
                return new InflaterSource(xarSource.getRange(offset, length), new Inflater());
            case BZIP2:
                // fall through
            default:
                throw new UnsupportedEncodingException("Encoding not supported: " + encoding.name());
        }
    }

    /**
     * Convenience method to get access to the underlying byte data of this entry as an {@link InputStream}.
     *
     * @return an (uncompressed) {@link InputStream} to read from
     * @throws IOException when an I/O error occurred while reading
     * @see #getSource()
     */
    public InputStream getInputStream() throws IOException {
        return Okio.buffer(getSource()).inputStream();
    }

    /**
     * Convenience method to get access to the underlying byte data of this entry as a byte array.
     *
     * @return a (uncompressed) byte array
     * @throws IOException when an I/O error occurred while reading
     * @see #getSource()
     */
    public byte[] getBytes() throws IOException {
        return Okio.buffer(getSource()).readByteArray();
    }

    /**
     * Convenience method for extracting the corresponding file for this entry bypassing integrity check.
     *
     * @param fileOrDirectory destination directory for extracted files or file name for extracted file
     * @throws IOException when an I/O error occurred while extracting
     * @see #extract(File, boolean, OnEntryExtractedListener)
     */
    public void extract(final File fileOrDirectory) throws IOException {
        extract(fileOrDirectory, false);
    }

    /**
     * Convenience method for extracting the corresponding file for this entry with optional integrity check.
     *
     * @param fileOrDirectory destination directory for extracted files or file name for extracted file
     * @param verifyIntegrity if {@code true}, the integrity of the extracted file will be verified after extraction
     * @throws IOException when an I/O error occurred while extracting
     * @see #extract(File, boolean, OnEntryExtractedListener)
     */
    public void extract(final File fileOrDirectory, final boolean verifyIntegrity) throws IOException {
        extract(fileOrDirectory, verifyIntegrity, null);
    }

    /**
     * Extracts the underlying byte data of an entry and writes it to a file.
     *
     * @param fileOrDirectory destination directory for extracted files or file name for extracted file
     * @param verifyIntegrity if {@code true}, the integrity of the extracted file will be verified after extraction
     * @param listener        the listener that gets notified after the entry was extracted (successfully or not)
     * @throws IOException when an I/O error occurred while extracting
     */
    public void extract(final File fileOrDirectory, final boolean verifyIntegrity, final OnEntryExtractedListener listener)
        throws IOException {
        if (isDirectory) {
            // get all files inside me
            final List<XarEntry> entries = xarSource.getToc().getEntries();
            final String directoryPath = name.concat("/");

            final List<XarEntry> files = new ArrayList<>();

            for (final XarEntry entry : entries) {
                if (entry.getName().length() > directoryPath.length() && entry.getName().substring(0, directoryPath.length()).equals(directoryPath)) {
                    files.add(entry);
                }
            }

            // extract them
            for (final XarEntry file : files) {
                file.extract(fileOrDirectory, verifyIntegrity, listener);
            }

        } else {
            final File targetFile;
            if (fileOrDirectory.isFile()) {
                targetFile = fileOrDirectory;
            } else {
                targetFile = new File(fileOrDirectory, name);
            }
            targetFile.getParentFile().mkdirs();

            try (final Source source = getSource()) {
                try (final BufferedSink sink = Okio.buffer(Okio.sink(targetFile))) {
                    sink.writeAll(source);
                }
            } finally {
                if (verifyIntegrity) {
                    verifyExtractedFile(targetFile);
                }
                if (listener != null) {
                    listener.onEntryExtracted(this);
                }
            }
        }
    }

    private void verifyExtractedFile(final File targetFile) throws IOException {
        if (checksumAlgorithm == null && size == 0L || checksumAlgorithm == ChecksumAlgorithm.NONE) {
            // empty files might have no checksum set
            return;
        }

        try (final BufferedSource source = Okio.buffer(Okio.source(targetFile))) {
            source.require(targetFile.length());
            final String hash = HashUtils.hashHex(source, checksumAlgorithm);
            if (!checksum.equals(hash)) {
                throw new IOException("Hash of extracted file does match the stored checksum.");
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
