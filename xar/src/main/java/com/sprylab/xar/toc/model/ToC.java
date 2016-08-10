package com.sprylab.xar.toc.model;

import java.util.Date;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class ToC {

    @Element(required = false)
    private Checksum checksum;

    @Element
    private Date creationTime;

    @ElementList(inline = true)
    private List<File> files;

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(final Checksum checksum) {
        this.checksum = checksum;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final Date creationTime) {
        this.creationTime = creationTime;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(final List<File> files) {
        this.files = files;
    }
}
