package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

abstract class ObjectStorePage extends Page {

	boolean isObjectPage() {
		return (!isSpaceMapPage());
	}
	boolean isSpaceMapPage() {
		return (pageNumber % Page.Size == 0);
	}
}
