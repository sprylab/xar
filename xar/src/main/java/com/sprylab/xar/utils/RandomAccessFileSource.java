package com.sprylab.xar.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import okio.Buffer;
import okio.Source;
import okio.Timeout;

/**
 * Supplies a stream of bytes from an underlying {@link RandomAccessFile}.
 */
public class RandomAccessFileSource implements Source {

    private final RandomAccessFile randomAccessFile;

    private final Timeout timeout;

    private long remainingLength;

    /**
     * Creates a new {@link RandomAccessFileSource}, which reads the whole {@code file}.
     *
     * @param file the file to access
     * @throws IOException when an I/O error occurred while reading or opening the file
     */
    public RandomAccessFileSource(final File file) throws IOException {
        this(file, 0L, file.length());
    }

    /**
     * Creates a new {@link RandomAccessFileSource}, which reads a {@code file} starting from {@code offset} until {@code length} bytes are read.
     *
     * @param file the file to access
     * @param offset the offset in bytes to start reading from
     * @param length the number of bytes to read
     * @throws IOException when an I/O error occurred while reading or opening the file
     */
    public RandomAccessFileSource(final File file, final long offset, final long length) throws IOException {
        this(file, offset, length, new Timeout());
    }

    /**
     * Creates a new {@link RandomAccessFileSource}, which reads a {@code file} starting from {@code offset} until {@code length} bytes are read.
     *
     * @param file the file to access
     * @param offset the offset in bytes to start reading from
     * @param length the number of bytes to read
     * @param timeout the timeout to use
     * @throws IOException when an I/O error occurred while reading or opening the file
     */
    public RandomAccessFileSource(final File file, final long offset, final long length, final Timeout timeout) throws IOException {
        this.randomAccessFile = new RandomAccessFile(file, "r");
        this.randomAccessFile.seek(offset);
        this.remainingLength = length;
        this.timeout = timeout;
    }

    @Override
    public long read(final Buffer sink, final long byteCount) throws IOException {
        if (byteCount < 0L) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (byteCount == 0L) {
            return 0L;
        }

        try {
            timeout.throwIfReached();
            // TODO obviously we currently only support files with size <= 2GB...
            int maxToCopy = (int) Math.min(byteCount, remainingLength);
            byte[] bytes = new byte[maxToCopy];
            int bytesRead = randomAccessFile.read(bytes, 0, maxToCopy);
            if (bytesRead == -1) {
                return -1;
            }
            sink.write(bytes);
            remainingLength -= bytesRead;
            return bytesRead;
        } catch (AssertionError e) {
            throw e;
        }
    }

    @Override
    public Timeout timeout() {
        return timeout;
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
