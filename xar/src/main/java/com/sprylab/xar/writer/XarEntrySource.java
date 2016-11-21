package com.sprylab.xar.writer;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;

import okio.Source;

public interface XarEntrySource {

    String getName();

    long getLength();

    long getSize();

    long getLastModified();

    String getExtractedChecksum();

    String getArchivedChecksum();

    ChecksumAlgorithm getChecksumAlgorithm();

    Encoding getEncoding();

    Source getSource();
}
