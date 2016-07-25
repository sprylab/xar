package com.sprylab.xar.utils;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;

import okio.BufferedSource;
import okio.ByteString;

/**
 * Utility class for easily hashing {@link BufferedSource}s.
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * Hashes the provided {@link BufferedSource} using the provided {@link ChecksumAlgorithm} and returns a {@link ByteString}.
     *
     * @param source    the {@link BufferedSource} to hash
     * @param algorithm the {@link ChecksumAlgorithm} to use
     * @return the calculated hash corresponding to <code>algorithm</code> or null if <code>algorithm == ChecksumAlgorithm.NONE</code>
     */
    public static ByteString hash(final BufferedSource source, final ChecksumAlgorithm algorithm) {
        switch (algorithm) {
            case NONE:
                return null;
            case SHA1:
                return sha1(source);
            case MD5:
                return md5(source);
        }
        throw new UnsupportedOperationException("Unsupported checksum algorithm " + algorithm.name());
    }

    /**
     * Hashes the provided {@link BufferedSource} using the provided {@link ChecksumAlgorithm} and returns a {@link String} encoded in hexadecimal.
     *
     * @param source    the {@link BufferedSource} to hash
     * @param algorithm the {@link ChecksumAlgorithm} to use
     * @return the calculated hash corresponding to <code>algorithm</code> or null if <code>algorithm == ChecksumAlgorithm.NONE</code>
     */
    public static String hashHex(final BufferedSource source, final ChecksumAlgorithm algorithm) {
        if (algorithm == ChecksumAlgorithm.NONE) {
            return null;
        }
        return hash(source, algorithm).hex();
    }

    /**
     * Hashes the provided {@link BufferedSource} using SHA1 and returns a {@link ByteString}.
     *
     * @param source the {@link BufferedSource} to hash
     * @return the calculated hash
     */
    public static ByteString sha1(final BufferedSource source) {
        return source.buffer().sha1();
    }

    /**
     * Hashes the provided {@link BufferedSource} using SHA1 and returns a {@link String} encoded in hexadecimal.
     *
     * @param source the {@link BufferedSource} to hash
     * @return the calculated hash
     */
    public static String sha1Hex(final BufferedSource source) {
        return sha1(source).hex();
    }

    /**
     * Hashes the provided {@link BufferedSource} using MD5 and returns a {@link ByteString}.
     *
     * @param source the {@link BufferedSource} to hash
     * @return the calculated hash
     */
    public static ByteString md5(final BufferedSource source) {
        return source.buffer().md5();
    }

    /**
     * Hashes the provided {@link BufferedSource} using MD5 and returns a {@link String} encoded in hexadecimal.
     *
     * @param source the {@link BufferedSource} to hash
     * @return the calculated hash
     */
    public static String md5Hex(final BufferedSource source) {
        return md5(source).hex();
    }

}
