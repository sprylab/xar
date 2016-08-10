package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root
public class SimpleChecksum {

    @Attribute
    private ChecksumAlgorithm style;

    @Text
    private String value;

    public ChecksumAlgorithm getStyle() {
        return style;
    }

    public void setStyle(final ChecksumAlgorithm style) {
        this.style = style;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
