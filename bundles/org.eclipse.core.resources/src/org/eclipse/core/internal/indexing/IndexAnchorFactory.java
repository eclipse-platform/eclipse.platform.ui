package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class IndexAnchorFactory implements IObjectFactory {

	public StoredObject create(Field f) throws ObjectStoreException {
		IndexAnchor object = new IndexAnchor(f);
		return object;
	}
}
