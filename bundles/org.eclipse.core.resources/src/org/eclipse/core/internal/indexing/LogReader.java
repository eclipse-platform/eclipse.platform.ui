package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.io.*;
import java.util.*;

class LogReader {

	private FileInputStream in;
	
	/**
	 * Closes the log.
	 */
	void close() {
		if (in == null) return;
		try {
			in.close();
			} 
		catch (IOException e) {
			}
		in = null;
		}
	/**
	 * Returns the Hashmap of modified pages read from the log.
	 */
	Map getModifiedPages() throws PageStoreException {
		Map modifiedPages = new TreeMap();
		if (in == null) return modifiedPages;
		Field f4 = new Field(4);
		try {
			int numberOfPages = f4.readFrom(in).getInt();
			int recordSize = 4 + Page.Size;
			int bytesAvailable = in.available();
			if (in.available() != (numberOfPages * recordSize)) return modifiedPages;
			for (int i = 0; i < numberOfPages; i++) {
				int pageNumber = f4.readFrom(in).getInt();
				Page page = null;
				page = new Page();
				page.initialize(null, pageNumber);
				page.readFrom(in);
				Integer key = new Integer(pageNumber);
				modifiedPages.put(key, page);
			}
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogReadFailure);
		}
		return modifiedPages;
	}
	/** 
	 * Returns the Hashmap of the modified pages.
	 */
	static Map getModifiedPages(String storeName) throws PageStoreException {
		LogReader reader = new LogReader();
		reader.open(storeName);
		Map modifiedPages = reader.getModifiedPages();
		reader.close();
		return modifiedPages;
	}
	/** 
	 * Open a log for reading.
	 */
	void open(String storeName) throws PageStoreException {
		if (!Log.exists(storeName)) return;
		try {
			in = new FileInputStream(Log.name(storeName));
			} 
		catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogOpenFailure);
			}
		}
}
