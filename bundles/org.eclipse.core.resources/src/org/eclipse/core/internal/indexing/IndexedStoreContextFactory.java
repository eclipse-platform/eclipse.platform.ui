package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

class IndexedStoreContextFactory implements IObjectFactory {

	public StoredObject create(Field contents) throws ObjectStoreException {
		IndexedStoreContext object = new IndexedStoreContext(contents);
		return object;
	}
}
