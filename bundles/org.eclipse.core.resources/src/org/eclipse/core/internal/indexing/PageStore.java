package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

public class PageStore {
	private String name;
	private String mode;
	private RandomAccessFile file;
	private int numberOfPages;
	private int numberOfFileReads;
	private int numberOfFileWrites;
	private int numberOfReads;
	private int numberOfWrites;
	private int numberOfCacheHits;
	private PageCache readCache = null;
	private Map modifiedPages;
	private Map acquiredPages;
	private static final int NumberOfMetadataAreas = 16;  // NEVER change this
	private static final int SizeOfMetadataArea = 64; // NEVER change this
	private static final int CurrentPageStoreVersion = 1; // version 1
	private static final int StoreOffset = NumberOfMetadataAreas * SizeOfMetadataArea;
	
/** 
 * Acquires a new empty page.
 */
public Page acquire(IPageFactory pageFactory) throws PageStoreException {
	return acquire(pageFactory, numberOfPages);
}
/**
 * Returns the page that has the given page number from the page file.
 */
public Page acquire(IPageFactory pageFactory, int pageNumber) throws PageStoreException {
	Page p = null;
	numberOfReads++;
	Integer key = new Integer(pageNumber);
	p = (Page)acquiredPages.get(key);
	if (p != null) {
		numberOfCacheHits++;
		addReference(p);
		return p;
	}
	p = (Page)modifiedPages.get(key);
	if (p != null) {
		numberOfCacheHits++;
		addReference(p);
		return p;
	}
	p = readCache.get(pageNumber);
	if (p != null) {
		numberOfCacheHits++;
		addReference(p);
		return p;
	}
	numberOfPages = Math.max(pageNumber + 1, numberOfPages);
	p = pageFactory.create(this, pageNumber);
	getPageFromFile(pageNumber, p);
	addReference(p);
	return p;
}
/** 
 * Adds a reference to a page.
 */
private void addReference(Page page) {
	Integer key = new Integer(page.getPageNumber());
	if (!page.hasReferences()) acquiredPages.put(key, page);
	page.addReference();
}
/**
 * Closes the page file.
 */
private void basicClose() {
	try {
		file.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	file = null;
}
/**
 * Checks to see if the metadata stored in the page store matches that expected by this
 * code.  If not, a conversion is necessary.
 */
private void checkMetadata() throws PageStoreException {
	Buffer metadata = getMetadataArea(0);
	Field versionField = metadata.getField(0, 4);
	int pageStoreVersion = versionField.getInt();
	if (pageStoreVersion == 0) {
		versionField.put(CurrentPageStoreVersion);
		putMetadataArea(0, metadata);
		return;
	}
	if (pageStoreVersion == CurrentPageStoreVersion)
		return;
	convertPageStore(pageStoreVersion);
}
/**
 * Closes the page file.
 */
public void close() {
	try {
		commit();
	} catch (PageStoreException e) {
	}
	basicClose();
}
/**
 * Commits all modified pages to the file.
 */
public void commit() throws PageStoreException {
	if (modifiedPages.size() == 0) return;
	LogWriter.putModifiedPages(name, modifiedPages);
	flush();
	Log.delete(name);
}
/**
 * Converts the page store file from a previous to the current version.  
 * No conversions are yet defined.
 */
private void convertPageStore(int fromVersion) throws PageStoreException {
	throw new PageStoreException(PageStoreException.ConversionFailure);
}
/**
 * Creates the page file on the file system.
 */
public static void create(String fileName) throws PageStoreException {
	try {
		FileOutputStream out = new FileOutputStream(fileName);
		Buffer buf = new Buffer(NumberOfMetadataAreas * SizeOfMetadataArea);
		buf.writeTo(out);
		out.close();
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.CreateFailure);
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
 * Write the modified pages to the page file.
 */
private void flush() throws PageStoreException {
	if (modifiedPages.size() == 0) return;
	Iterator pageStream = modifiedPages.values().iterator();
	while (pageStream.hasNext()) {
		Page page = (Page) pageStream.next();
		putPageToFile(page);
	}
	modifiedPages.clear();
}
public Buffer getMetadataArea(int i) throws PageStoreException {
	Buffer buffer = new Buffer(SizeOfMetadataArea);
	try {
		buffer.readFrom(file, positionOfMetadataArea(i));
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.MetadataRequestFailure);
	}
	return buffer;
}
/**
 * Returns the name of the page store.
 */
public String getName() {
	return name;
}
private void getPageFromFile(int pageNumber, Page p) throws PageStoreException {
	if (pageNumber >= numberOfPagesInFile());
	try {
		p.readFrom(file, positionOfPage(pageNumber));
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.ReadFailure);
	}
	p.materialize();
	readCache.put(p);
	numberOfFileReads++;
	}
public void modified(Page page) {
	Integer key = new Integer(page.getPageNumber());
	modifiedPages.put(key, page);
}
/** 
 * Returns the number of read cache hits that have been made on the cache.
 */
public int numberOfCacheHits() {
	return numberOfCacheHits;
	}
/** 
 * Returns the number of read operations that have been done to the underlying file.
 */
public int numberOfFileReads() {
	return numberOfFileReads;
	}
/**
 * Returns the number of write operations that have been done to the underlying file.
 */
public int numberOfFileWrites() {
	return numberOfFileWrites;
	}
/**
 * Returns the number of pages known about in the PageFile.  This can be greater than
 * the number of pages actually in the underlying file in the file system if new ones
 * have been manufactured and not yet written to the underlying file.
 */
public int numberOfPages() {
	return numberOfPages;
	}
/**
 * Returns the number of pages actually in the underlying file.
 */
private int numberOfPagesInFile() throws PageStoreException {
	try {
		return (int) ((file.length() - positionOfPage(0)) / Page.Size);
		} 
	catch (IOException e) {
		throw new PageStoreException(PageStoreException.LengthFailure);
		}
	}
/** 
 * Returns the number of read operations that have been done.
 */
public int numberOfReads() {
	return numberOfReads;
	}
/**
 * Returns the number of write operations that have been done.
 */
public int numberOfWrites() {
	return numberOfWrites;
	}
/**
 * Opens the PageStore with a cache size of 40.
 */
public void open(String name) throws PageStoreException {
	open(name, 40);
}
/**
 * Opens the PageStore.  The file is created if necessary.
 * This will raise an exception if the
 * media on which the file is located is read-only 
 * or not authorized to the user.
 */
public void open(String name, int cacheSize) throws PageStoreException {
	if (!exists(name)) create(name);
	try {
		this.file = new RandomAccessFile(name, "rw");
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.OpenFailure);
	}
	this.name = name;
	checkMetadata();
	numberOfPages = numberOfPagesInFile();
	numberOfFileReads = 0;
	numberOfFileWrites = 0;
	numberOfReads = 0;
	numberOfWrites = 0;
	numberOfCacheHits = 0;
	/* apply any outstanding transaction by reading the log file and applying it */
	readCache = new PageCache(0);
	modifiedPages = LogReader.getModifiedPages(name);
	flush();
	Log.delete(name);
	/* prepare for normal operation */
	readCache = new PageCache(cacheSize);
	acquiredPages = new HashMap();
}
/**
 * Returns the file seek position for a given metadata area
 */
private long positionOfMetadataArea(int i) {
	return (long) i * SizeOfMetadataArea;
}
/**
 * Returns the file seek position for a given page number.
 */
private long positionOfPage(int pageNumber) {
	return (long) (pageNumber * Page.Size) + StoreOffset;
}
public void putMetadataArea(int i, Buffer buffer) throws PageStoreException {
	if (i < 0 || i >= NumberOfMetadataAreas)
		throw new PageStoreException(PageStoreException.MetadataRequestFailure);
	if (buffer.length() != SizeOfMetadataArea)
		throw new PageStoreException(PageStoreException.MetadataRequestFailure);
	try {
		buffer.writeTo(file, positionOfMetadataArea(i));
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.MetadataRequestFailure);
	}
	return;
}
private void putPageToFile(Page page) throws PageStoreException {
	page.dematerialize();
	try {
		page.writeTo(file, positionOfPage(page.getPageNumber()));
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.WriteFailure);
	}
	readCache.put(page);
	numberOfFileWrites++;
}
/**
 * Releases a page and updates its reference count.
 */
public void release(Page page) throws PageStoreException {
	removeReference(page);
	}
/** 
 * Removes a reference to a page.
 */
private void removeReference(Page page) {
	Integer key = new Integer(page.getPageNumber());
	page.removeReference();
	if (page.hasReferences()) return;
	acquiredPages.remove(key);
}
/**
 * Throws out the modified pages.
 */
public void rollback() {
	modifiedPages.clear();
}
/**
 * Internal test for page log consistency.  Throws an exception if
 * a problem is detected.
 */
public void testLogging1() throws PageStoreException {
	LogWriter.putModifiedPages(name, modifiedPages);
	Map testPages = LogReader.getModifiedPages(name);
	int m = testPages.size();
	int n = modifiedPages.size();
	if (m != n) {
		throw new PageStoreException("Page set sizes do not match"
			+ m + " " + n);
	}
	Iterator testPagesStream = testPages.values().iterator();
	Iterator modifiedPagesStream = modifiedPages.values().iterator();
	while (testPagesStream.hasNext()) {
		Page testPage = (Page) testPagesStream.next();
		Page modifiedPage = (Page) modifiedPagesStream.next();
		if (testPage.getPageNumber() != modifiedPage.getPageNumber()) {
			throw new PageStoreException("Page number mismatch at " 
				+ testPage.getPageNumber() + " " + modifiedPage.getPageNumber());
		}
		if (Buffer.compare(testPage, modifiedPage) != 0) {
			throw new PageStoreException("Page buffer mismatch at " 
				+ testPage.getPageNumber());
		}
	}
	Log.delete(name);
}
/**
 * Internal test for applying a page log to the file.  Does the 
 * equivalent of a flush.
 */
public void testLogging2() throws PageStoreException {
	LogWriter.putModifiedPages(name, modifiedPages);
	modifiedPages = LogReader.getModifiedPages(name);
	flush();
}
/**
 * Internal test for simulating failure after the log is written but before the
 * log is applied.  Tests the open sequence.  Does the equivalent of a close and
 * open.  Pages must have been put to the store in order for this test to make sense.
 * This should look like it does a flush, since the modified pages are written to the
 * file.
 */
public void testLogging3() throws PageStoreException {
	LogWriter.putModifiedPages(name, modifiedPages);
	basicClose();
	open(name);
}
}
