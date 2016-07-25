package com.sprylab.xar.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sprylab.xar.toc.model.File;

/**
 * Represents a {@link File} with a given path location information.
 *
 * @author hbakici
 */
public class FilePath {

    public static List<FilePath> fromFileList(final Collection<File> files) {
        return fromFileList(files, "");
    }

    public static List<FilePath> fromFileList(final Collection<File> files, final String parentPath) {
        final List<FilePath> filePaths = new ArrayList<>(files.size());
        for (final File file : files) {
            filePaths.add(new FilePath(file, parentPath));
        }
        return filePaths;
    }

    private final File file;

    private final String parentPath;

    public FilePath(final File file, final String parentPath) {
        this.file = file;
        this.parentPath = parentPath;
    }

    public File getFile() {
        return file;
    }


    public String getParentPath() {
        return parentPath;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
