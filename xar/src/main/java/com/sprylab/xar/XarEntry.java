package com.sprylab.xar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Data;
import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.toc.model.SimpleChecksum;
import com.sprylab.xar.toc.model.Type;
import com.sprylab.xar.utils.FileAccessUtils;

/**
 * Represents an entry in a {@link XarFile}.
 *
 * @author rzimmer, hbakici
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

    private XarFile xarFile;

    public static XarEntry createFromFile(final XarFile xarFile, final com.sprylab.xar.toc.model.File file,
                                          final String parentPath) {
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
        xarEntry.xarFile = xarFile;

        final Data data = file.getData();
        if (data != null) {
            final SimpleChecksum extractedChecksum = data.getExtractedChecksum() != null ? data.getExtractedChecksum() : data.getUnarchivedChecksum();
            xarEntry.checksumAlgorithm = extractedChecksum.getStyle();
            xarEntry.checksum = extractedChecksum.getValue();
            xarEntry.size = data.getSize();
            xarEntry.offset = xarFile.getHeader().getSize().longValue()
                + xarFile.getHeader().getTocLengthCompressed().longValue()
                + data.getOffset();
            xarEntry.length = data.getLength();
            xarEntry.encoding = data.getEncoding();
        }

        return xarEntry;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getMode() {
        return mode;
    }

    public String getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

    public String getGid() {
        return gid;
    }

    public String getGroup() {
        return group;
    }

    /**
     * @return last modification time of entry
     */
    public Date getTime() {
        return time;
    }

    /**
     * @return uncompressed checksum of entry
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return uncompressed size of entry
     */
    public long getSize() {
        return size;
    }

    /**
     * @return children of this xar entry, or null if this entry is a file.
     */
    public List<XarEntry> getChildren() {
        return children;
    }

    /**
     * package local. Adds a child to the set of children.
     *
     * @param childEntry the child to add.
     */
    void addChild(final XarEntry childEntry) {
        if (children == null) {
            children = new ArrayList<XarEntry>();
        }
        children.add(childEntry);
    }

    public InputStream getInputStream() throws IOException {
        if (isDirectory) {
            throw new IllegalStateException("Cannot get InputStream for entries of type directory.");
        }

        if (encoding == null) {
            // file is empty
            return new NullInputStream(0L);
        }

        switch (encoding) {
            case NONE:
                return FileAccessUtils.createLimitedBufferedInputStream(xarFile.getFile(), offset, length);
            case GZIP:
                return FileAccessUtils.createLimitedInflaterInputStream(xarFile.getFile(), offset, length);
            case BZIP2:
                // fall through
            default:
                throw new UnsupportedEncodingException("Encoding not supported: " + encoding.name());
        }
    }

    public byte[] getBytes() throws IOException {
        final InputStream inputStream = getInputStream();
        if (inputStream == null) {
            return new byte[0];
        }
        return IOUtils.toByteArray(inputStream);
    }

    /**
     * Convenience method for extracting the corresponding file for this entry bypassing integrity check.
     *
     * @param fileOrDirectory destination directory for extracted files or file name for extracted file
     * @throws IOException
     */
    public void extract(final File fileOrDirectory) throws IOException {
        extract(fileOrDirectory, false);
    }

    public void extract(final File fileOrDirectory, final boolean check) throws IOException {
        extract(fileOrDirectory, check, null);
    }

    public void extract(final File fileOrDirectory, final boolean check, final OnEntryExtractedListener listener) throws IOException {
        if (isDirectory) {
            // get all files inside me
            final List<XarEntry> entries = xarFile.getEntries();
            final String directoryPath = name.concat("/");

            final List<XarEntry> files = new ArrayList<XarEntry>();

            for (final XarEntry entry : entries) {
                if (entry.getName().length() > directoryPath.length() && entry.getName().substring(0, directoryPath.length()).equals(directoryPath)) {
                    files.add(entry);
                }
            }

            // extract them
            for (final XarEntry file : files) {
                file.extract(fileOrDirectory, check);
            }

        } else {
            final File targetFile;
            if (fileOrDirectory.isFile()) {
                targetFile = fileOrDirectory;
            } else {
                targetFile = new File(fileOrDirectory, name);
            }
            FileUtils.forceMkdir(targetFile.getParentFile());
            final InputStream data = getInputStream();
            try {
                FileUtils.copyInputStreamToFile(data, targetFile);
            } finally {
                IOUtils.closeQuietly(data);

                if (check) {
                    checkExtractedFile(targetFile);
                }
                if (listener != null) {
                    listener.onEntryExtracted(this);
                }
            }
        }
    }

    private void checkExtractedFile(final File targetFile) throws IOException {
        if (checksumAlgorithm == null && size == 0L) {
            // empty files might have no checksum set
            return;
        }
        final FileInputStream targetFileInputStream = FileUtils.openInputStream(targetFile);
        String hash = null;
        switch (checksumAlgorithm) {
            case SHA1:
                hash = DigestUtils.sha1Hex(targetFileInputStream);
                break;
            case MD5:
                hash = DigestUtils.md5Hex(targetFileInputStream);
                break;
            case NONE:
                return;
        }

        if (!checksum.equals(hash)) {
            throw new IOException("Hash of extracted file does match the stored checksum.");
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public interface OnEntryExtractedListener {
        void onEntryExtracted(final XarEntry entry);
    }
}
