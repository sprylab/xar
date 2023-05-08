package com.sprylab.xar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.sprylab.xar.XarTocParser;
import com.sprylab.xar.toc.TocFactory;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.utils.FilePath;
import com.sprylab.xar.utils.StringUtils;

/**
 * Describes the table of content of an eXtensible ARchiver file represented by a {@link XarSource}
 * (see <a href="https://github.com/mackyle/xar/wiki/xarformat#The_Table_of_Contents">specification</a>).
 */
public class XarToc {

    private final ToC model;

    private final List<XarEntry> entries = new ArrayList<>();

    private final Map<String, XarEntry> nameToEntryMap = new HashMap<>();

    private final XarSource xarSource;

    public XarToc(final XarSource xarSource, final XarTocParser parser) throws XarException {
        this.xarSource = xarSource;
        try (final InputStream inputStream = xarSource.getToCStream()) {
            this.model = parser != null ? parser.parse(inputStream) : TocFactory.fromInputStream(inputStream);
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
            final XarEntry xarEntry = XarEntry.createFromXarSource(this.xarSource, fileEntry, currentFile.getParentPath());

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
