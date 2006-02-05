/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core;

/**
 * A cache that is associated with a synchronization that allows clients
 * to cache synchronization state related to their model for the duration of the
 * operation. When the context is disposed, the cache will be disposed and any
 * listeners notified.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface ICache {

	/**
	 * Cache the given object with this context.
	 * @param name the name that uniquely identifies the object
	 * @param value the value to be cached.
	 */
	void put(String name, Object value);
	
	/**
	 * Retrieve an object that has been cached with the context
	 * @param name the name of the object
	 * @return the object associated with the name or <code>null</code>
	 */
	Object get(String name);
	
	/**
	 * Remove the named object from the cache
	 * @param name the name
	 */
	void remove(String name);
	
	/**
	 * Add a listener to the cache that will receive notification
	 * when the cache is disposed. Adding a listener that has already
	 * been added has no effect.
	 * @param listener the listener to add
	 */
	void addCacheListener(ICacheListener listener);
	
	/**
	 * Remove the listener. Removing a listener that is not registered
	 * has no effect.
	 * @param listener the listener to remove
	 */
	void removeDisposeListener(ICacheListener listener);
	
}
