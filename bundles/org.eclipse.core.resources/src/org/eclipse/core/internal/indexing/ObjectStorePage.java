package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

abstract class ObjectStorePage extends Page {

	boolean isObjectPage() {
		return (!isSpaceMapPage());
	}
	boolean isSpaceMapPage() {
		return (pageNumber % Page.Size == 0);
	}
}
