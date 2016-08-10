package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root
public class Data {

    @Element(required = false)
    private SimpleChecksum extractedChecksum;

    @Element(required = false)
    private SimpleChecksum unarchivedChecksum;

    @Element(required = false)
    private SimpleChecksum archivedChecksum;

    @Path("encoding")
    @Attribute(name = "style", required = false)
    private Encoding encoding = Encoding.NONE;

    @Element
    private long size;

    @Element
    private long offset;

    @Element
    private long length;

    public SimpleChecksum getExtractedChecksum() {
        return extractedChecksum;
    }

    public void setExtractedChecksum(final SimpleChecksum extractedChecksum) {
        this.extractedChecksum = extractedChecksum;
    }

    public SimpleChecksum getUnarchivedChecksum() {
        return unarchivedChecksum;
    }

    public void setUnarchivedChecksum(final SimpleChecksum unarchivedChecksum) {
        this.unarchivedChecksum = unarchivedChecksum;
    }

    public SimpleChecksum getArchivedChecksum() {
        return archivedChecksum;
    }

    public void setArchivedChecksum(final SimpleChecksum archivedChecksum) {
        this.archivedChecksum = archivedChecksum;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(final Encoding encoding) {
        this.encoding = encoding;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(final long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(final long length) {
        this.length = length;
    }
}
