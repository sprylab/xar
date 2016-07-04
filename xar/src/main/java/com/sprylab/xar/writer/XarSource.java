package com.sprylab.xar.writer;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;

public interface XarSource {

	String getName();
	
	long getLength();

	long getSize();

	String getExtractedChecksum();

	String getArchivedChecksum();

	ChecksumAlgorithm getChecksumStyle();

	Encoding getEncoding();

	XarContentProvider getProvider();
}
