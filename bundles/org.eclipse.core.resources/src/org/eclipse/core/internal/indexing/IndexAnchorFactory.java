package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

class IndexAnchorFactory implements IObjectFactory {

	public StoredObject create(Field f) throws ObjectStoreException {
		IndexAnchor object = new IndexAnchor(f);
		return object;
	}
}
