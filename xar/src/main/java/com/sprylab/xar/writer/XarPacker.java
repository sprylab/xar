package com.sprylab.xar.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.utils.StringUtils;

public class XarPacker {

    private static final Set<String> DEFAULT_PACK_EXTENSIONS = new HashSet<>();

    static {
        DEFAULT_PACK_EXTENSIONS.add("txt");
        DEFAULT_PACK_EXTENSIONS.add("htm");
        DEFAULT_PACK_EXTENSIONS.add("html");
        DEFAULT_PACK_EXTENSIONS.add("css");
        DEFAULT_PACK_EXTENSIONS.add("js");
        DEFAULT_PACK_EXTENSIONS.add("xml");
        DEFAULT_PACK_EXTENSIONS.add("stxml");
    }

    private final File destFile;

    private final XarWriter writer;

    public XarPacker(final File archiveFile) {
        destFile = archiveFile;
        if (destFile.exists()) {
            destFile.delete();
        }
        writer = new XarWriter();
    }

    public void addDirectory(final File folder, final boolean asSubFolder, final Set<String> packedExtensions) throws Exception {
        XarDirectory root = null;
        if (asSubFolder) {
            root = new XarSimpleDirectory(folder.getName());
            writer.addDirectory(root, null);
        }
        addDirectoryContent(folder, root, packedExtensions == null ? DEFAULT_PACK_EXTENSIONS : null);
    }

    public void addDirectoryContent(final File folder, final XarDirectory parent, final Set<String> packedExtensions) throws Exception {
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                final XarDirectory dir = new XarSimpleDirectory(file.getName());
                writer.addDirectory(dir, parent);
                addDirectoryContent(file, dir, packedExtensions);
            } else {
                final boolean compress = packedExtensions.contains(StringUtils.substringAfterLast(file.getName(), "."));
                final XarSource source = new XarFileSource(file, compress ? Encoding.GZIP : Encoding.NONE);
                writer.addSource(source, parent);
            }
        }
    }

    public void write() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            writer.write(fos);
        }
    }

}
