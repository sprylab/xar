package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root
public class SimpleChecksum {

	@Attribute
	private ChecksumAlgorithm style;

	@Text
	private String value;

	public ChecksumAlgorithm getStyle() {
		return style;
	}

	public String getValue() {
		return value;
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(final ChecksumAlgorithm style) {
		this.style = style;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}
}
