package com.sprylab.xar.writer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.sprylab.xar.toc.ToCFactory;
import com.sprylab.xar.toc.model.Checksum;
import com.sprylab.xar.toc.model.ChecksumAlgorithm;
import com.sprylab.xar.toc.model.Data;
import com.sprylab.xar.toc.model.File;
import com.sprylab.xar.toc.model.SimpleChecksum;
import com.sprylab.xar.toc.model.ToC;
import com.sprylab.xar.toc.model.Type;
import com.sprylab.xar.toc.model.Xar;

public class XarWriter {

    private static final int MAGIC = 0x78617221;
    private static final short HEADER_SIZE = 28;
    private static final short VERSION = 1;

    private static final int CHECKSUM_LENGTH_MD5 = 16;
    private static final int CHECKSUM_LENGTH_SHA1 = 40;

    private ChecksumAlgorithm checksumAlgorithm;

    private final Xar xarRoot = new Xar();
	private final List<File> fileList = new ArrayList<File>();
	
	private final List<XarSource> sources = new ArrayList<XarSource>();
	
	private final Map<XarDirectory, File> dirMap = new HashMap<XarDirectory, File>();
	
	private long currentOffset;
	private int id = 0;
	
	public XarWriter() {
		this(ChecksumAlgorithm.SHA1);
	}

	public XarWriter(ChecksumAlgorithm checksumAlgorithm) {
		this.checksumAlgorithm = checksumAlgorithm;
		int checkSumLength = checksumAlgorithm == ChecksumAlgorithm.MD5 ? CHECKSUM_LENGTH_MD5
				: (checksumAlgorithm == ChecksumAlgorithm.SHA1 ? CHECKSUM_LENGTH_SHA1 : 0);
		final ToC toc = new ToC();
		toc.setCreationTime(new Date());;
		xarRoot.setToc(toc);
		toc.setFiles(fileList);
		final Checksum checksum = new Checksum();
		toc.setChecksum(checksum);
		checksum.setStyle(checksumAlgorithm);
		checksum.setSize(checkSumLength);
		checksum.setOffset(0);
		this.currentOffset = checkSumLength;
	}
	
	public void addSource(final XarSource source, final XarDirectory parent) {
		sources.add(source);
		final File file = new File();
		file.setType(Type.FILE);
		file.setName(source.getName());
		file.setId(String.valueOf(id++));
		final Data data = new Data();
		data.setOffset(currentOffset);
		data.setLength(source.getLength());
		data.setSize(source.getSize());
		currentOffset += source.getLength();
		
		final SimpleChecksum extractedChecksum = new SimpleChecksum();
		extractedChecksum.setStyle(source.getChecksumStyle());
		extractedChecksum.setValue(source.getExtractedChecksum() == null ? "0" : source.getExtractedChecksum());
		data.setExtractedChecksum(extractedChecksum);
		data.setUnarchivedChecksum(extractedChecksum);

		final SimpleChecksum archivedChecksum = new SimpleChecksum();
		archivedChecksum.setStyle(source.getChecksumStyle());
		archivedChecksum.setValue(source.getArchivedChecksum() == null ? "0" : source.getArchivedChecksum());
		data.setArchivedChecksum(archivedChecksum);
		
		data.setEncoding(source.getEncoding());
		file.setData(data);
		addFile(file, parent);
	}
	
	public void addDirectory(final XarDirectory dir, final XarDirectory parent) {
		final File file = new File();
		file.setType(Type.DIRECTORY);
		file.setName(dir.getName());
		file.setId(String.valueOf(id++));
		addFile(file, parent);
		dirMap.put(dir, file);
	}
	
	private void addFile(final File file, final XarDirectory parent) {
		if (parent == null) {
			fileList.add(file);
		} else {
			final File parentFile = dirMap.get(parent);
			if (parentFile == null) {
				throw new IllegalArgumentException("parent unknown");
			}
			List<File> children = parentFile.getChildren();
			if (children == null) {
				children = new ArrayList<File>();
				parentFile.setChildren(children);
			}
			children.add(file);
		}
	}
	
	public void write(final OutputStream output) throws Exception {
		final java.io.File tocFile = java.io.File.createTempFile("xar", ".toc");
		FileOutputStream fos = new FileOutputStream(tocFile);
		ToCFactory.toOutputStream(xarRoot, fos);
		IOUtils.closeQuietly(fos);
		final java.io.File compressedTocFile = java.io.File.createTempFile("xar", ".toc.gz");
		fos = new FileOutputStream(compressedTocFile);
		final OutputStream os = new DeflaterOutputStream(fos);
		FileInputStream fis = new FileInputStream(tocFile);
		IOUtils.copy(fis, os);
		IOUtils.closeQuietly(fis);
		IOUtils.closeQuietly(os);
		IOUtils.closeQuietly(fos);

		output.write(createHeader(compressedTocFile.length(), tocFile.length()));
		fis = new FileInputStream(compressedTocFile);
		IOUtils.copy(fis, output);
		IOUtils.closeQuietly(fis);

		if (checksumAlgorithm != ChecksumAlgorithm.NONE) {
			final InputStream compressedTocFileInputStream = FileUtils.openInputStream(compressedTocFile);
			final byte[] hash;
			switch (checksumAlgorithm) {
				case MD5:
					hash = DigestUtils.md5(compressedTocFileInputStream);
					break;
				default:
				case SHA1:
					hash = DigestUtils.sha1(compressedTocFileInputStream);
					break;
			}

			output.write(hash);
		}

		for (final XarSource xs : sources) {
			final XarContentProvider provider = xs.getProvider();
			final InputStream inputStream = provider.open();
			IOUtils.copy(inputStream, output);
			IOUtils.closeQuietly(inputStream);
			provider.completed();
		}
	}
	
	/**
	 * Create Header
	 * 
	 * 	    uint32_t magic;     
	 *	    uint16_t size;
	 *	    uint16_t version;
	 *	    uint64_t toc_length_compressed;
	 *	    uint64_t toc_length_uncompressed;
	 *	    uint32_t cksum_alg;
	 *
	 * @param tocLengthCompressed
	 * @param tocLengthUnCompressed
	 */
	private byte[] createHeader(final long tocLengthCompressed, final long tocLengthUnCompressed) {
		final ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
		bb.putInt(0, MAGIC);
		bb.putShort(4, HEADER_SIZE);
		bb.putShort(6, VERSION);
		bb.putLong(8, tocLengthCompressed);
		bb.putLong(16, tocLengthUnCompressed);
		bb.putInt(24, checksumAlgorithm.ordinal());
		return bb.array();
	}
}
