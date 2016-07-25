package com.sprylab.xar.writer;

public class XarSimpleDirectory implements XarDirectory {

    private String name;

    public XarSimpleDirectory(final String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

}
