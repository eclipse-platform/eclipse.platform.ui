package org.eclipse.team.internal.ccvs.core.syncinfo;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.*;

/**
 * Interface for classes that can load an object from a store with the given id.
 */
public interface ICacheLoader {	
	/**
	 * Loads object associated with the given id from a store. The cache is passed so that if other objects
	 * are read as a side effect of fetching name, then they can automatically be added to the
	 * cache as well. 
	 */
	public CacheData load(Object id, ICache cache);
}
