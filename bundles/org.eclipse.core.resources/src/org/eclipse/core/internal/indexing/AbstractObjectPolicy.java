/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.indexing;

public abstract class AbstractObjectPolicy {

	/**
	 * Creates a new instance of an object for this object store.  Uses
	 * the contents of the field to decide what type of object to create.
	 */
	public abstract StoredObject createObject(Field field, ObjectStore store, ObjectAddress address) throws ObjectStoreException ;

}
