package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public abstract class AbstractPagePolicy {
	
	/**
	 * Returns a page given a page number, a buffer, and the page store.  The
	 * buffer should be copied in the implementations as it may be reused later.
	 * The buffer is extended or truncated to the page size during this copy.
	 * Both the buffer contents and the page number can be used to determine
	 * the type of page to create.
	 */
	public abstract Page createPage(int pageNumber, byte[] buffer, PageStore pageStore);

}
