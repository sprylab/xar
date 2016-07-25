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

public class XarFileTest {

    private static final String TEST_XAR_NONE_FILE_NAME = "test_none.xar";

    private static final String TEST_XAR_GZIP_FILE_NAME = "test_gzip.xar";

    private static final String UNPACKED_REFERENCE_FILES_DIR_NAME = "unpacked";

    private File noneTestFile;

    private File gzipTestFile;

    private XarFile noneXarFile;

    private XarFile gzipXarFile;

    @Before
    public void setUp() throws Exception {
        noneTestFile = TestUtil.getClasspathResourceAsFile(TEST_XAR_NONE_FILE_NAME);
        noneXarFile = new XarFile(noneTestFile);

        gzipTestFile = TestUtil.getClasspathResourceAsFile(TEST_XAR_GZIP_FILE_NAME);
        gzipXarFile = new XarFile(gzipTestFile);
    }

    @Test
    public void testOpenXar() {
        assertNotNull(noneTestFile);
        assertNotNull(noneXarFile);

        assertNotNull(gzipTestFile);
        assertNotNull(gzipXarFile);

        checkNoneHeader();

        checkGzipHeader();
    }

    private void checkNoneHeader() {
        final XarFile.Header header = noneXarFile.getHeader();
        assertEquals(UInteger.valueOf(0x78617221), header.getMagic());
        assertEquals(UShort.valueOf(28), header.getSize());
        assertEquals(UShort.valueOf(1), header.getVersion());
        assertEquals(ULong.valueOf(1040), header.getTocLengthCompressed());
        assertEquals(ULong.valueOf(5873), header.getTocLengthUncompressed());
        assertEquals(UInteger.valueOf(1), header.getCksumAlg());
    }

    private void checkGzipHeader() {
        final XarFile.Header header = gzipXarFile.getHeader();
        assertEquals(UInteger.valueOf(0x78617221), header.getMagic());
        assertEquals(UShort.valueOf(28), header.getSize());
        assertEquals(UShort.valueOf(1), header.getVersion());
        assertEquals(ULong.valueOf(1041), header.getTocLengthCompressed());
        assertEquals(ULong.valueOf(5873), header.getTocLengthUncompressed());
        assertEquals(UInteger.valueOf(1), header.getCksumAlg());
    }

    @Test
    public void testListEntries() {
        for (final XarFile xarFile : getXarFiles()) {
            checkEntries(xarFile);
        }
    }

    private void checkEntries(final XarFile xarFile) {
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

        final List<XarEntry> xarEntries = xarFile.getEntries();

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
        for (final XarFile xarFile : getXarFiles()) {
            checkSpecificEntries(xarFile);
        }
    }

    private void checkSpecificEntries(final XarFile xarFile) {
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
        for (final XarFile xarFile : getXarFiles()) {
            checkEntry(xarFile, "file.txt", getResourceAsByteArray("unpacked/file.txt"));
            checkEntry(xarFile, "dir/subdir1/subsubdir_1/subsubdir_file_1.txt",
                       getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_1/subsubdir_file_1.txt"));
            checkEntry(xarFile, "dir/subdir1/subsubdir_2/empty_file.txt",
                       getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_2/empty_file.txt"));
            checkEntry(xarFile, "dir/subdir1/subsubdir_3/1.txt",
                       getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_3/1.txt"));
        }
    }

    private byte[] getResourceAsByteArray(final String resourceName) throws URISyntaxException, IOException {
        return IOUtils.toByteArray(FileUtils.openInputStream(TestUtil.getClasspathResourceAsFile(resourceName)));
    }

    private void checkEntry(final XarFile xarFile, final String entryName, final byte[] expectedContent)
        throws IOException {
        final XarEntry xarEntry = xarFile.getEntry(entryName);

        assertEquals(entryName, xarEntry.getName());

        final byte[] actualContent = xarEntry.getBytes();
        final String actualContentString = new String(actualContent).trim();
        final String expectedContentString = new String(expectedContent).trim();
        assertEquals("Content of extracted file is not equal.", expectedContentString, actualContentString);
    }

    @Test
    public void testFileSize() {
        assertEquals(noneTestFile.length(), noneXarFile.getSize());
        assertEquals(gzipTestFile.length(), gzipXarFile.getSize());
    }

    @Test
    public void testExtractAllFiles() throws IOException, URISyntaxException {
        for (final XarFile xarFile : getXarFiles()) {
            checkExtractAllFiles(xarFile, false);
            checkExtractAllFiles(xarFile, true);
        }
    }

    private void checkExtractAllFiles(final XarFile xarFile, final boolean check)
        throws IOException, URISyntaxException {
        final File tempDir = TestUtil.getTempDirectory();
        xarFile.extractAll(tempDir, check, new XarEntry.OnEntryExtractedListener() {

            @Override
            public void onEntryExtracted(final XarEntry entry) {
                assertTrue(xarFile.hasEntry(entry.getName()));
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
        for (final XarFile xarFile : getXarFiles()) {
            checkExtractDirectory(xarFile, false);
            checkExtractDirectory(xarFile, true);
        }
    }


    private void checkExtractDirectory(final XarFile xarFile, final boolean check)
        throws IOException, URISyntaxException {
        final File tempDir = TestUtil.getTempDirectory();
        final XarEntry xarEntry = xarFile.getEntry("dir");
        xarEntry.extract(tempDir, check, new XarEntry.OnEntryExtractedListener() {

            @Override
            public void onEntryExtracted(final XarEntry entry) {
                assertTrue(xarFile.hasEntry(entry.getName()));
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

    private List<XarFile> getXarFiles() {
        return Arrays.asList(noneXarFile, gzipXarFile);
    }

}
