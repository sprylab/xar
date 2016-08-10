package com.sprylab.xar.toc;

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

/**
 * Factory for easily reading and writing {@link ToC} from and to streams.
 */
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

    /**
     * Reads and parses a {@link ToC} from an {@link InputStream}.
     *
     * @param source the {@link InputStream} to read from
     * @return the deserialized {@link ToC} object
     * @throws Exception when the {@link ToC} could not be deserialized
     */
    public static ToC fromInputStream(final InputStream source) throws Exception {
        final Serializer serializer = getSerializer();
        final Xar xar = serializer.read(Xar.class, source, false);
        return xar.getToc();
    }

    /**
     * Reads and deserializes a {@link Xar} from an {@link InputStream} and serializes and writes it to an {@link OutputStream}.
     *
     * @param source the {@link InputStream} to read from
     * @param target the {@link OutputStream} to write to
     * @throws Exception when the {@link ToC} could not be deserialized or serialized
     */
    public static void copy(final InputStream source, final OutputStream target) throws Exception {
        final Serializer serializer = getSerializer();
        final Xar xar = serializer.read(Xar.class, source, false);
        serializer.write(xar, target);
    }

    /**
     * Writes a {@link ToC} to an {@link OutputStream}.
     *
     * @param toc    the {@link ToC} to serialize
     * @param target the {@link OutputStream} to write to
     * @throws Exception when the {@link ToC} could not be serialized
     */
    public static void toOutputStream(final ToC toc, final OutputStream target) throws Exception {
        final Xar xar = new Xar();
        xar.setToc(toc);

        final Serializer serializer = getSerializer();
        serializer.write(xar, target);
    }
}
