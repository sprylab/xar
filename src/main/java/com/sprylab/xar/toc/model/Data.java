package com.sprylab.xar.toc.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root
public class Data {

	@Element(required = false)
	private SimpleChecksum extractedChecksum;

	@Element(required = false)
	private SimpleChecksum unarchivedChecksum;

	@Element
	private SimpleChecksum archivedChecksum;

	@Path("encoding")
	@Attribute(name = "style", required = false)
	private Encoding encoding = Encoding.NONE;

	@Element
	private long size;

	@Element
	private long offset;

	@Element
	private long length;

	public SimpleChecksum getExtractedChecksum() {
		return extractedChecksum;
	}

	public SimpleChecksum getUnarchivedChecksum() {
		return unarchivedChecksum;
	}

	public SimpleChecksum getArchivedChecksum() {
		return archivedChecksum;
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public long getSize() {
		return size;
	}

	public long getOffset() {
		return offset;
	}

	public long getLength() {
		return length;
	}

	/**
	 * @param extractedChecksum
	 *            the extractedChecksum to set
	 */
	public void setExtractedChecksum(final SimpleChecksum extractedChecksum) {
		this.extractedChecksum = extractedChecksum;
	}

	/**
	 * @param unarchivedChecksum
	 *            the unarchivedChecksum to set
	 */
	public void setUnarchivedChecksum(final SimpleChecksum unarchivedChecksum) {
		this.unarchivedChecksum = unarchivedChecksum;
	}

	/**
	 * @param archivedChecksum
	 *            the archivedChecksum to set
	 */
	public void setArchivedChecksum(final SimpleChecksum archivedChecksum) {
		this.archivedChecksum = archivedChecksum;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(final Encoding encoding) {
		this.encoding = encoding;
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

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(final long length) {
		this.length = length;
	}
}
