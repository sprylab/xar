package com.sprylab.xar.toc.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.simpleframework.xml.transform.Transform;

/**
 * Transforms {@link String}s to {@link Date}s and vice versa using the date format used in xar files.
 */
public class DateTransform implements Transform<Date> {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Date read(final String value) throws Exception {
        try {
            return format.parse(value);
        } catch (final ParseException e) {
            return new Date();
        }
    }

    @Override
    public String write(final Date value) throws Exception {
        return format.format(value);
    }
}
