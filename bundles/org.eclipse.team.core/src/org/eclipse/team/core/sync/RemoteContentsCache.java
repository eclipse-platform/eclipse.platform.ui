/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * This class implements a caching facility that can be used by TeamProviders to cache contents
 */
public class RemoteContentsCache {
	
	// Directory to cache file contents
	private static final String CACHE_DIRECTORY = ".cache"; //$NON-NLS-1$
	// Maximum lifespan of local cache file, in milliseconds
	private static final long CACHE_FILE_LIFESPAN = 60*60*1000; // 1hr
	
	// Map of registered cahces indexed by local name of a QualifiedName
	private static Map caches = new HashMap(); // String (local name) > RemoteContentsCache
	
	private String name;
	private Map cacheEntries;
	private long lastCacheCleanup;
	private int cacheDirSize;

	// Lock used to serialize the writting of cache contents
	private ILock lock = Platform.getJobManager().newLock(); 
	
	/**
	 * Enables the use of remote contents caching for the given cacheId. The cache ID must be unique.
	 * A good candidate for this ID is the plugin ID of the plugin peforming the caching.
	 * 
	 * @param cacheId the unique Id of the cache being enabled
	 * @throws TeamException if the cache area on disk could not be properly initialized
	 */
	public static synchronized void enableCaching(String cacheId) {
		if (isCachingEnabled(cacheId)) return;
		RemoteContentsCache cache = new RemoteContentsCache(cacheId);
		try {
			cache.createCacheDirectory();
		} catch (TeamException e) {
			// Log the exception and continue
			TeamPlugin.log(e);
		}
		caches.put(cacheId, cache);
	}
	
	/**
	 * Returns whether caching has been enabled for the given Id. A cache should only be enabled once.
	 * It is conceivable that a cache be persisted over workbench invocations thus leading to a cahce that
	 * is enabled on startup without intervention by the owning plugin.
	 * 
	 * @param cacheId the unique Id of the cache
	 * @return true if caching for the given Id is enabled
	 */
	public static boolean isCachingEnabled(String cacheId) {
		return getCache(cacheId) != null;
	}
	
	/**
	 * Disable the cache, dispoing of any file contents in the cache.
	 * 
	 * @param cacheId the unique Id of the cache
	 * @throws TeamException if the cached contents could not be deleted from disk
	 */
	public static void disableCache(String cacheId) {
		RemoteContentsCache cache = getCache(cacheId);
		if (cache == null) {
			// There is no cache to dispose of
			return;
		}
		caches.remove(cacheId);
		try {
			cache.deleteCacheDirectory();
		} catch (TeamException e) {
			// Log the exception and continue
			TeamPlugin.log(e);
		}
	}
	
	/**
	 * Return the cache for the given id or null if caching is not enabled for the given id.
	 * @param cacheId
	 * @return
	 */
	public static synchronized RemoteContentsCache getCache(String cacheId) {
		return (RemoteContentsCache)caches.get(cacheId);
	}
	
	private RemoteContentsCache(String name) {
		this.name = name;
	}
	
	/**
	 * Return whether the cache contains an entry for the given id. Register a hit if it does.
	 * @param id the id of the cache entry
	 * @return true if there are contents cached for the id
	 */
	public boolean hasEntry(String id) {
		return internalGetCacheEntry(id) != null;
	}

	protected IPath getCachePath() {
		return getStateLocation().append(CACHE_DIRECTORY).append(name);
	}

	private IPath getStateLocation() {
		return TeamPlugin.getPlugin().getStateLocation();
	}
	
	private void clearOldCacheEntries() {
		long current = new Date().getTime();
		if ((lastCacheCleanup!=-1) && (current - lastCacheCleanup < CACHE_FILE_LIFESPAN)) return;
		List stale = new ArrayList();
		for (Iterator iter = cacheEntries.values().iterator(); iter.hasNext();) {
			RemoteContentsCacheEntry entry = (RemoteContentsCacheEntry) iter.next();
			long lastHit = entry.getLastAccessTimeStamp();
			if ((current - lastHit) > CACHE_FILE_LIFESPAN){
				stale.add(entry);
			}
		}
		for (Iterator iter = stale.iterator(); iter.hasNext();) {
			RemoteContentsCacheEntry entry = (RemoteContentsCacheEntry) iter.next();
			entry.dispose();
		}
	}
	
	private void purgeFromCache(String id) {
		RemoteContentsCacheEntry entry = (RemoteContentsCacheEntry)cacheEntries.get(id);
		File f = entry.getFile();
		try {
			deleteFile(f);
		} catch (TeamException e) {
			// Ignore the deletion failure.
			// A failure only really matters when purging the directory on startup
		}
		cacheEntries.remove(id);
	}
	
	private void createCacheDirectory() throws TeamException {
		IPath cacheLocation = getCachePath();
		File file = cacheLocation.toFile();
		if (file.exists()) {
			deleteFile(file);
		}
		if (! file.mkdirs()) {
			throw new TeamException(Policy.bind("RemoteContentsCache.fileError", file.getAbsolutePath())); //$NON-NLS-1$
		}
		cacheEntries = new HashMap();
		lastCacheCleanup = -1;
		cacheDirSize = 0;
	}
			
	private void deleteCacheDirectory() throws TeamException {
		cacheEntries = null;
		lastCacheCleanup = -1;
		cacheDirSize = 0;
		IPath cacheLocation = getCachePath();
		File file = cacheLocation.toFile();
		if (file.exists()) {
			try {
				deleteFile(file);
			} catch (TeamException e) {
				// Don't worry about problems deleting.
				// The only case that matters is when the cache directory is created
			}
		}
	}
	
	private void deleteFile(File file) throws TeamException {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (int i = 0; i < children.length; i++) {
				deleteFile(children[i]);
			}
		}
		if (! file.delete()) {
			throw new TeamException(Policy.bind("RemoteContentsCache.fileError", file.getAbsolutePath())); //$NON-NLS-1$
		}
	}

	/**
	 * Purge the given cache entry from the cache. This method should only be invoked from
	 * an instance of RemoteContentsCacheEntry after it has set it's state to DISPOSED.
	 * @param entry
	 */
	protected void purgeFromCache(RemoteContentsCacheEntry entry) {
		purgeFromCache(entry.getId());
	}

	private RemoteContentsCacheEntry internalGetCacheEntry(String id) {
		RemoteContentsCacheEntry entry = (RemoteContentsCacheEntry)cacheEntries.get(id);
		if (entry != null) {
			entry.registerHit();
		}
		return entry;
	}
	
	/**
	 * @param id the id that uniquely identifes the remote resource that is cached.
	 * @return
	 */
	public synchronized RemoteContentsCacheEntry getCacheEntry(String id) {
		if (cacheEntries == null) {
			// This probably means that the cache has been disposed
			throw new IllegalStateException(Policy.bind("RemoteContentsCache.cacheDisposed", name)); //$NON-NLS-1$
		}
		RemoteContentsCacheEntry entry = internalGetCacheEntry(id);
		if (entry == null) {
			// cache miss
			entry = createCacheEntry(id);
		}
		return entry;
	}
	
	private RemoteContentsCacheEntry createCacheEntry(String id) {
		clearOldCacheEntries();
		String filePath = String.valueOf(cacheDirSize++);
		RemoteContentsCacheEntry entry = new RemoteContentsCacheEntry(this, id, filePath);
		cacheEntries.put(id, entry);
		return entry;
	}

	public String getName() {
		return name;
	}

	/**
	* Provide access to the lock for the cache. This method should only be used by a cache entry.
	 * @return Returns the lock.
	 */
	protected ILock getLock() {
		return lock;
	}

}
