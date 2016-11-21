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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joou.UInteger;
import org.joou.ULong;
import org.joou.UShort;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

/**
 * User: pschiffer
 * Date: 21.11.2016
 * Time: 21:46
 */
public class HttpXarSourceTest {

    private static final Pattern PATTERN_RANGE_HEADER = Pattern.compile("bytes=(\\d+)-(\\d+)");
    private static final String TEST_XAR_NONE_FILE_NAME = "test_none.xar";
    private static final String TEST_XAR_GZIP_FILE_NAME = "test_gzip.xar";
    private static final String UNPACKED_REFERENCE_FILES_DIR_NAME = "unpacked";

    @Rule
    public MockWebServer mockWebServer = new MockWebServer();

    private XarSource noneXarSource;
    private XarSource gzipXarSource;

    @Before
    public void setUpMockWebServer() throws Exception {
        final Dispatcher dispatcher = new TestFileDispatcher();
        mockWebServer.setDispatcher(dispatcher);
    }

    @Before
    public void setUp() throws Exception {
        noneXarSource = new HttpXarSource(mockWebServer.url("/" + HttpXarSourceTest.TEST_XAR_NONE_FILE_NAME).toString());

        gzipXarSource = new HttpXarSource(mockWebServer.url("/" + HttpXarSourceTest.TEST_XAR_GZIP_FILE_NAME).toString());
    }

    @Test
    public void checkNoneHeader() throws Exception {
        final XarHeader header = noneXarSource.getHeader();
        assertEquals(UInteger.valueOf(0x78617221), header.getMagic());
        assertEquals(UShort.valueOf(28), header.getSize());
        assertEquals(UShort.valueOf(1), header.getVersion());
        assertEquals(ULong.valueOf(1040), header.getTocLengthCompressed());
        assertEquals(ULong.valueOf(5873), header.getTocLengthUncompressed());
        assertEquals(UInteger.valueOf(1), header.getCksumAlg());
    }

    @Test
    public void checkGzipHeader() throws Exception {
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
    public void testExtractAllFiles() throws IOException, URISyntaxException {
        for (final XarSource xarFile : getXarFiles()) {
            checkExtractAllFiles(xarFile, false);
            checkExtractAllFiles(xarFile, true);
        }
    }

    private void checkExtractAllFiles(final XarSource xarSource, final boolean check)
        throws IOException, URISyntaxException {
        final File tempDir = TestUtil.getTempDirectory();
        xarSource.extractAll(tempDir, check, new XarSource.OnEntryExtractedListener() {

            @Override
            public void onEntryExtracted(final XarEntry entry) {
                try {
                    assertTrue(xarSource.hasEntry(entry.getName()));
                } catch (final XarException e) {
                    e.printStackTrace();
                }
            }
        });

        // compare extracted files with reference files
        final File unpackedReferenceDirectory = TestUtil.getClasspathResourceAsFile(HttpXarSourceTest.UNPACKED_REFERENCE_FILES_DIR_NAME);

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
        xarEntry.extract(tempDir, check, new XarSource.OnEntryExtractedListener() {

            @Override
            public void onEntryExtracted(final XarEntry entry) {
                try {
                    assertTrue(xarFile.hasEntry(entry.getName()));
                } catch (final XarException e) {
                    e.printStackTrace();
                }
            }
        });

        // compare extracted files with reference files
        final File unpackedReferenceDirectory = TestUtil.getClasspathResourceAsFile(HttpXarSourceTest.UNPACKED_REFERENCE_FILES_DIR_NAME);

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

    private static class TestFileDispatcher extends Dispatcher {

        @Override
        public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {
            try {
                final MockResponse mockResponse = new MockResponse();
                final File file = TestUtil.getClasspathResourceAsFile(recordedRequest.getPath().substring(1));
                if (file == null || !file.exists()) {
                    return mockResponse.setResponseCode(404);
                }
                final Buffer buffer = new Buffer();
                final String range = recordedRequest.getHeader("Range");
                final BufferedSource source = Okio.buffer(Okio.source(file));
                if (range != null) {
                    // Partial file
                    final Matcher matcher = PATTERN_RANGE_HEADER.matcher(range);
                    if (matcher.matches()) {
                        final Integer start = Integer.valueOf(matcher.group(1));
                        final Integer end = Integer.valueOf(matcher.group(2));
                        source.skip(start);
                        source.readFully(buffer, end - start + 1);
                    } else {
                        // Full file
                        source.readAll(buffer);
                    }
                } else {
                    // Full file
                    source.readAll(buffer);
                }
                mockResponse.setBody(buffer);

                return mockResponse;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
