package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class IndexNodeFactory implements IObjectFactory {

	public StoredObject create(Field f) throws ObjectStoreException {
		IndexNode object = new IndexNode(f);
		return object;
	}
}
