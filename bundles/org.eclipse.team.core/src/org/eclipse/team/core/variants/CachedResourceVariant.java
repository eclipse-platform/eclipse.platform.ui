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
package org.eclipse.team.core.variants;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.ResourceVariantCache;
import org.eclipse.team.internal.core.ResourceVariantCacheEntry;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * A resource variant is a partial implementation of a remote resource
 * whose contents and handle are cached locally. It is assumed that a
 * resource varant is an immutable version or revision of a resource.
 * Therefore, once the contents are cached they cannot be replaced.
 * However, the cached handle can be replaced to allow clients to
 * cache addition state or properties for a resource variant.
 * <p>
 * Overriding subclasses need to provide a cache Id for al there resource variants
 * and a cache path for each resource variant that uniquely identifies it. In addition,
 * they must implement <code>fetchContents</code> to retrieve the contents of the
 * resource variant and then call <code>setContents</code> to place these contents in the cache.
 * Subclasses may also call <code>cacheHandle</code> in order to place the handle in the
 * cache so that it can be retrieved later by calling <code>getCachedHandle</code> on any
 * resource variant whose cache path is the same as the cached handle. This allows subclasses to
 * cache additional resource variant properties such as author, comment, etc.
 * </p>
 * <p>
 * The cache in which the resource variants reside will occasionally clear
 * cached entries if they have not been accessed for a certain amount of time.
 * </p>
 */
public abstract class CachedResourceVariant extends PlatformObject implements IResourceVariant {
	
	// holds the storage instance for this resource variant
	private IStorage storage;
	
	/*
	 * Internal class which provides access to the cached contents
	 * of this resource variant
	 */
	class ResourceVariantStorage implements IEncodedStorage {
		public InputStream getContents() throws CoreException {
			if (!isContentsCached()) {
				// The cache may have been cleared if someone held
				// on to the storage too long
				throw new TeamException(Policy.bind("CachedResourceVariant.0", getCachePath())); //$NON-NLS-1$
			}
			return getCachedContents();
		}
		public IPath getFullPath() {
			return getFullPath();
		}
		public String getName() {
			return CachedResourceVariant.this.getName();
		}
		public boolean isReadOnly() {
			return true;
		}
		public Object getAdapter(Class adapter) {
			return CachedResourceVariant.this.getAdapter(adapter);
		}
		public String getCharset() throws CoreException {
			IContentDescription description = getContentDescription();
			return (description == null || description.getProperty(IContentDescription.CHARSET) == null) ? null : (String) description.getProperty(IContentDescription.CHARSET);
		}
		public IContentDescription getContentDescription() throws CoreException {
			// tries to obtain a description for this file contents
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			InputStream contents = null;
			try {
				contents = getContents();
				return contentTypeManager.getDescriptionFor(contents, getName(), IContentDescription.ALL);
			} catch (IOException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.FAILED_DESCRIBING_CONTENTS, "As error occurred computing the content type of resource variant {0}" + getFullPath(), e));
			} finally {
				if (contents != null)
					try {
						contents.close();
					} catch (IOException e) {
						// Ignore exceptions on close
					}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.IRemoteResource#getStorage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
		if (isContainer()) return null;
		ensureContentsCached(monitor);
		if (storage == null) {
			storage = new ResourceVariantStorage();
		}
		return storage;
	}
	
	private void ensureContentsCached(IProgressMonitor monitor) throws TeamException {
		// Ensure that the contents are cached from the server
		if (!isContentsCached()) {
			fetchContents(monitor);
		}
	}
	
	/**
	 * Method that is invoked when the contents of the resource variant need to 
	 * be fetched. This method will only be invoked for files (i.e.
	 * <code>isContainer()</code> returns <code>false</code>.
	 * Subclasses should override this method and invoke <code>setContents</code>
	 * with a stream containing the fetched contents.
	 * @param monitor a progress monitor
	 */
	protected abstract void fetchContents(IProgressMonitor monitor) throws TeamException;

	/**
	 * This method should be invoked by subclasses from within their <code>fetchContents</code>
	 * method in order to cache the contents for this resource variant.
	 * @param stream the stream containing the contents of the resource variant
	 * @param monitor a progress monitor
	 * @throws TeamException
	 */
	protected void setContents(InputStream stream, IProgressMonitor monitor) throws TeamException {
		// Ensure that there is a cache entry to receive the contents
		Assert.isTrue(!isContainer());
		if (!isHandleCached()) cacheHandle();
		getCacheEntry().setContents(stream, monitor);
	}
	
	private ResourceVariantCacheEntry getCacheEntry() {
		return getCache().getCacheEntry(this.getCachePath());
	}
	
	/**
	 * Return whether there are already contents cached for this resource variant.
	 * This method will return <code>false</code> even if the contents are currently
	 * being cached by another thread. The consequence of this is that the contents
	 * may be fetched twice in the rare case where two threads request the same contents
	 * at the same time. For containers, this method will always return <code>false</code>.
	 */
	protected boolean isContentsCached() {
		if (isContainer() || !isHandleCached()) {
			return false;
		}
		ResourceVariantCacheEntry entry = getCache().getCacheEntry(getCachePath());
		return entry.getState() == ResourceVariantCacheEntry.READY;
	}
	
	/**
	 * Return the cached contents for this resource variant or <code>null</code>
	 * if the contents have not been cached.
	 * For containers, this method will always return <code>null</code>.
	 * @return the cached contents or <code>null</code>
	 * @throws TeamException
	 */
	protected InputStream getCachedContents() throws TeamException {
		if (isContainer() || !isContentsCached()) return null;
		return getCache().getCacheEntry(getCachePath()).getContents();
	}
	
	/**
	 * Return <code>true</code> if the cache contains an entry for this resource
	 * variant. It is possible that another instance of this variant is cached.
	 * To get the cached instance, call <code>getCachedHandle()</code>. Note that 
	 * cached contents can be retrieved from any handle to a resource variant whose
	 * cache path (as returned by <code>getCachePath()</code>) match but other
	 * state information may only be accessible from the cached copy.
	 * @return whether the variant is cached
	 */
	protected boolean isHandleCached() {
		return (getCache().hasEntry(getCachePath()));
	}

	/**
	 * Get the path that uniquely identifies the remote resource
	 * variant. This path descibes the remote location where
	 * the remote resource is stored and also uniquely identifies
	 * each resource variant. It is used to uniquely identify this
	 * resource variant when it is stored in the resource variant cache.
	 * @return the full path of the remote resource variant
	 */
	protected abstract String getCachePath();
	
	/**
	 * Return the size (in bytes) of the contents of this resource variant.
	 * The method will return 0 if the contents have not yet been cached
	 * locally.
	 * For containers, this method will always return 0.
	 */
	public long getSize() {
		if (isContainer() || !isContentsCached()) return 0;
		ResourceVariantCacheEntry entry = getCacheEntry();
		if (entry == null || entry.getState() != ResourceVariantCacheEntry.READY) {
			return 0;
		}
		return entry.getSize();
	}
	
	/*
	 * Return the cache that is used to cache this resource variant and its contents.
	 * @return Returns the cache.
	 */
	private ResourceVariantCache getCache() {
		ResourceVariantCache.enableCaching(getCacheId());
		return ResourceVariantCache.getCache(getCacheId());
	}
	
	/**
	 * Return the ID that uniquely identifies the cache in which this resource variant
	 * is to be cache. The ID of the plugin that provides the resource variant subclass
	 * is a good candidate for this ID. The creation, management and disposal of the cache
	 * is managed by Team.
	 * @return the cache ID
	 */
	protected abstract String getCacheId();

	/**
	 * Return the cached handle for this resource variant if there is
	 * one. If there isn't one, then <code>null</code> is returned.
	 * If there is no cached handle and one is desired, then <code>cacheHandle()</code>
	 * should be called.
	 * @return a cached copy of this resource variant or <code>null</code>
	 */
	protected CachedResourceVariant getCachedHandle() {
		ResourceVariantCacheEntry entry = getCacheEntry();
		if (entry == null) return null;
		return entry.getResourceVariant();
	}
	
	/**
	 * Cache this handle in the cache, replacing any previously cached handle.
	 * Note that caching this handle will replace any state associated with a 
	 * previously cached handle, if there is one, but the contents will remain.
	 * The reason for this is the assumption that the cache path for a resource
	 * variant (as returned by <code>getCachePath()</code> identifies an immutable
	 * resource version (or revision). The ability to replace the handle itself
	 * is provided so that additional state may be cached before or after the contents
	 * are fetched.
	 */
	protected void cacheHandle() {
		getCache().add(getCachePath(), this);
	}
	
}
