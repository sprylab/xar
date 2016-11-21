package com.sprylab.xar.toc.model;

import java.util.Locale;

import org.simpleframework.xml.transform.Transform;

/**
 * This is a adopted {@code org.simpleframework.xml.transform.EnumTransform} class, which can read lowercase enums in
 * XML and maps them to uppercase enums in Java and vice versa.
 */
public class LowerCaseEnumTransform implements Transform<Enum> {

    private final Class type;

    public LowerCaseEnumTransform(final Class type) {
        this.type = type;
    }

    @Override
    public Enum read(final String value) throws Exception {
        return Enum.valueOf(type, value.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String write(final Enum value) throws Exception {
        return value.name().toLowerCase(Locale.ENGLISH);
    }

}
