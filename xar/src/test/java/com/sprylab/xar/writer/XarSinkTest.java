package com.sprylab.xar.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sprylab.xar.FileXarSource;
import com.sprylab.xar.TestUtil;
import com.sprylab.xar.XarEntry;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;

public class XarSinkTest {

    private XarSink xarSink;

    private File tempDirectory;

    private File archiveFile;

    @Before
    public void setUp() {
        xarSink = new XarSink();
        tempDirectory = TestUtil.getTempDirectory();
        archiveFile = new File(tempDirectory, "test.xar");
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(tempDirectory);
    }

    @Test
    public void testAddSimpleFileSourceToRoot() throws Exception {
        final File fileToCompress = TestUtil.getClasspathResourceAsFile("unpacked/file.txt");

        final XarFileSource fileSource = new XarFileSource(fileToCompress);
        xarSink.addSource(fileSource);

        xarSink.write(FileUtils.openOutputStream(archiveFile));

        assertThat(archiveFile).exists();

        final FileXarSource fileXarSource = new FileXarSource(archiveFile);
        final XarEntry entry = fileXarSource.getEntry("file.txt");
        assertThatEntryEqualsFile(entry, fileToCompress);
    }

    @Test
    public void testAddCompressedAndHashedFileSourceToRoot() throws Exception {
        final File fileToCompress = TestUtil.getClasspathResourceAsFile("unpacked/file.txt");

        final XarFileSource fileSource = new XarFileSource(fileToCompress, Encoding.GZIP, ChecksumAlgorithm.SHA1);
        xarSink.addSource(fileSource);

        xarSink.write(FileUtils.openOutputStream(archiveFile));

        assertThat(archiveFile).exists();

        final FileXarSource fileXarSource = new FileXarSource(archiveFile);
        final XarEntry entry = fileXarSource.getEntry("file.txt");
        assertThatEntryEqualsFile(entry, fileToCompress);
    }

    @Test
    public void testAddSimpleFileSourceToParentDirectory() throws Exception {
        final File fileToCompress = TestUtil.getClasspathResourceAsFile("unpacked/file.txt");

        final XarDirectory xarDirectory = new XarSimpleDirectory("parent");
        xarSink.addDirectory(xarDirectory, null);

        final XarFileSource fileSource = new XarFileSource(fileToCompress);
        xarSink.addSource(fileSource, xarDirectory);

        xarSink.write(FileUtils.openOutputStream(archiveFile));

        assertThat(archiveFile).exists();

        final FileXarSource fileXarSource = new FileXarSource(archiveFile);
        final XarEntry entry = fileXarSource.getEntry("parent/file.txt");
        assertThatEntryEqualsFile(entry, fileToCompress);
    }

    private void assertThatEntryEqualsFile(final XarEntry entry, final File file) throws Exception {
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isEqualTo(file.isDirectory());
        assertThat(entry.getBytes()).isEqualTo(FileUtils.readFileToByteArray(file));

        switch (entry.getChecksumAlgorithm()) {
            case NONE:
                assertThat(entry.getChecksum()).isNullOrEmpty();
                break;
            case SHA1:
                assertThat(entry.getChecksum()).isEqualTo(DigestUtils.sha1Hex(FileUtils.openInputStream(file)));
                break;
            case MD5:
                assertThat(entry.getChecksum()).isEqualTo(DigestUtils.md5Hex(FileUtils.openInputStream(file)));
                break;
        }
    }
}