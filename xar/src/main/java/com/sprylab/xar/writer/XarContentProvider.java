package com.sprylab.xar.writer;

import java.io.IOException;
import java.io.InputStream;

public interface XarContentProvider {

	InputStream open() throws IOException; 
	
	void completed();
}
