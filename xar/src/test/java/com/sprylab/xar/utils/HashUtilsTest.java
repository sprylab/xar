package com.sprylab.xar.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;

import okio.Buffer;

public class HashUtilsTest {

    private static final String HASH_INPUT = "abc";

    private static final String HASH_VALUE_SHA1 = "a9993e364706816aba3e25717850c26c9cd0d89d";

    private static final String HASH_VALUE_MD5 = "900150983cd24fb0d6963f7d28e17f72";

    private Buffer buffer;

    @Before
    public void setUp() throws Exception {
        buffer = new Buffer();
        buffer.writeUtf8(HASH_INPUT);
    }

    @Test
    public void testHashHex() throws Exception {
        assertHash(HashUtils.hashHex(buffer, ChecksumAlgorithm.NONE), null);
        assertHash(HashUtils.hashHex(buffer, ChecksumAlgorithm.SHA1), HASH_VALUE_SHA1);
        assertHash(HashUtils.hashHex(buffer, ChecksumAlgorithm.MD5), HASH_VALUE_MD5);
    }

    @Test
    public void testSha1Hex() throws Exception {
        assertHash(HashUtils.sha1Hex(buffer), HASH_VALUE_SHA1);
    }

    @Test
    public void testMd5Hex() throws Exception {
        assertHash(HashUtils.md5Hex(buffer), HASH_VALUE_MD5);
    }

    private void assertHash(final String actualHash, final String expectedHash) {
        final Buffer clonedBuffer = buffer.clone();
        assertThat(actualHash).isEqualTo(expectedHash);
        assertThat(clonedBuffer.size()).isEqualTo(HASH_INPUT.length());
        assertThat(clonedBuffer.readByteArray()).isEqualTo(HASH_INPUT.getBytes());
    }

}