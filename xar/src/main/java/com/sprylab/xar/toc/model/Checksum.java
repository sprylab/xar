package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Checksum {

    @Attribute
    private ChecksumAlgorithm style;

    @Element
    private long size;

    @Element
    private long offset;

    public ChecksumAlgorithm getStyle() {
        return style;
    }

    public void setStyle(final ChecksumAlgorithm style) {
        this.style = style;
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
}
