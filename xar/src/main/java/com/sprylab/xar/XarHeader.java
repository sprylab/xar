package com.sprylab.xar;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.joou.UInteger;
import org.joou.ULong;
import org.joou.UShort;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;

import okio.BufferedSource;

/**
 * Describes the file header of an eXtensible ARchiver file represented by a {@link XarSource}
 * (see <a href="https://github.com/mackyle/xar/wiki/xarformat#The_Header">specification</a>).
 */
public class XarHeader {

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

    public XarHeader(final XarSource xarSource) throws XarException {
        try {
            final BufferedSource source = xarSource.getRange(0, HEADER_SIZE);

            this.magic = UInteger.valueOf(source.readInt());
            checkMagic();

            this.size = UShort.valueOf(source.readShort());

            this.version = UShort.valueOf(source.readShort());

            this.tocLengthCompressed = ULong.valueOf(source.readLong());
            this.tocLengthUncompressed = ULong.valueOf(source.readLong());

            this.cksumAlg = UInteger.valueOf(source.readInt());
        } catch (final IOException e) {
            throw new XarException("Error reading header", e);
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
