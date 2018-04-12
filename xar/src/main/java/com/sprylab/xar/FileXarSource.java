package com.sprylab.xar;

import java.io.File;
import java.io.IOException;

import com.sprylab.xar.utils.RandomAccessFileSource;

import okio.BufferedSource;
import okio.Okio;

/**
 * Represents an eXtensible ARchiver using a {@link File}.
 */
public class FileXarSource extends XarSource {

    private final File file;

    /**
     * Creates a new {@link XarSource} from {@code file}.
     *
     * @param file the archive file
     */
    public FileXarSource(final File file) {
        this.file = file;
    }

    @Override
    public BufferedSource getRange(final long offset, final long length) throws IOException {
        return Okio.buffer(new RandomAccessFileSource(file, offset, length));
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    /**
     * @return the underlying {@link File}
     */
    public File getFile() {
        return file;
    }

}
