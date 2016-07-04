package com.sprylab.xar.writer;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;

public class XarBaseSource implements XarSource {

	private String name;
	private long length;
	private long size;
	private String extractedChecksum;
	private String archivedChecksum;
	private ChecksumAlgorithm checksumStyle;
	private Encoding encoding;
	private XarContentProvider provider;

	/**
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(final long length) {
		this.length = length;
	}

	/**
	 * @return the checksumStyle
	 */
	public ChecksumAlgorithm getChecksumStyle() {
		return checksumStyle;
	}

	/**
	 * @param checksumStyle
	 *            the checksumStyle to set
	 */
	public void setChecksumStyle(final ChecksumAlgorithm checksumStyle) {
		this.checksumStyle = checksumStyle;
	}

	/**
	 * @return the encoding
	 */
	public Encoding getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(final Encoding encoding) {
		this.encoding = encoding;
	}

	/**
	 * @return the provider
	 */
	public XarContentProvider getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(final XarContentProvider provider) {
		this.provider = provider;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(final long size) {
		this.size = size;
	}

	/**
	 * @return the extractedChecksum
	 */
	public String getExtractedChecksum() {
		return extractedChecksum;
	}

	/**
	 * @param extractedChecksum the extractedChecksum to set
	 */
	public void setExtractedChecksum(final String extractedChecksum) {
		this.extractedChecksum = extractedChecksum;
	}

	/**
	 * @return the archivedChecksum
	 */
	public String getArchivedChecksum() {
		return archivedChecksum;
	}

	/**
	 * @param archivedChecksum the archivedChecksum to set
	 */
	public void setArchivedChecksum(final String archivedChecksum) {
		this.archivedChecksum = archivedChecksum;
	}

}
