package com.sprylab.xar.toc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.sprylab.xar.TestUtil;
import com.sprylab.xar.toc.model.ToC;

public class ToCTest {

    private static final String TOC_NONE_XML_FILE_NAME = "toc_none.xml";

    private static final String TOC_GZIP_XML_FILE_NAME = "toc_gzip.xml";

    private static final String TOC_DUPLICATE_XML_FILE_NAME = "toc_duplicate.xml";

    private File noneToCFile;

    private File gzipToCFile;

    private File duplicateToCFile;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        noneToCFile = TestUtil.getClasspathResourceAsFile(TOC_NONE_XML_FILE_NAME);
        gzipToCFile = TestUtil.getClasspathResourceAsFile(TOC_GZIP_XML_FILE_NAME);
        duplicateToCFile = TestUtil.getClasspathResourceAsFile(TOC_DUPLICATE_XML_FILE_NAME);
    }

    @Test
    public void testToC() throws Exception {
        final ToC noneToC = TocFactory.fromInputStream(FileUtils.openInputStream(noneToCFile));
        assertNotNull(noneToC);

        final ToC gzipToC = TocFactory.fromInputStream(FileUtils.openInputStream(gzipToCFile));
        assertNotNull(gzipToC);

        final ToC duplicateToC = TocFactory.fromInputStream(FileUtils.openInputStream(duplicateToCFile));
        assertNotNull(duplicateToC);
    }
}
