package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

class LogWriter {

	protected FileOutputStream out;
	protected PageStore pageStore;

	/**
	 * Puts the modified pages to the log file.
	 */
	public static void putModifiedPages(PageStore pageStore, Map modifiedPages) throws PageStoreException {
		LogWriter writer = new LogWriter();
		writer.open(pageStore);
		writer.putModifiedPages(modifiedPages);
		writer.close();
	}

	/**
	 * Opens the log.
	 */
	protected void open(PageStore pageStore) throws PageStoreException {
		this.pageStore = pageStore;
		try {
			out = new FileOutputStream(Log.name(pageStore.getName()));
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogOpenFailure);
		}
	}

	/**
	 * Closes the log.
	 */
	protected void close() {
		try {
			out.close();
		} catch (IOException e) {
		}
		out = null;
	}

	/**
	 * Puts the modified pages into the log.
	 */
	protected void putModifiedPages(Map modifiedPages) throws PageStoreException {
		Buffer b4 = new Buffer(4);
		byte[] pageBuffer = new byte[Page.SIZE];
		int numberOfPages = modifiedPages.size();
		b4.put(0, 4, numberOfPages);
		if (!write(b4.getByteArray())) throw new PageStoreException(PageStoreException.LogWriteFailure);
		Iterator pageStream = modifiedPages.values().iterator();
		while (pageStream.hasNext()) {
			Page page = (Page) pageStream.next();
			int pageNumber = page.getPageNumber();
			b4.put(0, 4, pageNumber);
			if (!write(b4.getByteArray())) throw new PageStoreException(PageStoreException.LogWriteFailure);
			page.toBuffer(pageBuffer);
			if (!write(pageBuffer)) throw new PageStoreException(PageStoreException.LogWriteFailure);
		}
	}
	
	public boolean write(byte[] buffer) {
		try {
			out.write(buffer);
		} catch (IOException e) {
			return false;
		}
		return true;
		}


}
