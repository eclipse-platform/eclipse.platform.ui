package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An I...Factory should implement a create(...) method with parameters appropriate
 * to the context in which it is used.
 */

public interface IObjectFactory {

	/**
	 * Creates a new object from the given field.  The field includes the type.
	 */
	public StoredObject create(Field contents) throws ObjectStoreException;
}
