package com.sprylab.xar.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sprylab.xar.toc.model.File;

/**
 * Represents a {@link File} with a given path location information.
 */
public class FilePath {

    private final File file;

    private final String parentPath;

    /**
     * Creates a new file path segment.
     *
     * @param file       the file represented by this {@link FilePath}
     * @param parentPath the name of the parent directory of {@code file}
     */
    public FilePath(final File file, final String parentPath) {
        this.file = file;
        this.parentPath = parentPath;
    }

    /**
     * Creates a new file path segment with no parent directory.
     *
     * @param files the file represented by this {@link FilePath}
     * @return a list of {@link FilePath} segments
     */
    public static List<FilePath> fromFileList(final Collection<File> files) {
        return fromFileList(files, "");
    }

    /**
     * Creates a list of {@link FilePath}'s for the given files and assigns {@code parentPath} as their parent directory.
     *
     * @param files      the files representing this {@link FilePath}
     * @param parentPath the name of the parent directory of {@code files}
     * @return a list of {@link FilePath} segments
     */
    public static List<FilePath> fromFileList(final Collection<File> files, final String parentPath) {
        final List<FilePath> filePaths = new ArrayList<>(files.size());
        for (final File file : files) {
            filePaths.add(new FilePath(file, parentPath));
        }
        return filePaths;
    }

    /**
     * @return the {@link File} represented by this file path.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return Returns the name of the parent directory of {@link #getFile()} represented by this file path.
     */
    public String getParentPath() {
        return parentPath;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
