package com.sprylab.xar.writer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import com.sprylab.xar.XarFile.Header;
import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.Checksum;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Data;
import com.sprylab.xar.toc.model.File;
import com.sprylab.xar.toc.model.SimpleChecksum;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.toc.model.Type;
import com.sprylab.xar.utils.HashUtils;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.DeflaterSink;
import okio.Okio;
import okio.Sink;

public class XarWriter {

    private static final long CHECKSUM_LENGTH_MD5 = 16L;

    private static final long CHECKSUM_LENGTH_SHA1 = 20L;

    private final ChecksumAlgorithm checksumAlgorithm;

    private final ToC toc = new ToC();

    private final List<File> files = new ArrayList<>();

    private final List<XarSource> sources = new ArrayList<>();

    private final Map<XarDirectory, File> dirMap = new HashMap<>();

    private long currentOffset;

    private int id;

    public XarWriter() {
        this(ChecksumAlgorithm.SHA1);
    }

    public XarWriter(final ChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
        final long checksumLength;
        switch (checksumAlgorithm) {
            default:
            case NONE:
                checksumLength = 0L;
                break;
            case SHA1:
                checksumLength = CHECKSUM_LENGTH_SHA1;
                break;
            case MD5:
                checksumLength = CHECKSUM_LENGTH_MD5;
                break;
        }
        toc.setCreationTime(new Date());
        toc.setFiles(files);
        final Checksum checksum = new Checksum();
        toc.setChecksum(checksum);
        checksum.setStyle(checksumAlgorithm);
        checksum.setSize(checksumLength);
        checksum.setOffset(0L);
        this.currentOffset = checksumLength;
    }

    public void addSource(final XarSource source) {
        addSource(source, null);
    }

    public void addSource(final XarSource source, final XarDirectory parent) {
        sources.add(source);
        final File file = new File();
        file.setType(Type.FILE);
        file.setName(source.getName());
        file.setId(String.valueOf(id++));
        final Date lastModifiedDate = new Date(source.getLastModified());
        file.setMtime(lastModifiedDate);
        file.setCtime(lastModifiedDate);
        final Data data = new Data();
        data.setOffset(currentOffset);
        data.setLength(source.getLength());
        data.setSize(source.getSize());
        currentOffset += source.getLength();

        final ChecksumAlgorithm checksumStyle = source.getChecksumAlgorithm();
        if (checksumStyle != null && checksumStyle != ChecksumAlgorithm.NONE) {
            final SimpleChecksum extractedChecksum = new SimpleChecksum();
            extractedChecksum.setStyle(checksumStyle);
            extractedChecksum.setValue(source.getExtractedChecksum() == null ? "0" : source.getExtractedChecksum());
            data.setExtractedChecksum(extractedChecksum);
            data.setUnarchivedChecksum(extractedChecksum);

            final SimpleChecksum archivedChecksum = new SimpleChecksum();
            archivedChecksum.setStyle(checksumStyle);
            archivedChecksum.setValue(source.getArchivedChecksum() == null ? "0" : source.getArchivedChecksum());
            data.setArchivedChecksum(archivedChecksum);
        }

        data.setEncoding(source.getEncoding());
        file.setData(data);
        addFile(file, parent);
    }

    public void addDirectory(final XarDirectory dir, final XarDirectory parent) {
        final File file = new File();
        file.setType(Type.DIRECTORY);
        file.setName(dir.getName());
        file.setId(String.valueOf(id++));
        addFile(file, parent);
        dirMap.put(dir, file);
    }

    private void addFile(final File file, final XarDirectory parent) {
        if (parent == null) {
            files.add(file);
        } else {
            final File parentFile = dirMap.get(parent);
            if (parentFile == null) {
                throw new IllegalArgumentException("Unknown parent.");
            }
            List<File> children = parentFile.getChildren();
            if (children == null) {
                children = new ArrayList<>();
                parentFile.setChildren(children);
            }
            children.add(file);
        }
    }

    public void write(final OutputStream output) throws Exception {
        final Buffer buffer = new Buffer();

        final Buffer tocBuffer = new Buffer();
        ToCFactory.toOutputStream(toc, tocBuffer.outputStream());
        tocBuffer.close();
        final long tocBufferSize = tocBuffer.size();

        final Buffer tocCompressedBuffer = new Buffer();

        try (final Sink deflaterSink = new DeflaterSink(tocCompressedBuffer, new Deflater(Deflater.BEST_COMPRESSION))) {
            deflaterSink.write(tocBuffer, tocBuffer.size());
        }

        final long tocCompressedBufferSize = tocCompressedBuffer.size();

        final ByteString tocCompressedBufferHash = HashUtils.hash(tocCompressedBuffer, checksumAlgorithm);

        buffer.write(Header.createHeader(tocCompressedBufferSize, tocBufferSize, checksumAlgorithm));

        buffer.writeAll(tocCompressedBuffer);

        if (tocCompressedBufferHash != null) {
            buffer.write(tocCompressedBufferHash);
        }

        for (final XarSource xs : sources) {
            try (final BufferedSource source = Okio.buffer(xs.getSource())) {
                buffer.writeAll(source);
            }
        }

        try (final Sink sink = Okio.sink(output)) {
            buffer.readAll(sink);
        }
    }

}
