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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
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
	private Map cacheFileNames;
	private Map cacheFileTimes;
	private long lastCacheCleanup;
	private int cacheDirSize;
	
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
	public static void disableCache(String cacheId) throws TeamException {
		RemoteContentsCache cache = getCache(cacheId);
		if (cache == null) {
			// There is no cahce to dispose of
			return;
		}
		cache.deleteCacheDirectory();
		caches.remove(cacheId);
	}
	
	/**
	 * Return the <code>java.io.File</code> that contains the same contents as the remote resource 
	 * identified by the <local name, qualified name> pair. It is up to the owner of the cache to use
	 * an appropriate qualified name that uniquely identified remote versions of a file.
	 * 
	 * @param id
	 * @return
	 * @throws TeamException if the cache is not enabled
	 */
	public static synchronized File getFile(QualifiedName id) throws TeamException {
		RemoteContentsCache cache = getCache(id.getLocalName());
		if (cache == null) {
			throw new TeamException("The cache for " + id.getLocalName() + " is not enabled.");
		}
		return cache.getFile(id.getQualifier());
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
	 * Return the <code>java.io.File</code> that contains the same contents as the remote resource 
	 * identified by the given unique id. It is up to the owner of the cache to use
	 * an appropriate id that uniquely identified remote versions of a file.
	 * @param id
	 * @return
	 */
	public synchronized File getFile(String id) {
		if (cacheFileNames == null) {
			// This probably means that the cache has been disposed
			throw new IllegalStateException("The cache for " + name + "is disposed.");
		}
		String physicalPath;
		if (cacheFileNames.containsKey(id)) {
			// cache hit
			physicalPath = (String)cacheFileNames.get(id);
			registerHit(id);
		} else {
			// cache miss
			physicalPath = String.valueOf(cacheDirSize++);
			cacheFileNames.put(id, physicalPath);
			registerHit(id);
			clearOldCacheEntries();
		}
		return getCacheFileForPhysicalPath(physicalPath);
	}
	
	/**
	 * Return the InputStream that contains the cached contents for the given id. If an error occurs
	 * reading the cached contents then the cache entry is automatically removed to allow the contents
	 * to be refetched.
	 * 
	 * @param id
	 * @return an InputStream containing the cached contents or null if no contents are cached
	 * @throws TeamException
	 */
	public synchronized InputStream getContents(String id) throws TeamException {
		if (!cacheFileNames.containsKey(id)) {
			// The contents are not cached
			return null;
		}
		File ioFile = getFile(id);
		try {
			try {
				if (ioFile.exists()) {
					return new FileInputStream(ioFile);
				}
			} catch (IOException e) {
				// Try to purge the cache and continue
				purgeCacheFile(id);
				throw e;
			}
		} catch (IOException e) {
			// We will end up here if we couldn't read or delete the cache file
			throw new TeamException("An error occured accessing cache file " + ioFile.getAbsolutePath(), e);
		}
		return null;
	}
	
	/**
	 * Set the contents for the cache entry at the given id to the contents provided in the given input stream. Upon
	 * completion of this method the input stream will be closed even if an error occurred. If an error did occur, the
	 * cache entry for the given id will be cleared.
	 * 
	 * @param id
	 * @param stream
	 * @param monitor
	 * @throws TeamException if an error occured opening or writing to the cache file
	 */
	public synchronized void setContents(String id, InputStream stream, IProgressMonitor monitor) throws TeamException {
		File ioFile = getFile(id);
		try {
			
			// Open the cache file for writing
			OutputStream out;
			try {
				out = new BufferedOutputStream(new FileOutputStream(ioFile));
			} catch (FileNotFoundException e) {
				throw new TeamException("An error occurred opening cache file " + ioFile.getAbsolutePath() + " for writing.", e);
			}
	
			// Transfer the contents
			try {
				try {
					byte[] buffer = new byte[1024];
					int read;
					while ((read = stream.read(buffer)) >= 0) {
						Policy.checkCanceled(monitor);
						out.write(buffer, 0, read);
					}
				} finally {
					out.close();
				}
			} catch (IOException e) {
				// Make sure we don't leave the cache file around as it may not have the right contents
				purgeCacheFile(id);
				throw e;
			}
		} catch (IOException e) {
			throw new TeamException("An error occurred  writing to cache file " + ioFile.getAbsolutePath() + ".", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e1) {
				// Ignore close errors
			}
		}
	}
	
	/**
	 * Purge the cache entry for the given id.
	 * @param id
	 */
	public synchronized void purge(String id) {
		purgeCacheFile(id);
	}
	
	private File getCacheFileForPhysicalPath(String physicalPath) {
		return new File(getCachePath().toFile(), physicalPath);
	}
	
	private IPath getCachePath() {
		return getStateLocation().append(CACHE_DIRECTORY).append(name);
	}

	private IPath getStateLocation() {
		return TeamPlugin.getPlugin().getStateLocation();
	}

	private void registerHit(String path) {
		cacheFileTimes.put(path, Long.toString(new Date().getTime()));
	}
	
	private void clearOldCacheEntries() {
		long current = new Date().getTime();
		if ((lastCacheCleanup!=-1) && (current - lastCacheCleanup < CACHE_FILE_LIFESPAN)) return;
		for (Iterator iter = cacheFileTimes.keySet().iterator(); iter.hasNext();) {
			String f = (String) iter.next();
			long lastHit = Long.valueOf((String)cacheFileTimes.get(f)).longValue();
			if ((current - lastHit) > CACHE_FILE_LIFESPAN) purgeCacheFile(f);
		}
	}
	
	private void purgeCacheFile(String path) {
		File f = getCacheFileForPhysicalPath((String)cacheFileNames.get(path));
		try {
			deleteFile(f);
		} catch (TeamException e) {
			// log the falied delete and continue
			TeamPlugin.log(e);
		}
		cacheFileTimes.remove(path);
		cacheFileNames.remove(path);
	}
	
	private void createCacheDirectory() throws TeamException {
		IPath cacheLocation = getCachePath();
		File file = cacheLocation.toFile();
		if (file.exists()) {
			deleteFile(file);
		}
		if (! file.mkdirs()) {
			throw new TeamException("Could not create cache directory " + file.getAbsolutePath());
		}
		cacheFileNames = new HashMap();
		cacheFileTimes = new HashMap();
		lastCacheCleanup = -1;
		cacheDirSize = 0;
	}
			
	private void deleteCacheDirectory() throws TeamException {
		IPath cacheLocation = getCachePath();
		File file = cacheLocation.toFile();
		if (file.exists()) {
			deleteFile(file);
		}
		cacheFileNames = cacheFileTimes = null;
		lastCacheCleanup = -1;
		cacheDirSize = 0;
	}
	
	private void deleteFile(File file) throws TeamException {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (int i = 0; i < children.length; i++) {
				deleteFile(children[i]);
			}
		}
		if (! file.delete()) {
			throw new TeamException("Could not delete file " + file.getAbsolutePath(), null);
		}
	}
}
