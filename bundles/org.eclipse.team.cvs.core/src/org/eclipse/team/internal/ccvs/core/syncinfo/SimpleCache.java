package org.eclipse.team.internal.ccvs.core.syncinfo;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of a cache. If a id is not in the cache it can be loaded via the loaded 
 * registered with the cache instance.
 */
public class SimpleCache implements ICache {

	private Map cache = new HashMap();	
	private ICacheLoader loader;	
	private boolean cleanUpCache = false;
	
	/*
	 * @see ICache#get(Object, Object)
	 */
	public CacheData get(Object id, Object args) {
		CacheData data = (CacheData)cache.get(id);
		if(data==null && loader!=null) {
			data = loader.load(id, this);
			if(data!=null) {
				put(data);
			}
		}			
		return data;
	}

	/*
	 * @see ICache#put(Object, Object)
	 */
	public void put(CacheData data) {
		cache.put(data.getId(), data);
	}

	/*
	 * @see ICache#remove(Object)
	 */
	public void remove(Object id) {
		cache.remove(id);
	}

	/*
	 * @see ICache#isEmpty()
	 */
	public boolean isEmpty() {
		return cache.isEmpty();
	}

	/*
	 * @see ICache#registerLoader(ICacheLoader)
	 */
	public void registerLoader(ICacheLoader loader) {
		this.loader = loader;
	}

	/*
	 * @see ICache#setAutoInvalidate(boolean)
	 */
	public void setAutoInvalidate(boolean enable) {
		cleanUpCache = true;
	}
	
	/*
	 * @see ICache#clear()
	 */
	public void clear() {
		cache.clear();
	}
}