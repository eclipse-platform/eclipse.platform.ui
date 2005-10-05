/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

import java.io.*;
import java.util.*;

public class PageStore implements Observer {

	private static final int NumberOfMetadataAreas = 16; // NEVER change this
	private static final int SizeOfMetadataArea = 64; // NEVER change this
	private static final int CurrentPageStoreVersion = 1; // version 1
	private static final byte[] ZEROES = new byte[1024];

	private String name;
	private RandomAccessFile file;
	private int numberOfPages;
	private int numberOfFileReads;
	private int numberOfFileWrites;
	private int numberOfReads;
	private int numberOfCacheHits;
	private Map modifiedPages;
	private Map acquiredPages;
	private int storeOffset;
	private AbstractPagePolicy policy;
	private byte[] pageBuffer;
	private byte[] metadataBuffer;

	/**
	 * Creates the page file on the file system.  Creates a file of zero length.
	 */
	public static void create(String fileName) throws PageStoreException {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.close();
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.CreateFailure, e);
		}
	}

	/**
	 * Deletes the page file from the file system.
	 */
	public static void delete(String fileName) {
		new File(fileName).delete();
	}

	/** 
	 * Returns true if the file exists in the file system.
	 */
	public static boolean exists(String fileName) {
		return new File(fileName).exists();
	}

	/**
	 * Creates a new PageStore with a given policy.
	 */
	public PageStore(AbstractPagePolicy policy) {
		this.policy = policy;
		this.storeOffset = NumberOfMetadataAreas * SizeOfMetadataArea;
	}

	/** 
	 * Acquires a new empty page.
	 */
	//public Page acquire(IPageFactory pageFactory) throws PageStoreException {
	//	return acquire(pageFactory, numberOfPages);
	//}
	/**
	 * Returns the page that has the given page number from the page file.
	 */
	//public Page acquire(IPageFactory pageFactory, int pageNumber) throws PageStoreException {
	//	Page p = null;
	//	numberOfReads++;
	//	Integer key = new Integer(pageNumber);
	//	p = (Page)acquiredPages.get(key);
	//	if (p != null) {
	//		numberOfCacheHits++;
	//		addReference(p);
	//		return p;
	//	}
	//	p = (Page)modifiedPages.get(key);
	//	if (p != null) {
	//		numberOfCacheHits++;
	//		addReference(p);
	//		return p;
	//	}
	//	p = readCache.get(pageNumber);
	//	if (p != null) {
	//		numberOfCacheHits++;
	//		addReference(p);
	//		return p;
	//	}
	//	numberOfPages = Math.max(pageNumber + 1, numberOfPages);
	//	p = pageFactory.create(this, pageNumber);
	//	getPageFromFile(pageNumber, p);
	//	addReference(p);
	//	return p;
	//}
	/** 
	 * Adds a reference to a page.
	 */
	//private void addReference(Page page) {
	//	Integer key = new Integer(page.getPageNumber());
	//	if (!page.hasReferences()) acquiredPages.put(key, page);
	//	page.addReference();
	//}
	/**
	 * Opens the PageStore.  The file is created if necessary.
	 * This will raise an exception if the
	 * media on which the file is located is read-only 
	 * or not authorized to the user.
	 */
	public void open(String name) throws PageStoreException {
		this.name = name;
		pageBuffer = new byte[Page.SIZE];
		metadataBuffer = new byte[SizeOfMetadataArea];
		if (!exists(name))
			create(name);
		try {
			this.file = new RandomAccessFile(name, "rw"); //$NON-NLS-1$
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.OpenFailure, e);
		}
		checkMetadata();
		numberOfPages = numberOfPagesInFile();
		numberOfFileReads = 0;
		numberOfFileWrites = 0;
		numberOfReads = 0;
		numberOfCacheHits = 0;
		/* apply any outstanding transaction by reading the log file and applying it */
		modifiedPages = LogReader.getModifiedPages(this);
		flush();
		Log.delete(name);
		/* prepare for normal operation */
		acquiredPages = new HashMap();
	}

	/**
	 * Checks to see if the metadata stored in the page store matches that expected by this
	 * code.  If not, a conversion is necessary.
	 */
	private void checkMetadata() throws PageStoreException {
		byte[] md = readMetadataArea(0);
		Buffer metadata = new Buffer(md);
		Field versionField = metadata.getField(0, 4);
		int pageStoreVersion = versionField.getInt();
		if (pageStoreVersion == 0) {
			versionField.put(CurrentPageStoreVersion);
			writeMetadataArea(0, md);
			return;
		}
		if (pageStoreVersion == CurrentPageStoreVersion)
			return;
		convertPageStore(pageStoreVersion);
	}

	/**
	 * Converts the page store file from a previous to the current version.  
	 * No conversions are yet defined.
	 */
	private void convertPageStore(int fromVersion) throws PageStoreException {
		throw new PageStoreException(PageStoreException.ConversionFailure);
	}

	/**
	 * Commits all changes and closes the page store.
	 */
	public void close() {
		close(true);
	}

	/**
	 * Closes the page store.
	 */
	public void close(boolean commit) {
		if (commit) {
			try {
				commit();
			} catch (PageStoreException e) {
				// ignore
			}
		}
		try {
			file.close();
		} catch (IOException e) {
			// ignore
		}
		file = null;
	}

	/**
	 * Commits all modified pages to the file.
	 */
	public void commit() throws PageStoreException {
		if (modifiedPages.size() == 0)
			return;
		LogWriter.putModifiedPages(this, modifiedPages);
		flush();
		Log.delete(name);
	}

	/**
	 * Writes the modified pages to the page file.
	 */
	private void flush() throws PageStoreException {
		if (modifiedPages.size() == 0)
			return;
		Iterator pageStream = modifiedPages.values().iterator();
		while (pageStream.hasNext()) {
			Page page = (Page) pageStream.next();
			writePage(page);
		}
		modifiedPages.clear();
	}

	//public void readFrom(RandomAccessFile file, long offset) throws IOException {
	//	long n = file.length() - offset;
	//	if (n <= 0) {
	//		clear(contents, 0, contents.length);
	//		return;
	//	}
	//	file.seek(offset);
	//	int m = (int)Math.min((long)contents.length, n);
	//	file.readFully(contents, 0, m);
	//	if (m < contents.length) {
	//		clear(contents, m, contents.length - m);
	//	}
	//}
	//public void writeTo(OutputStream out) throws IOException {
	//	out.write(contents);
	//}
	//public void writeTo(OutputStream out, int offset, int length) throws IOException {
	//	out.write(contents, offset, length);
	//}
	//public void writeTo(RandomAccessFile file, long offset) throws IOException {
	//	long p = file.length();
	//	long n = offset - p;
	//	while (n > 0) {
	//		int m = (int)Math.min((long)ZEROES.length, n);
	//		file.seek(p);
	//		file.write(ZEROES, 0, m);
	//		p += m;
	//		n -= m;
	//	}
	//	file.seek(offset);
	//	file.write(contents);
	//}

	/**
	 * Opens the PageStore with a cache size of 40.
	 */
	//public void open(String name) throws PageStoreException {
	//	open(name, 40);
	//}
	/**
	 * Opens the PageStore.  The file is created if necessary.
	 * This will raise an exception if the
	 * media on which the file is located is read-only 
	 * or not authorized to the user.
	 */
	//public void open(String name, int cacheSize) throws PageStoreException {
	//	if (!exists(name)) create(name);
	//	try {
	//		this.file = new RandomAccessFile(name, "rw");
	//	} catch (IOException e) {
	//		throw new PageStoreException(PageStoreException.OpenFailure);
	//	}
	//	this.name = name;
	//	checkMetadata();
	//	numberOfPages = numberOfPagesInFile();
	//	numberOfFileReads = 0;
	//	numberOfFileWrites = 0;
	//	numberOfReads = 0;
	//	numberOfWrites = 0;
	//	numberOfCacheHits = 0;
	//	/* apply any outstanding transaction by reading the log file and applying it */
	//	readCache = new PageCache(0);
	//	modifiedPages = LogReader.getModifiedPages(name);
	//	flush();
	//	Log.delete(name);
	//	/* prepare for normal operation */
	//	readCache = new PageCache(cacheSize);
	//	acquiredPages = new HashMap();
	//}
	/**
	 * Acquires the page that has the given page number from the page store.
	 */
	public Page acquire(int pageNumber) throws PageStoreException {
		numberOfReads++;
		Integer key = new Integer(pageNumber);
		Page page = (Page) acquiredPages.get(key);
		if (page == null) {
			page = (Page) modifiedPages.get(key);
			if (page == null) {
				numberOfPages = Math.max(pageNumber + 1, numberOfPages);
				page = readPage(pageNumber);
			} else {
				numberOfCacheHits++;
			}
			acquiredPages.put(key, page);
			page.addObserver(this);
		} else {
			numberOfCacheHits++;
		}
		page.addReference();
		return page;
	}

	/**
	 * Releases a page and decrements its reference count.
	 */
	public void release(Page page) {
		Integer key = new Integer(page.getPageNumber());
		page.removeReference();
		if (page.hasReferences())
			return;
		page.deleteObserver(this);
		acquiredPages.remove(key);
	}

	/**
	 * Processes a page update.
	 */
	public void update(Observable object, Object arg) {
		Page page = (Page) object;
		Integer key = new Integer(page.getPageNumber());
		modifiedPages.put(key, page);
	}

	/**
	 * Returns the file seek offset for a given page number.
	 */
	protected long offsetOfPage(int pageNumber) {
		return (long) (pageNumber * Page.SIZE) + storeOffset;
	}

	protected Page readPage(int pageNumber) throws PageStoreException {
		if (!readBuffer(offsetOfPage(pageNumber), pageBuffer)) {
			throw new PageStoreException(PageStoreException.ReadFailure);
		}
		numberOfFileReads++;
		Page p = policy.createPage(pageNumber, pageBuffer, this);
		p.addObserver(this);
		return p;
	}

	protected void writePage(Page page) throws PageStoreException {
		page.toBuffer(pageBuffer);
		long fileOffset = offsetOfPage(page.getPageNumber());
		if (!writeBuffer(fileOffset, pageBuffer, 0, pageBuffer.length)) {
			throw new PageStoreException(PageStoreException.WriteFailure);
		}
		numberOfFileWrites++;
	}

	/**
	 * Returns the file seek offset for a given metadata area
	 */
	protected long offsetOfMetadataArea(int i) {
		return (long) i * SizeOfMetadataArea;
	}

	public byte[] readMetadataArea(int i) throws PageStoreException {
		if (!readBuffer(offsetOfMetadataArea(i), metadataBuffer)) {
			throw new PageStoreException(PageStoreException.MetadataRequestFailure);
		}
		return new Buffer(metadataBuffer).get(0, metadataBuffer.length);
	}

	public void writeMetadataArea(int i, byte[] buffer) throws PageStoreException {
		if (i < 0 || i >= NumberOfMetadataAreas)
			throw new PageStoreException(PageStoreException.MetadataRequestFailure);
		if (buffer.length != SizeOfMetadataArea)
			throw new PageStoreException(PageStoreException.MetadataRequestFailure);
		if (!writeBuffer(offsetOfMetadataArea(i), buffer, 0, buffer.length)) {
			throw new PageStoreException(PageStoreException.MetadataRequestFailure);
		}
		return;
	}

	protected boolean readBuffer(long fileOffset, byte[] buffer) {
		new Buffer(buffer).clear();
		long fileLength = getFileLength();
		if (fileOffset >= fileLength)
			return true;
		int bytesToRead = (int) Math.min(buffer.length, (fileLength - fileOffset));
		try {
			file.seek(fileOffset);
			file.readFully(buffer, 0, bytesToRead);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	protected boolean writeBuffer(long fileOffset, byte[] buffer, int offset, int length) {
		clearFileToOffset(fileOffset);
		try {
			file.seek(fileOffset);
			file.write(buffer, offset, length);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	protected long getFileLength() {
		long n = 0;
		try {
			n = file.length();
		} catch (IOException e) {
			return 0;
		}
		return n;
	}

	protected void clearFileToOffset(long fileOffset) {
		long fileLength = getFileLength();
		while (fileLength < fileOffset) {
			int m = (int) Math.min(ZEROES.length, (fileOffset - fileLength));
			writeBuffer(fileLength, ZEROES, 0, m);
			fileLength += m;
		}
	}

	/**
	 * Returns the number of pages actually in the underlying file.
	 */
	protected int numberOfPagesInFile() {
		return (int) ((getFileLength() - offsetOfPage(0)) / Page.SIZE);
	}

	/**
	 * Returns the name of the page store.
	 */
	public String getName() {
		return name;
	}

	public AbstractPagePolicy getPolicy() {
		return policy;
	}

	/**
	 * Returns the number of pages known about in the PageFile.  This can be greater than
	 * the number of pages actually in the underlying file in the file system if new ones
	 * have been manufactured and not yet written to the underlying file.
	 */
	public int numberOfPages() {
		return numberOfPages;
	}
}
