package com.sprylab.xar.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;

public class XarPacker {

    private static final Set<String> DEFAULT_PACK_EXTENSIONS = new HashSet<String>();

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

    public XarPacker(final String destFileName) {
        destFile = new File(destFileName);
        if (destFile.exists()) {
            destFile.delete();
        }
        writer = new XarWriter(ChecksumAlgorithm.SHA1);
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
        for (final File f : folder.listFiles()) {
            if (f.isDirectory()) {
                final XarDirectory dir = new XarSimpleDirectory(f.getName());
                writer.addDirectory(dir, parent);
                addDirectoryContent(f, dir, packedExtensions);
            } else {
                final int sep = f.getName().lastIndexOf('.');
                final boolean compress = (sep >= 0) && packedExtensions.contains(f.getName().substring(sep + 1));
                final XarSource source = new XarFileSource(f, compress);
                writer.addSource(source, parent);
            }
        }
    }

    public void write() throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
            writer.write(fos);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    public static void main(final String[] args) {
        if ((args.length == 0) || (args.length > 2)) {
            System.out.println("Usage: XarPacker <directory> [<target-file>]");
            return;
        }
        final File folder = new File(args[0]);
        if (!folder.isDirectory()) {
            System.out.println("Directory not found: " + args[0]);
            return;
        }

        final String destFileName = (args.length == 1) ? args[0] + ".xar" : args[1];
        try {
            final XarPacker packer = new XarPacker(destFileName);
            packer.addDirectory(folder, false, null);
            packer.write();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
