package com.sprylab.xar.writer;

public class XarSimpleDirectory implements XarDirectory {

    private String name;

    public XarSimpleDirectory(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
