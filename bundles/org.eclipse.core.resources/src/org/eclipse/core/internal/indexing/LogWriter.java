package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.io.*;
import java.util.*;

class LogWriter {

	private FileOutputStream out;

	/**
	 * Closes the log.
	 */
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
		}
		out = null;
	}
	/**
	 * Opens the log.
	 */
	void open(String storeName) throws PageStoreException {
		try {
			out = new FileOutputStream(Log.name(storeName));
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogOpenFailure);
		}
	}
	/**
	 * Puts the modified pages to the log file.
	 */
	static void putModifiedPages(String storeName, Map modifiedPages) throws PageStoreException {
		LogWriter writer = new LogWriter();
		writer.open(storeName);
		writer.putModifiedPages(modifiedPages);
		writer.close();
	}
	/**
	 * Puts the modified pages into the log.
	 */
	void putModifiedPages(Map modifiedPages) throws PageStoreException {
		Field f4 = new Field(4);
		try {
			int numberOfPages = modifiedPages.size();
			f4.put(numberOfPages).writeTo(out);
			Iterator pageStream = modifiedPages.values().iterator();
			while (pageStream.hasNext()) {
				Page page = (Page) pageStream.next();
				int pageNumber = page.getPageNumber();
				f4.put(pageNumber).writeTo(out);
				page.writeTo(out);
			}
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogWriteFailure);
		}

	}
}
