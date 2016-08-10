package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Xar {

    @Element
    private ToC toc;

    public ToC getToc() {
        return toc;
    }

    public void setToc(final ToC toc) {
        this.toc = toc;
    }

}
