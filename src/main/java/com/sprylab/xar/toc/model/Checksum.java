package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Checksum {

	@Attribute
	private ChecksumAlgorithm style;

	@Element
	private long size;

	@Element
	private long offset;

	public ChecksumAlgorithm getStyle() {
		return style;
	}

	public long getSize() {
		return size;
	}

	public long getOffset() {
		return offset;
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(final ChecksumAlgorithm style) {
		this.style = style;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(final long size) {
		this.size = size;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(final long offset) {
		this.offset = offset;
	}
}
