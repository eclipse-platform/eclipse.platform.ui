package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public abstract class AbstractObjectPolicy {

	/**
	 * Creates a new instance of an object for this object store.  Uses
	 * the contents of the field to decide what type of object to create.
	 */
	public abstract StoredObject createObject(Field field, ObjectStore store, ObjectAddress address) throws ObjectStoreException ;

}
