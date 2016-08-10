package com.sprylab.xar.toc.model;

import java.util.Date;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class File {

    @Attribute
    private String id;

    @Element
    private String name;

    @Element
    private Type type;

    @Element(required = false)
    private String mode;

    @Element(required = false)
    private String uid;

    @Element(required = false)
    private String user;

    @Element(required = false)
    private String gid;

    @Element(required = false)
    private String group;

    @Element(required = false)
    private Date atime;

    @Element(required = false)
    private Date mtime;

    @Element(required = false)
    private Date ctime;

    @ElementList(inline = true, required = false, name = "file")
    private List<File> childs;

    @Element(required = false)
    private Data data;

    @ElementList(inline = true, required = false, name = "ea")
    private List<EA> eas;

    @Element(required = false)
    private String inode;

    @Element(required = false)
    private String deviceno;

    @Element(required = false)
    private FinderTime finderCreateTime;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(final String mode) {
        this.mode = mode;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(final String gid) {
        this.gid = gid;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public Date getAtime() {
        return atime;
    }

    public void setAtime(final Date atime) {
        this.atime = atime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(final Date mtime) {
        this.mtime = mtime;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(final Date ctime) {
        this.ctime = ctime;
    }

    public List<File> getChildren() {
        return childs;
    }

    public void setChildren(final List<File> childs) {
        this.childs = childs;
    }

    public Data getData() {
        return data;
    }

    public void setData(final Data data) {
        this.data = data;
    }

    public List<EA> getEas() {
        return eas;
    }

    public void setEas(final List<EA> eas) {
        this.eas = eas;
    }

    public String getInode() {
        return inode;
    }

    public void setInode(final String inode) {
        this.inode = inode;
    }

    public String getDeviceno() {
        return deviceno;
    }

    public void setDeviceno(final String deviceno) {
        this.deviceno = deviceno;
    }

    public FinderTime getFinderCreateTime() {
        return finderCreateTime;
    }

    public void setFinderCreateTime(final FinderTime finderCreateTime) {
        this.finderCreateTime = finderCreateTime;
    }
}
