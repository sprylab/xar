package com.sprylab.xar;

import java.io.File;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;

public final class TestUtil {

   public static File getTempDirectory() {
       return new File(FileUtils.getTempDirectory(), RandomStringUtils.randomAlphabetic(10));
   }

    public static File getClasspathResourceAsFile(final String name) throws URISyntaxException {
        return new File(Thread.currentThread().getContextClassLoader().getResource(name).toURI());
    }

}
