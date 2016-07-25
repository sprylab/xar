package com.sprylab.xar.writer;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.Checksum;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Data;
import com.sprylab.xar.toc.model.File;
import com.sprylab.xar.toc.model.SimpleChecksum;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.toc.model.Type;
import com.sprylab.xar.toc.model.Xar;
import com.sprylab.xar.utils.HashUtils;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.DeflaterSink;
import okio.Okio;
import okio.Sink;

public class XarWriter {

    private static final Logger LOG = LoggerFactory.getLogger(XarWriter.class);

    private static final int MAGIC = 0x78617221;

    private static final short HEADER_SIZE = 28;

    private static final short VERSION = 1;

    private static final int CHECKSUM_LENGTH_MD5 = 16;

    private static final int CHECKSUM_LENGTH_SHA1 = 20;

    private ChecksumAlgorithm checksumAlgorithm;

    private final Xar xarRoot = new Xar();

    private final List<File> fileList = new ArrayList<>();

    private final List<XarSource> sources = new ArrayList<>();

    private final Map<XarDirectory, File> dirMap = new HashMap<>();

    private long currentOffset;

    private int id = 0;

    public XarWriter() {
        this(ChecksumAlgorithm.SHA1);
    }

    public XarWriter(final ChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
        final int checkSumLength = checksumAlgorithm == ChecksumAlgorithm.MD5 ? CHECKSUM_LENGTH_MD5
            : (checksumAlgorithm == ChecksumAlgorithm.SHA1 ? CHECKSUM_LENGTH_SHA1 : 0);
        final ToC toc = new ToC();
        toc.setCreationTime(new Date());
        xarRoot.setToc(toc);
        toc.setFiles(fileList);
        final Checksum checksum = new Checksum();
        toc.setChecksum(checksum);
        checksum.setStyle(checksumAlgorithm);
        checksum.setSize(checkSumLength);
        checksum.setOffset(0);
        this.currentOffset = checkSumLength;
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
        final Data data = new Data();
        data.setOffset(currentOffset);
        data.setLength(source.getLength());
        data.setSize(source.getSize());
        currentOffset += source.getLength();

        final ChecksumAlgorithm checksumStyle = source.getChecksumStyle();
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
            fileList.add(file);
        } else {
            final File parentFile = dirMap.get(parent);
            if (parentFile == null) {
                throw new IllegalArgumentException("parent unknown");
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
        ToCFactory.toOutputStream(xarRoot, tocBuffer.outputStream());
        tocBuffer.close();
        final long tocBufferSize = tocBuffer.size();

        final Buffer tocCompressedBuffer = new Buffer();

        try (Sink deflaterSink = new DeflaterSink(tocCompressedBuffer, new Deflater())) {
            deflaterSink.write(tocBuffer, tocBuffer.size());
        }

        final long tocCompressedBufferSize = tocCompressedBuffer.size();

        final ByteString tocCompressedBufferHash = HashUtils.hash(tocCompressedBuffer, checksumAlgorithm);

        buffer.write(createHeader(tocCompressedBufferSize, tocBufferSize));

        buffer.writeAll(tocCompressedBuffer);

        if (tocCompressedBufferHash != null) {
            buffer.write(tocCompressedBufferHash);
        }

        for (final XarSource xs : sources) {
            final BufferedSource source = Okio.buffer(xs.getSource());
            buffer.writeAll(source);
            source.close();
        }

        final Sink sink = Okio.sink(output);
        buffer.readAll(sink);
        sink.close();
    }

    /**
     * Create Header
     * <p>
     * uint32_t magic;
     * uint16_t size;
     * uint16_t version;
     * uint64_t toc_length_compressed;
     * uint64_t toc_length_uncompressed;
     * uint32_t cksum_alg;
     *
     * @param tocLengthCompressed
     * @param tocLengthUnCompressed
     */
    private byte[] createHeader(final long tocLengthCompressed, final long tocLengthUnCompressed) {
        final ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
        bb.putInt(0, MAGIC);
        bb.putShort(4, HEADER_SIZE);
        bb.putShort(6, VERSION);
        bb.putLong(8, tocLengthCompressed);
        bb.putLong(16, tocLengthUnCompressed);
        bb.putInt(24, checksumAlgorithm.ordinal());
        return bb.array();
    }
}
