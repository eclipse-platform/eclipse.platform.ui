package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

class UserDefinedObjectFactory implements IObjectFactory {

	public StoredObject create(Field f) throws ObjectStoreException {
		UserDefinedObject object = new UserDefinedObject(f);
		return object;
	}
}
