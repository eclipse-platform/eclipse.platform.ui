package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public abstract class PageStorePolicy {
	
	/**
	 * Returns a page given a page number, a buffer, and the page store.  The
	 * buffer should be copied in the implementations as it may be reused later.
	 * The buffer is extended or truncated to the page size during this copy.
	 * Both the buffer contents and the page number can be used to determine
	 * the type of page to create.
	 */
	public abstract Page createPage(int pageNumber, byte[] buffer, PageStore pageStore);

	/** 
	 * Returns the size of a page for a particular page store.
	 */
	public abstract int pageSize();

	/**
	 * Returns the size of a single metadata area.
	 */
	public final int sizeOfMetadataArea() {
		return 64;
	}

	/**
	 * Returns the number of metadata areas 
	 */
	public final int numberOfMetadataAreas() {
		return 16;
	}
	
	


}
