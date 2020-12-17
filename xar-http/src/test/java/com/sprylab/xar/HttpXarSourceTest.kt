package com.sprylab.xar

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.joou.UInteger
import org.joou.ULong
import org.joou.UShort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import java.util.regex.Pattern

class HttpXarSourceTest {
    @Rule @JvmField
    val mockWebServer = MockWebServer()

    lateinit var noneXarSource: XarSource
    lateinit var gzipXarSource: XarSource

    @Before
    @Throws(Exception::class)
    fun setUpMockWebServer() {
        val dispatcher: Dispatcher = TestFileDispatcher()
        mockWebServer.dispatcher = dispatcher
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        noneXarSource = HttpXarSource(mockWebServer.url("/$TEST_XAR_NONE_FILE_NAME").toString())
        gzipXarSource = HttpXarSource(mockWebServer.url("/$TEST_XAR_GZIP_FILE_NAME").toString())
    }

    @Test
    @Throws(Exception::class)
    fun checkNoneHeader() {
        val header = noneXarSource.header
        assertEquals(UInteger.valueOf(0x78617221), header.magic)
        assertEquals(UShort.valueOf(28), header.size)
        assertEquals(UShort.valueOf(1), header.version)
        assertEquals(ULong.valueOf(1040), header.tocLengthCompressed)
        assertEquals(ULong.valueOf(5873), header.tocLengthUncompressed)
        assertEquals(UInteger.valueOf(1), header.cksumAlg)
    }

    @Test
    @Throws(Exception::class)
    fun checkGzipHeader() {
        val header = gzipXarSource.header
        assertEquals(UInteger.valueOf(0x78617221), header.magic)
        assertEquals(UShort.valueOf(28), header.size)
        assertEquals(UShort.valueOf(1), header.version)
        assertEquals(ULong.valueOf(1041), header.tocLengthCompressed)
        assertEquals(ULong.valueOf(5873), header.tocLengthUncompressed)
        assertEquals(UInteger.valueOf(1), header.cksumAlg)
    }

    @Test
    @Throws(XarException::class)
    fun testListEntries() {
        for (xarSource in xarFiles) {
            checkEntries(xarSource)
        }
    }

    @Throws(XarException::class)
    private fun checkEntries(xarSource: XarSource) {
        val entriesToFind: MutableMap<String, Boolean> = HashMap()
        entriesToFind["file.txt"] = java.lang.Boolean.FALSE
        entriesToFind["dir"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_1"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_1/subsubdir_file_1.txt"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_2"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_2/empty_file.txt"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_3"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_3/1.txt"] = java.lang.Boolean.FALSE
        val xarEntries = xarSource.entries
        assertEquals(9, xarEntries.size.toLong())
        for (xarEntry in xarEntries) {
            val name = xarEntry.name
            val isFound = entriesToFind[name]
            if (isFound != null) {
                entriesToFind[name] = java.lang.Boolean.TRUE
            }
        }
        for ((key, value) in entriesToFind) {
            assertTrue("Expected entry $key not found.", value)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testListSpecificEntries() {
        for (xarSource in xarFiles) {
            checkSpecificEntries(xarSource)
        }
    }

    @Throws(XarException::class)
    private fun checkSpecificEntries(xarFile: XarSource) {
        val entriesToFind: MutableMap<String, Boolean> = HashMap()
        entriesToFind["dir/subdir1/subsubdir_1"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_2"] = java.lang.Boolean.FALSE
        entriesToFind["dir/subdir1/subsubdir_3"] = java.lang.Boolean.FALSE
        val entry = xarFile.getEntry("dir/subdir1")
        assertNotNull(entry)
        assertNotNull(entry.children)
        val children = entry.children
        assertEquals(3, children.size.toLong())
        for (xarEntry in children) {
            val name = xarEntry.name
            val isFound = entriesToFind[name]
            if (isFound != null) {
                entriesToFind[name] = java.lang.Boolean.TRUE
            }
        }
        for ((key, value) in entriesToFind) {
            assertTrue("Expected entry $key not found.", value)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetEntry() {
        for (xarSource in xarFiles) {
            checkEntry(xarSource, "file.txt", getResourceAsByteArray("unpacked/file.txt"))
            checkEntry(
                xarSource, "dir/subdir1/subsubdir_1/subsubdir_file_1.txt",
                getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_1/subsubdir_file_1.txt")
            )
            checkEntry(
                xarSource, "dir/subdir1/subsubdir_2/empty_file.txt",
                getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_2/empty_file.txt")
            )
            checkEntry(
                xarSource, "dir/subdir1/subsubdir_3/1.txt",
                getResourceAsByteArray("unpacked/dir/subdir1/subsubdir_3/1.txt")
            )
        }
    }

    @Throws(URISyntaxException::class, IOException::class)
    private fun getResourceAsByteArray(resourceName: String): ByteArray {
        return IOUtils.toByteArray(FileUtils.openInputStream(getClasspathResourceAsFile(resourceName)))
    }

    @Throws(IOException::class)
    private fun checkEntry(xarSource: XarSource, entryName: String, expectedContent: ByteArray) {
        val xarEntry = xarSource.getEntry(entryName)
        assertEquals(entryName, xarEntry.name)
        val actualContent = xarEntry.bytes
        val actualContentString = String(actualContent).trim { it <= ' ' }
        val expectedContentString = String(expectedContent).trim { it <= ' ' }
        assertEquals("Content of extracted file is not equal.", expectedContentString, actualContentString)
    }

    @Test
    @Throws(Exception::class)
    fun testFileSize() {
        assertEquals(getResourceAsByteArray(TEST_XAR_NONE_FILE_NAME).size.toLong(), noneXarSource.size)
        assertEquals(getResourceAsByteArray(TEST_XAR_GZIP_FILE_NAME).size.toLong(), gzipXarSource.size)
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun testExtractAllFiles() {
        for (xarFile in xarFiles) {
            checkExtractAllFiles(xarFile, false)
            checkExtractAllFiles(xarFile, true)
        }
    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun checkExtractAllFiles(xarSource: XarSource, check: Boolean) {
        val tempDir = getTempDirectory()
        xarSource.extractAll(tempDir, check) { entry ->
            try {
                assertTrue(xarSource.hasEntry(entry.name))
            } catch (e: XarException) {
                e.printStackTrace()
            }
        }

        // compare extracted files with reference files
        val unpackedReferenceDirectory = getClasspathResourceAsFile(UNPACKED_REFERENCE_FILES_DIR_NAME)!!
        val referenceFiles = FileUtils.listFiles(unpackedReferenceDirectory, null, true)
        for (referenceFile in referenceFiles) {
            val relativeFileName = referenceFile.absolutePath
                .replace(unpackedReferenceDirectory.absolutePath, "")
            FileUtils.contentEquals(referenceFile, File(tempDir, relativeFileName))
        }

        // clean up
        FileUtils.deleteDirectory(tempDir)
    }

    @Test
    @Throws(Exception::class)
    fun testExtractDir() {
        for (xarFile in xarFiles) {
            checkExtractDirectory(xarFile, false)
            checkExtractDirectory(xarFile, true)
        }
    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun checkExtractDirectory(xarFile: XarSource, check: Boolean) {
        val tempDir = getTempDirectory()
        val xarEntry = xarFile.getEntry("dir")
        xarEntry.extract(tempDir, check) { entry ->
            try {
                assertTrue(xarFile.hasEntry(entry.name))
            } catch (e: XarException) {
                e.printStackTrace()
            }
        }

        // compare extracted files with reference files
        val unpackedReferenceDirectory = getClasspathResourceAsFile(UNPACKED_REFERENCE_FILES_DIR_NAME)!!
        val referenceFiles = FileUtils.listFiles(unpackedReferenceDirectory, null, true)
        for (referenceFile in referenceFiles) {
            val relativeFileName = referenceFile.absolutePath
                .replace(unpackedReferenceDirectory.absolutePath, "")
            FileUtils.contentEquals(referenceFile, File(tempDir, relativeFileName))
        }

        // clean up
        FileUtils.deleteDirectory(tempDir)
    }

    private val xarFiles: List<XarSource>
        get() = listOf(noneXarSource, gzipXarSource)

    companion object {
        val PATTERN_RANGE_HEADER: Pattern = "bytes=(\\d+)-(\\d+)".toPattern()
        const val TEST_XAR_NONE_FILE_NAME = "test_none.xar"
        const val TEST_XAR_GZIP_FILE_NAME = "test_gzip.xar"
        const val UNPACKED_REFERENCE_FILES_DIR_NAME = "unpacked"
    }
}
