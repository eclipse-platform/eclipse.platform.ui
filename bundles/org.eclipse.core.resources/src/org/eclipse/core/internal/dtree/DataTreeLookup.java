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
package org.eclipse.core.internal.dtree;

import org.eclipse.core.runtime.IPath;

/**
 * The result of doing a lookup() in a data tree.  Uses an instance
 * pool that assumes no more than POOL_SIZE instance will ever be
 * needed concurrently.  Reclaims and reuses instances on an LRU basis.
 */
public class DataTreeLookup {
	public IPath key;
	public boolean isPresent;
	public Object data;
	public boolean foundInFirstDelta;
	private static final int POOL_SIZE = 100;
	/**
	 * The array of lookup instances available for use.
	 */
	private static DataTreeLookup[] instancePool;
	/**
	 * The index of the next available lookup instance.
	 */
	private static int nextFree = 0;
	static {
		instancePool = new DataTreeLookup[POOL_SIZE];
		//fill the pool with objects
		for (int i = 0; i < POOL_SIZE; i++) {
			instancePool[i] = new DataTreeLookup();
		}
	}

	/**
	 * Constructors for internal use only.  Use factory methods.
	 */
	private DataTreeLookup() {
		super();
	}

	/**
	 * Factory method for creating a new lookup object.
	 */
	public static DataTreeLookup newLookup(IPath nodeKey, boolean isPresent, Object data) {
		DataTreeLookup instance;
		synchronized (instancePool) {
			instance = instancePool[nextFree];
			nextFree = ++nextFree % POOL_SIZE;
		}
		instance.key = nodeKey;
		instance.isPresent = isPresent;
		instance.data = data;
		instance.foundInFirstDelta = false;
		return instance;
	}

	/**
	 * Factory method for creating a new lookup object.
	 */
	public static DataTreeLookup newLookup(IPath nodeKey, boolean isPresent, Object data, boolean foundInFirstDelta) {
		DataTreeLookup instance;
		synchronized (instancePool) {
			instance = instancePool[nextFree];
			nextFree = ++nextFree % POOL_SIZE;
		}
		instance.key = nodeKey;
		instance.isPresent = isPresent;
		instance.data = data;
		instance.foundInFirstDelta = foundInFirstDelta;
		return instance;
	}
}
