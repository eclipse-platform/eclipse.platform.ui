/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

/**
 * Keeps a cache of recently read content descriptions.
 * 
 * @since 3.0
 * @see IFile#getContentDescription()
 */
public class ContentDescriptionManager implements IManager, IRegistryChangeListener, IContentTypeManager.IContentTypeChangeListener {

	/**
	 * This job causes the content description cache and the related flags 
	 * in the resource tree to be flushed. 
	 */
	private class FlushJob extends WorkspaceJob {

		public FlushJob() {
			super(Messages.resources_flushingContentDescriptionCache);
			setSystem(true);
			setUser(false);
			setPriority(LONG);
			setRule(workspace.getRoot());
		}

		/* (non-Javadoc)
		 * See Job#belongsTo(Object)
		 */
		public boolean belongsTo(Object family) {
			return FAMILY_DESCRIPTION_CACHE_FLUSH.equals(family);
		}

		/* (non-Javadoc)
		 * See WorkspaceJob#runInWorkspace(IProgressMonitor)
		 */
		public IStatus runInWorkspace(final IProgressMonitor monitor) {
			try {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				synchronized (ContentDescriptionManager.this) {
					// nothing to be done if no information cached
					if (getCacheState() == EMPTY_CACHE)
						return Status.OK_STATUS;
					setCacheState(FLUSHING_CACHE);
					// flush the MRU cache
					cache.discardAll();
					// discard content type related flags for all files in the tree 
					IElementContentVisitor visitor = new IElementContentVisitor() {
						public boolean visitElement(ElementTree tree, IPathRequestor requestor, Object elementContents) {
							if (monitor.isCanceled())
								throw new OperationCanceledException();
							if (elementContents == null)
								return false;
							ResourceInfo info = (ResourceInfo) elementContents;
							if (info.getType() != IResource.FILE)
								return true;
							info = workspace.getResourceInfo(requestor.requestPath(), false, true);
							if (info == null)
								return false;
							info.clear(ICoreConstants.M_CONTENT_CACHE);
							return true;
						}
					};
					try {
						new ElementTreeIterator(workspace.getElementTree(), Path.ROOT).iterate(visitor);
					} catch (WrappedRuntimeException e) {
						throw (CoreException) e.getTargetException();
					}
					// done cleaning
					setCacheState(EMPTY_CACHE);
				}
				monitor.worked(Policy.opWork);
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}
	}

	/** 
	 * An input stream that only opens the file if bytes are actually requested.
	 * @see #readDescription(File)
	 */
	class LazyFileInputStream extends InputStream {
		private InputStream actual;
		private IPath target;

		LazyFileInputStream(IPath target) {
			this.target = target;
		}

		public int available() throws IOException {
			if (actual == null)
				return 0;
			return actual.available();
		}

		public void close() throws IOException {
			if (actual == null)
				return;
			actual.close();
		}

		private void ensureOpened() throws FileNotFoundException {
			if (actual != null)
				return;
			if (target == null)
				throw new FileNotFoundException();
			actual = new FileInputStream(target.toFile());
		}

		public int read() throws IOException {
			ensureOpened();
			return actual.read();
		}

		public int read(byte[] b, int off, int len) throws IOException {
			ensureOpened();
			return actual.read(b, off, len);
		}

		public long skip(long n) throws IOException {
			ensureOpened();
			return actual.skip(n);
		}
	}

	private static final QualifiedName CACHE_STATE = new QualifiedName(ResourcesPlugin.PI_RESOURCES, "contentCacheState"); //$NON-NLS-1$
	private static final QualifiedName CACHE_TIMESTAMP = new QualifiedName(ResourcesPlugin.PI_RESOURCES, "contentCacheTimestamp"); //$NON-NLS-1$\
	public static final byte EMPTY_CACHE = 1;

	public static final String FAMILY_DESCRIPTION_CACHE_FLUSH = ResourcesPlugin.PI_RESOURCES + ".contentDescriptionCacheFamily"; //$NON-NLS-1$	
	public static final byte FLUSHING_CACHE = 4;
	public static final byte INVALID_CACHE = 3;

	private static final String PT_CONTENTTYPES = "contentTypes"; //$NON-NLS-1$
	public static final byte USED_CACHE = 2;

	Cache cache;

	private byte cacheState;

	private Job flushJob;

	Workspace workspace;

	/**
	 * @see IContentTypeManager.IContentTypeChangeListener#contentTypeChanged(ContentTypeChangeEvent)
	 */
	public void contentTypeChanged(ContentTypeChangeEvent event) {
		invalidateCache(true);
	}

	Cache getCache() {
		return cache;
	}

	/** Public so tests can examine it. */
	public synchronized byte getCacheState() {
		if (cacheState != 0)
			// we have read/set it before, no nead to read property
			return cacheState;
		String persisted;
		try {
			persisted = workspace.getRoot().getPersistentProperty(CACHE_STATE);
			cacheState = persisted != null ? Byte.parseByte(persisted) : INVALID_CACHE;
		} catch (NumberFormatException e) {
			cacheState = INVALID_CACHE;
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
			cacheState = INVALID_CACHE;
		}
		return cacheState;
	}

	private long getCacheTimestamp() throws CoreException {
		try {
			return Long.parseLong(workspace.getRoot().getPersistentProperty(CACHE_TIMESTAMP));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public IContentDescription getDescriptionFor(File file, ResourceInfo info) throws CoreException {
		switch (getCacheState()) {
			case INVALID_CACHE :
				// the cache is not good, flush it
				flushJob.schedule(1000);
			//fall through and just read the file
			case FLUSHING_CACHE :
				// the cache is being flushed, but is still not good, just read the file
				return readDescription(file);
		}
		// first look for the flags in the resource info to avoid looking in the cache
		// don't need to copy the info because the modified bits are not in the deltas
		if (info == null)
			return null;
		if (info.isSet(ICoreConstants.M_NO_CONTENT_DESCRIPTION))
			// presumably, this file has no known content type
			return null;
		if (info.isSet(ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION)) {
			// this file supposedly has a default content description for an "obvious" content type			
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			// try to find the obvious content type matching its name
			IContentType type = contentTypeManager.findContentTypeFor(file.getName());
			if (type != null)
				// we found it, we are done
				return type.getDefaultDescription();
			// for some reason, there was no content type for this file name
			// fix this and keep going			
			info.clear(ICoreConstants.M_CONTENT_CACHE);
		}
		synchronized (this) {
			// tries to get a description from the cache	
			Cache.Entry entry = cache.getEntry(file.getFullPath());
			if (entry != null && entry.getTimestamp() == info.getContentId())
				// there was a description in the cache, and it was up to date
				return (IContentDescription) entry.getCached();
			// we are going to add an entry to the cache or update the resource info - remember that
			setCacheState(USED_CACHE);
			// either we didn't find a description in the cache, or it was not up-to-date - has to be read again
			IContentDescription newDescription = readDescription(file);
			if (newDescription == null) {
				// no content type exists for this file name/contents - remember this
				info.set(ICoreConstants.M_NO_CONTENT_DESCRIPTION);
				return null;
			}
			if (newDescription.getContentType().getDefaultDescription().equals(newDescription)) {
				// we got a default description
				IContentType defaultForName = Platform.getContentTypeManager().findContentTypeFor(file.getName());
				if (newDescription.getContentType().equals(defaultForName)) {
					// it is a default description for the obvious content type given its file name, we don't have to cache					
					info.set(ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION);
					return newDescription;
				}
			}
			// we actually got a description filled by a describer (or a default description for a non-obvious type)
			if (entry == null)
				// there was no entry before - create one
				entry = cache.addEntry(file.getFullPath(), newDescription, info.getContentId());
			else {
				// just update the existing entry
				entry.setTimestamp(info.getContentId());
				entry.setCached(newDescription);
			}
			return newDescription;
		}
	}

	private long getPlatformTimeStamp() {
		return Platform.getPlatformAdmin().getState(false).getTimeStamp();
	}

	public synchronized void invalidateCache(boolean flush) {
		if (getCacheState() == EMPTY_CACHE)
			// cache has not been touched, nothing to do			
			return;
		// mark the cache as invalid
		try {
			setCacheState(INVALID_CACHE);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
		if (!flush)
			return;
		flushJob.schedule(1000);
	}

	/**
	 * Tries to obtain a content description for the given file.  
	 */
	private IContentDescription readDescription(File file) throws CoreException {
		// tries to obtain a description for this file contents
		InputStream contents = new LazyFileInputStream(file.getLocation());
		try {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			return contentTypeManager.getDescriptionFor(contents, file.getName(), IContentDescription.ALL);
		} catch (IOException e) {
			String message = NLS.bind(Messages.resources_errorContentDescription, file.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, file.getFullPath(), message, e);
		} finally {
			file.ensureClosed(contents);
		}
	}

	/**
	 * @see IRegistryChangeListener#registryChanged(IRegistryChangeEvent)
	 */
	public void registryChanged(IRegistryChangeEvent event) {
		// no changes related to the content type registry
		if (event.getExtensionDeltas(Platform.PI_RUNTIME, PT_CONTENTTYPES).length == 0)
			return;
		invalidateCache(true);
	}

	synchronized void setCacheState(byte newCacheState) throws CoreException {
		if (cacheState == newCacheState)
			return;
		workspace.getRoot().setPersistentProperty(CACHE_STATE, Byte.toString(newCacheState));
		cacheState = newCacheState;
	}

	private void setCacheTimeStamp(long timeStamp) throws CoreException {
		workspace.getRoot().setPersistentProperty(CACHE_TIMESTAMP, Long.toString(timeStamp));
	}

	public void shutdown(IProgressMonitor monitor) throws CoreException {
		if (getCacheState() != INVALID_CACHE)
			// remember the platform timestamp for which we have a valid cache 
			setCacheTimeStamp(getPlatformTimeStamp());
		Platform.getContentTypeManager().removeContentTypeChangeListener(this);
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		cache.dispose();
		cache = null;
		flushJob.cancel();
		flushJob = null;
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		workspace = (Workspace) ResourcesPlugin.getWorkspace();
		cache = new Cache(100, 1000, 0.1);
		getCacheState();
		if (cacheState == FLUSHING_CACHE)
			// in case we died before completing the last flushing 
			setCacheState(INVALID_CACHE);
		flushJob = new FlushJob();
		if (getCacheTimestamp() != getPlatformTimeStamp())
			invalidateCache(false);
		// register a content type change listener
		Platform.getContentTypeManager().addContentTypeChangeListener(this);
		// register a registry change listener		
		Platform.getExtensionRegistry().addRegistryChangeListener(this, Platform.PI_RUNTIME);
	}
}