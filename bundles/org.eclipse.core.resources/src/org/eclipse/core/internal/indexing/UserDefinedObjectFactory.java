package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
class UserDefinedObjectFactory implements IObjectFactory {

	public StoredObject create(Field f) throws ObjectStoreException {
		UserDefinedObject object = new UserDefinedObject(f);
		return object;
	}
}