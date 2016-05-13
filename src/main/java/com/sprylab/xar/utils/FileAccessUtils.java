package com.sprylab.xar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;

public class FileAccessUtils {

    /**
     * Creates a limited, buffered {@link InflaterInputStream} from a file.
     *
     * @param file   the file to read
     * @param offset the offset to start
     * @param length the number of bytes to read counting from offset
     * @return a buffered {@link InputStream}
     * @throws IOException
     */
    public static InputStream createLimitedInflaterInputStream(final File file, final long offset,
                                                               final long length) throws IOException {
        return new InflaterInputStream(createLimitedBufferedInputStream(file, offset, length));
    }

    public static InputStream createLimitedBufferedInputStream(final File file, final long offset,
                                                               final long length) throws IOException {
        final FileInputStream fileInputStream = FileUtils.openInputStream(file);
        fileInputStream.skip(offset);
        return IOUtils.toBufferedInputStream(new BoundedInputStream(fileInputStream, length));
    }

}
