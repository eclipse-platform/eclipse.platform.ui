package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
