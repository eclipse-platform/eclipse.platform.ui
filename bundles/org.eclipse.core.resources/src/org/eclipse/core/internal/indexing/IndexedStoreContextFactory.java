package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class IndexedStoreContextFactory implements IObjectFactory {

	public StoredObject create(Field contents) throws ObjectStoreException {
		IndexedStoreContext object = new IndexedStoreContext(contents);
		return object;
	}
}
