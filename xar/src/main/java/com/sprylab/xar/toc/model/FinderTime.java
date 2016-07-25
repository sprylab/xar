package com.sprylab.xar.toc.model;


import java.util.Date;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class FinderTime {

    @Element
    private Date time;

    @Element
    private long nanoseconds;

    public Date getTime() {
        return time;
    }

    /**
     * @return the nanoseconds
     */
    public long getNanoseconds() {
        return nanoseconds;
    }

    /**
     * @param nanoseconds the nanoseconds to set
     */
    public void setNanoseconds(final long nanoseconds) {
        this.nanoseconds = nanoseconds;
    }

    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }
}
