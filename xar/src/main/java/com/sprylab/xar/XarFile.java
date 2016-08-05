package com.sprylab.xar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.utils.FileAccessUtils;
import com.sprylab.xar.utils.FilePath;
import com.sprylab.xar.utils.StringUtils;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * Represents a eXtensible ARchiver file.
 *
 * @author rzimmer, hbakici
 */
public class XarFile {

    private static final Logger LOG = LoggerFactory.getLogger(XarFile.class);

    static boolean DEBUG = false;

    private final File file;

    private Header header;

    private final List<XarEntry> entries = new ArrayList<>();

    private final Map<String, XarEntry> nameToEntryMap = new HashMap<>();

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

    public Source getToCSource() throws IOException {
        return FileAccessUtils.createLimitedInflaterSource(file, header.getSize().longValue(),
            header.getTocLengthCompressed().longValue());
    }

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
     * Lists all the {@link com.sprylab.xar.XarEntry}s in this file.
     *
     * @return a list of all entries.
     */
    public List<XarEntry> getEntries() {
        return entries;
    }

    public XarEntry getEntry(final String entryName) {
        return nameToEntryMap.get(entryName);
    }

    public boolean hasEntry(final String entryName) {
        return nameToEntryMap.containsKey(entryName);
    }

    public long getSize() {
        return file.length();
    }

    public Header getHeader() {
        return header;
    }

    public File getFile() {
        return file;
    }

    /**
     * Convenience method for extracting all files bypassing integrity check.
     *
     * @param directory destination directory for extracted files
     * @throws IOException
     */
    public void extractAll(final File directory) throws IOException {
        extractAll(directory, false);
    }

    public void extractAll(final File directory, final boolean check) throws IOException {
        extractAll(directory, check, null);
    }

    public void extractAll(final File directory, final boolean check, final XarEntry.OnEntryExtractedListener listener) throws
        IOException {
        for (final XarEntry entry : entries) {
            if (!entry.isDirectory()) {
                if (DEBUG) {
                    LOG.debug("Extract entry: {}", entry);
                }
                entry.extract(directory, check, listener);
            }
        }
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    public static class Header {

        /**
         * Magic number for xar files: 'xar!'
         */
        private static final UInteger MAGIC = UInteger.valueOf(0x78617221);

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
