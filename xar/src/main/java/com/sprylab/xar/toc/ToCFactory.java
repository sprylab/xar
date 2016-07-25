package com.sprylab.xar.toc;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;
import org.simpleframework.xml.stream.Style;
import org.simpleframework.xml.transform.RegistryMatcher;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.DateTransform;
import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.toc.model.EncodingEnumTransform;
import com.sprylab.xar.toc.model.LowerCaseEnumTransform;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.toc.model.Type;
import com.sprylab.xar.toc.model.Xar;

public class ToCFactory {

    private static Serializer SERIALIZER;

    private static Serializer getSerializer() {
        if (SERIALIZER == null) {
            final Style style = new HyphenStyle();
            final Format format = new Format(style);

            final RegistryMatcher matcher = new RegistryMatcher();
            matcher.bind(Date.class, DateTransform.class);
            matcher.bind(ChecksumAlgorithm.class, new LowerCaseEnumTransform(ChecksumAlgorithm.class));
            matcher.bind(Type.class, new LowerCaseEnumTransform(Type.class));
            matcher.bind(Encoding.class, new EncodingEnumTransform());

            SERIALIZER = new Persister(matcher, format);
        }
        return SERIALIZER;
    }


    public static ToC fromFile(final File source) throws Exception {
        final Serializer serializer = getSerializer();
        final Xar xar = serializer.read(Xar.class, source, false);
        return xar.getToc();
    }

    public static ToC fromInputStream(final InputStream source) throws Exception {
        final Serializer serializer = getSerializer();
        final Xar xar = serializer.read(Xar.class, source, false);
        return xar.getToc();
    }

    public static void copy(final InputStream source, final OutputStream target) throws Exception {
        final Serializer serializer = getSerializer();
        final Xar xar = serializer.read(Xar.class, source, false);
        serializer.write(xar, target);
    }

    public static void toOutputStream(final Xar xar, final OutputStream target) throws Exception {
        final Serializer serializer = getSerializer();
        serializer.write(xar, target);
    }
}
