package com.sprylab.xar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.utils.FilePath;
import com.sprylab.xar.utils.StringUtils;

import okio.BufferedSource;
import okio.Okio;

/**
 * User: pschiffer
 * Date: 21.11.2016
 * Time: 21:55
 */
public class XarToc {

    private final ToC model;

    private final List<XarEntry> entries = new ArrayList<>();

    private final Map<String, XarEntry> nameToEntryMap = new HashMap<>();
    private final XarSource xarSource;

    public static XarToc createToc(final XarSource file) throws XarException {
        try (final BufferedSource source = Okio.buffer(file.getToCSource())) {
            return new XarToc(file, source);
        } catch (final IOException e) {
            throw new XarException("Error opening XarFile", e);
        }
    }

    public XarToc(final XarSource XarSource, final BufferedSource source) throws XarException {
        try (final InputStream inputStream = source.inputStream()) {
            this.model = ToCFactory.fromInputStream(inputStream);
            this.xarSource = XarSource;
            createEntries();
        } catch (final Exception e) {
            throw new XarException("Could not create toc", e);
        }
    }

    private void createEntries() throws XarException {
        // Unfortunately simple-xml throws Exceptions
        //noinspection OverlyBroadCatchBlock
        final Stack<FilePath> fileStack = new Stack<>();
        fileStack.addAll(FilePath.fromFileList(this.model.getFiles()));

        while (!fileStack.isEmpty()) {
            final FilePath currentFile = fileStack.pop();
            final com.sprylab.xar.toc.model.File fileEntry = currentFile.getFile();
            final XarEntry xarEntry = XarEntry.createFromFile(this.xarSource, fileEntry, currentFile.getParentPath());

            if (xarEntry.isDirectory()) {
                final List<com.sprylab.xar.toc.model.File> children = fileEntry.getChildren();
                if (children != null && !children.isEmpty()) {
                    fileStack.addAll(FilePath.fromFileList(children, xarEntry.getName()));
                }
            }
            addEntry(xarEntry);
            addToParentEntry(xarEntry, currentFile.getParentPath());
        }
    }

    private void addEntry(final XarEntry xarEntry) {
        entries.add(xarEntry);
        nameToEntryMap.put(xarEntry.getName(), xarEntry);
    }

    private void addToParentEntry(final XarEntry xarEntry, final String parentPath) {
        if (StringUtils.isEmpty(parentPath)) {
            // the entry itself is in the root entry
            return;
        }
        final XarEntry parentEntry = nameToEntryMap.get(parentPath);
        parentEntry.addChild(xarEntry);
    }

    public List<XarEntry> getEntries() {
        return entries;
    }

    public XarEntry getEntry(final String entryName) {
        return nameToEntryMap.get(entryName);
    }

    public boolean hasEntry(final String entryName) {
        return nameToEntryMap.containsKey(entryName);
    }
}
