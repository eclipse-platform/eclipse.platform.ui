package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class ObjectStorePagePolicy extends AbstractPagePolicy {

	/**
	 * @see PagePolicy#createPage(int, byte[], PageStore)
	 */
	public final Page createPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		if (pageNumber % Page.SIZE == 0) {
			return new SpaceMapPage(pageNumber, buffer, pageStore);
		} else {
			return new ObjectPage(pageNumber, buffer, pageStore);
		}
	}

}
