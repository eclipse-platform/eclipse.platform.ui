package org.eclipse.core.tests.internal.indexing;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.indexing.*;

class TestPage extends Page {

	/**
	 * Creates a new page.
	 */
	protected TestPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		super(pageNumber, pageStore);
		pageBuffer = new Buffer(Page.SIZE);
		pageBuffer.copyFrom(buffer);
	}

	public boolean check(byte b) {
		for (int i = 0; i < pageBuffer.length(); i++) {
			if (pageBuffer.getByte(i) == b) continue;
			return false;
		}			
		return true;
	}

	public void fill(byte b) {
		for (int i = 0; i < pageBuffer.length(); i++) {
			pageBuffer.put(i, b);
		}
		setChanged();
		notifyObservers();
	}

	public byte value() {
		return pageBuffer.getByte(0);
	}
	
	public void toBuffer(byte[] buffer) {
		pageBuffer.copyTo(buffer);
	}
}
