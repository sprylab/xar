package com.sprylab.xar.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Encoding;

public class XarFileSource implements XarSource {
	
	private File file;
	private final File originalFile;
	private final boolean compress;
	private ChecksumAlgorithm checksumStyle = ChecksumAlgorithm.NONE;
	private String extractedChecksum;
	private final String archivedChecksum;
	
	public XarFileSource(final File file) throws IOException {
		this(file, false);
	}
	
	public XarFileSource(final File file, final boolean compress) throws IOException {
		this(file, compress, ChecksumAlgorithm.NONE);
	}
	
	public XarFileSource(final File file, final boolean compress, final ChecksumAlgorithm checksumStyle) throws IOException {
		this.file = file;
		this.originalFile = file;
		this.checksumStyle = checksumStyle;
		this.compress = compress;
		if (compress) {
			this.extractedChecksum = computeChecksum(file);
			final File zipFile = File.createTempFile("xar-", ".gz");
			final FileOutputStream fos = new FileOutputStream(zipFile);
			final DeflaterOutputStream output = new DeflaterOutputStream(fos);
			final FileInputStream input = new FileInputStream(file);
			IOUtils.copy(input, output);
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(fos);
			this.file = zipFile;
		}
		this.archivedChecksum = computeChecksum(file);
		if (!compress) {
			this.extractedChecksum = archivedChecksum;
		}
	}

	private String computeChecksum(final File targetFile) throws IOException {
		final FileInputStream targetFileInpuStream = FileUtils.openInputStream(targetFile);
		switch (checksumStyle) {
			case SHA1:
				return DigestUtils.sha1Hex(targetFileInpuStream);
			case MD5:
				return DigestUtils.md5Hex(targetFileInpuStream);
			case NONE:
			default:
				return null;
		}
	}

	@Override
	public long getLength() {
		return file.length();
	}

	@Override
	public ChecksumAlgorithm getChecksumStyle() {
		return checksumStyle;
	}

	@Override
	public Encoding getEncoding() {
		return compress ? Encoding.GZIP : Encoding.NONE;
	}

	@Override
	public XarContentProvider getProvider() {
		return new XarContentProvider() {
			
			@Override
			public InputStream open() throws IOException {
				return new FileInputStream(file);
			}
			
			@Override
			public void completed() {
				if (compress) {
					file.delete();
				}
			}
		};
	}

	@Override
	public String getName() {
		return originalFile.getName();
	}

	@Override
	public long getSize() {
		return originalFile.length();
	}

	@Override
	public String getExtractedChecksum() {
		return extractedChecksum;
	}

	@Override
	public String getArchivedChecksum() {
		return archivedChecksum;
	}
}
