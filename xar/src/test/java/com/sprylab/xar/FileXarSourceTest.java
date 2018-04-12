package com.sprylab.xar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joou.UInteger;
import org.joou.ULong;
import org.joou.UShort;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileXarSourceTest {

    private static final Logger LOG = LoggerFactory.getLogger(FileXarSourceTest.class);

    private static final String TEST_XAR_NONE_FILE_NAME = "test_none.xar";

    private static final String TEST_XAR_GZIP_FILE_NAME = "test_gzip.xar";

    private static final String UNPACKED_REFERENCE_FILES_DIR_NAME = "unpacked";

    private File noneTestFile;

    private File gzipTestFile;

    private XarSource noneXarSource;

    private XarSource gzipXarSource;

    @Before
    public void setUp() throws Exception {
        noneTestFile = TestUtil.getClasspathResourceAsFile(TEST_XAR_NONE_FILE_NAME);
        noneXarSource = new FileXarSource(noneTestFile);

        gzipTestFile = TestUtil.getClasspathResourceAsFile(TEST_XAR_GZIP_FILE_NAME);
        gzipXarSource = new FileXarSource(gzipTestFile);
    }

    @Test
    public void testOpenXar() throws Exception {
        assertNotNull(noneTestFile);
        assertNotNull(noneXarSource);

        assertNotNull(gzipTestFile);
        assertNotNull(gzipXarSource);

        checkNoneHeader();

        checkGzipHeader();
    }

    private void checkNoneHeader() throws XarException {
        final XarHeader header = noneXarSource.getHeader();
        assertEquals(UInteger.valueOf(0x78617221), header.getMagic());
        assertEquals(UShort.valueOf(28), header.getSize());
        assertEquals(UShort.valueOf(1), header.getVersion());
        assertEquals(ULong.valueOf(1040), header.getTocLengthCompressed());
        assertEquals(ULong.valueOf(5873), header.getTocLengthUncompressed());
        assertEquals(UInteger.valueOf(1), header.getCksumAlg());
    }

    private void checkGzipHeader() throws XarException {
        final XarHeader header = gzipXarSource.getHeader();
        assertEquals(UInteger.valueOf(0x78617221), header.getMagic());
        assertEquals(UShort.valueOf(28), header.getSize());
        assertEquals(UShort.valueOf(1), header.getVersion());
        assertEquals(ULong.valueOf(1041), header.getTocLengthCompressed());
        assertEquals(ULong.valueOf(5873), header.getTocLengthUncompressed());
        assertEquals(UInteger.valueOf(1), header.getCksumAlg());
    }

    @Test
    public void testListEntries() throws XarException {
        for (final XarSource xarSource : getXarFiles()) {
            checkEntries(xarSource);
        }
    }

    private void checkEntries(final XarSource xarSource) throws XarException {
        final Map<String, Boolean> entriesToFind = new HashMap<>();

        entriesToFind.put("file.txt", Boolean.FALSE);
        entriesToFind.put("dir", Boolean.FALSE);
        entriesToFind.put("dir/subdir1", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_1", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_1/subsubdir_file_1.txt", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_2", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_2/empty_file.txt", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_3", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_3/1.txt", Boolean.FALSE);

        final List<XarEntry> xarEntries = xarSource.getEntries();

        assertEquals(9, xarEntries.size());

        for (final XarEntry xarEntry : xarEntries) {
            final String name = xarEntry.getName();
            final Boolean isFound = entriesToFind.get(name);
            if (isFound != null) {
                entriesToFind.put(name, Boolean.TRUE);
            }
        }

        for (final Map.Entry<String, Boolean> entry : entriesToFind.entrySet()) {
            assertTrue("Expected entry " + entry.getKey() + " not found.", entry.getValue());
        }
    }

    @Test
    public void testListSpecificEntries() throws Exception {
        for (final XarSource xarSource : getXarFiles()) {
            checkSpecificEntries(xarSource);
        }
    }

    private void checkSpecificEntries(final XarSource xarFile) throws XarException {
        final Map<String, Boolean> entriesToFind = new HashMap<>();

        entriesToFind.put("dir/subdir1/subsubdir_1", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_2", Boolean.FALSE);
        entriesToFind.put("dir/subdir1/subsubdir_3", Boolean.FALSE);

        final XarEntry entry = xarFile.getEntry("dir/subdir1");
        assertNotNull(entry);
        assertNotNull(entry.getChildren());
        final List<XarEntry> children = entry.getChildren();

        assertEquals(3, children.size());

        for (final XarEntry xarEntry : children) {
            final String name = xarEntry.getName();
            final Boolean isFound = entriesToFind.get(name);
            if (isFound != null) {
                entriesToFind.put(name, Boolean.TRUE);
            }
        }

        for (final Map.Entry<String, Boolean> findingEntry : entriesToFind.entrySet()) {
            assertTrue("Expected entry " + findingEntry.getKey() + " not found.", findingEntry.getValue());
        }
    }

    @Test
    public void testGetEntry() throws Exception {
        for (final XarSource xarSource : getXarFiles()) {
            checkEntry(xarSource, "file.txt", getResourceAsByteArray("unpacked/file.txt"));
            checkEntry(xarSource, "dir/subdir1/subsubdir_1/subsubdir_file_1.txt",
                getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_1/subsubdir_file_1.txt"));
            checkEntry(xarSource, "dir/subdir1/subsubdir_2/empty_file.txt",
                getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_2/empty_file.txt"));
            checkEntry(xarSource, "dir/subdir1/subsubdir_3/1.txt",
                getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_3/1.txt"));
        }
    }

    private byte[] getResourceAsByteArray(final String resourceName) throws URISyntaxException, IOException {
        return IOUtils.toByteArray(FileUtils.openInputStream(TestUtil.getClasspathResourceAsFile(resourceName)));
    }

    private void checkEntry(final XarSource xarSource, final String entryName, final byte[] expectedContent)
        throws IOException {
        final XarEntry xarEntry = xarSource.getEntry(entryName);

        assertEquals(entryName, xarEntry.getName());

        final byte[] actualContent = xarEntry.getBytes();
        final String actualContentString = new String(actualContent).trim();
        final String expectedContentString = new String(expectedContent).trim();
        assertEquals("Content of extracted file is not equal.", expectedContentString, actualContentString);
    }

    @Test
    public void testFileSize() throws Exception {
        assertEquals(noneTestFile.length(), noneXarSource.getSize());
        assertEquals(gzipTestFile.length(), gzipXarSource.getSize());
    }

    @Test
    public void testExtractAllFiles() throws Exception {
        for (final XarSource xarFile : getXarFiles()) {
            checkExtractAllFiles(xarFile, false);
            checkExtractAllFiles(xarFile, true);
        }
    }

    private void checkExtractAllFiles(final XarSource xarSource, final boolean check)
        throws IOException, URISyntaxException {
        final File tempDir = TestUtil.getTempDirectory();
        xarSource.extractAll(tempDir, check, entry -> {
            try {
                assertTrue(xarSource.hasEntry(entry.getName()));
            } catch (final XarException e) {
                e.printStackTrace();
            }
        });

        // compare extracted files with reference files
        final File unpackedReferenceDirectory = TestUtil.getClasspathResourceAsFile(UNPACKED_REFERENCE_FILES_DIR_NAME);

        final Collection<File> referenceFiles = FileUtils.listFiles(unpackedReferenceDirectory, null, true);
        for (final File referenceFile : referenceFiles) {
            final String relativeFileName = referenceFile.getAbsolutePath()
                .replace(unpackedReferenceDirectory.getAbsolutePath(), "");

            FileUtils.contentEquals(referenceFile, new File(tempDir, relativeFileName));
        }

        // clean up
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testExtractDir() throws Exception {
        for (final XarSource xarFile : getXarFiles()) {
            checkExtractDirectory(xarFile, false);
            checkExtractDirectory(xarFile, true);
        }
    }

    private void checkExtractDirectory(final XarSource xarFile, final boolean check)
        throws IOException, URISyntaxException {
        final File tempDir = TestUtil.getTempDirectory();
        final XarEntry xarEntry = xarFile.getEntry("dir");
        xarEntry.extract(tempDir, check, entry -> {
            try {
                assertTrue(xarFile.hasEntry(entry.getName()));
            } catch (final XarException e) {
                LOG.warn("Error extracting xar", e);
            }
        });

        // compare extracted files with reference files
        final File unpackedReferenceDirectory = TestUtil.getClasspathResourceAsFile(UNPACKED_REFERENCE_FILES_DIR_NAME);

        final Collection<File> referenceFiles = FileUtils.listFiles(unpackedReferenceDirectory, null, true);
        for (final File referenceFile : referenceFiles) {
            final String relativeFileName = referenceFile.getAbsolutePath()
                .replace(unpackedReferenceDirectory.getAbsolutePath(), "");

            FileUtils.contentEquals(referenceFile, new File(tempDir, relativeFileName));
        }

        // clean up
        FileUtils.deleteDirectory(tempDir);
    }

    private List<XarSource> getXarFiles() {
        return Arrays.asList(noneXarSource, gzipXarSource);
    }
}
