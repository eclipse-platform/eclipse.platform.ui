package org.eclipse.team.internal.ccvs.core.syncinfo;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Interface for a simple cache.
 */
public interface ICache {
	/**
	 * Returns a reference to the object associated with name. If the object name is not
	 * found in the cache, it is loaded by the loader defined for this cache. If a loader is
	 * not registered then <code>null</code> will be returned.
	 */
	public CacheData get(Object id, Object args);
	
	public void put(CacheData data);
	
	public void remove(Object id);
	
	public boolean isEmpty();
	
	public void registerLoader(ICacheLoader loader);
	
	public void clear();
}