package com.sprylab.xar;

import java.io.InputStream;

import com.sprylab.xar.toc.TocFactory;
import com.sprylab.xar.toc.model.ToC;

/**
 * Interface for a custom xar toc parser which can be set on a {@link XarSource}.
 */
public interface XarTocParser {

    public ToC parse(final InputStream source) throws Exception;
}
