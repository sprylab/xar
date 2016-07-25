package com.sprylab.xar.utils;

import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;

import okio.Buffer;
import okio.BufferedSource;
import okio.InflaterSource;
import okio.Okio;
import okio.Source;

/**
 * Utility class for randomly accessing files.
 */
public final class FileAccessUtils {

    private FileAccessUtils() {
    }

    /**
     * Creates a limited {@link InflaterSource} from a file.
     *
     * @param file   the file to read
     * @param offset the offset to start
     * @param length the number of bytes to read counting from offset
     * @return a {@link InflaterSource} for accessing {@code file} constrained to {@code offset} and {@code length}
     * @throws IOException
     */
    public static Source createLimitedInflaterSource(final File file, final long offset, final long length) throws IOException {
        return new InflaterSource(createLimitedBufferedSource(file, offset, length), new Inflater());
    }

    /**
     * Creates a limited {@link BufferedSource} from a file.
     *
     * @param file   the file to read
     * @param offset the offset to start
     * @param length the number of bytes to read counting from offset
     * @return a {@link BufferedSource} for accessing {@code file} constrained to {@code offset} and {@code length}
     * @throws IOException
     */
    public static Source createLimitedBufferedSource(final File file, final long offset, final long length) throws IOException {
        final BufferedSource source = Okio.buffer(Okio.source(file));
        source.skip(offset);
        final Buffer buffer = new Buffer();
        source.readFully(buffer, length);
        return buffer;
    }

}
