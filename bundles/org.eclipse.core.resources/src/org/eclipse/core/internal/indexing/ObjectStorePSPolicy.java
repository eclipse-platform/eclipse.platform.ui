package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class ObjectStorePSPolicy extends PageStorePolicy {

	/**
	 * @see PagePolicy#createPage(int, byte[], PageStore)
	 */
	public final Page createPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		if (pageNumber % pageSize() == 0) {
			return new SpaceMapPage(pageNumber, buffer, pageStore);
		} else {
			return new ObjectPage(pageNumber, buffer, pageStore);
		}
	}

	/**
	 * Returns the size of a page for an object store.
	 */
	public final int pageSize() {
		return ObjectStorePage.SIZE;
	}

}
