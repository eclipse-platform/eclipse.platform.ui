package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

abstract class ObjectStorePage extends Page {
	
	public static final int SIZE = 8192;
	
	public ObjectStorePage(int pageNumber, byte[] buffer, PageStore pageStore) {
		super(pageNumber, pageStore);
		pageBuffer = new Buffer(SIZE);
		pageBuffer.copyFrom(buffer);
		materialize();
	}

	public boolean isObjectPage() {
		return false;
	}
	public boolean isSpaceMapPage() {
		return false;
	}
	
	protected abstract void materialize();
	
}
