package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

class IndexNodeFactory implements IObjectFactory {

	public StoredObject create(Field f) throws ObjectStoreException {
		IndexNode object = new IndexNode(f);
		return object;
	}
}
