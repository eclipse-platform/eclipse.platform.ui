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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Policy;

/**
 * This class provides the implementation for the ICacheEntry
 */
public class RemoteContentsCacheEntry {
	
	public static final int UNINITIALIZED = 0;
	public static final int READY = 1;
	public static final int DISPOSED = 2;
	
	private String id;
	private String filePath;
	private RemoteContentsCache cache;
	private byte[] syncBytes;
	private int state = UNINITIALIZED;
	private long lastAccess;

	public RemoteContentsCacheEntry(RemoteContentsCache cache, String id, String filePath) {
		state = UNINITIALIZED;
		this.cache = cache;
		this.id = id;
		this.filePath = filePath;
		registerHit();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ICacheEntry#getContents()
	 */
	public InputStream getContents() throws TeamException {
		if (state != READY) return null;
		registerHit();
		File ioFile = getFile();
		try {
			try {
				if (ioFile.exists()) {
					return new FileInputStream(ioFile);
				}
			} catch (IOException e) {
				// Try to purge the cache and continue
				cache.purgeFromCache(this);
				throw e;
			}
		} catch (IOException e) {
			// We will end up here if we couldn't read or delete the cache file
			throw new TeamException(Policy.bind("RemoteContentsCache.fileError", ioFile.getAbsolutePath()), e); //$NON-NLS-1$
		}
		// This can occur when there is no remote contents
		return new ByteArrayInputStream(new byte[0]);
	}

	protected File getFile() {
		return new File(cache.getCachePath().toFile(), filePath);
	}

	/**
	 * Set the contents of for this cache entry. This method supports concurrency by only allowing
	 * one cache entry to be written at a time. In the case of two concurrent writes to the same cache entry,
	 * the contents from the first write is used and the content from subsequent writes is ignored.
	 * @param stream an InputStream that provides the contents to be cached
	 * @param monitor a progress monitor
	 * @throws TeamException if the entry is DISPOSED or an I/O error occurres
	 */
	public void setContents(InputStream stream, IProgressMonitor monitor) throws TeamException {
		// Use a lock to only allow one write at a time
		try {
			beginOperation();
			internalSetContents(stream, monitor);
		} finally {
			endOperation();
		}
	}
	
	private void endOperation() {
		cache.getLock().release();
	}

	private void beginOperation() {
		cache.getLock().acquire();
	}

	private void internalSetContents(InputStream stream, IProgressMonitor monitor) throws TeamException {
		// if the state is DISPOSED then there is a problem
		if (state == DISPOSED) {
			throw new TeamException("Cache entry in {0} for {1} has been disposed" + cache.getName() + id);
		}
		// Otherwise, the state is UNINITIALIZED or READY so we can proceed
		registerHit();
		File ioFile = getFile();
		try {
			
			// Open the cache file for writing
			OutputStream out;
			try {
				if (state == UNINITIALIZED) {
					out = new BufferedOutputStream(new FileOutputStream(ioFile));
				} else {
					// If the entry is READY, the contents must have been read in another thread.
					// We still need to red the contents but they can be ignored since presumably they are the same
					out = new ByteArrayOutputStream();
				}
			} catch (FileNotFoundException e) {
				throw new TeamException(Policy.bind("RemoteContentsCache.fileError", ioFile.getAbsolutePath()), e); //$NON-NLS-1$
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
				cache.purgeFromCache(this);
				throw e;
			}
			
			// Mark the cache entry as ready
			state = READY;
		} catch (IOException e) {
			throw new TeamException(Policy.bind("RemoteContentsCache.fileError", ioFile.getAbsolutePath()), e); //$NON-NLS-1$
		} finally {
			try {
				stream.close();
			} catch (IOException e1) {
				// Ignore close errors
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ICacheEntry#getSyncBytes(byte[])
	 */
	public byte[] getSyncBytes() {
		return syncBytes;
	}

	/**
	 * Set the sync bytes associated with the cached remote contents.
	 * This method is sychronized to ensure atomic setting of the bytes.
	 * @param bytes
	 */
	public void setSyncBytes(byte[] bytes) {
		try {
			beginOperation();
			syncBytes = bytes;
		} finally {
			endOperation();
		}
		syncBytes = bytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ICacheEntry#getState()
	 */
	public int getState() {
		return state;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ICacheEntry#getSize()
	 */
	public long getSize() {
		if (state != READY) return 0;
		File ioFile = getFile();
		if (ioFile.exists()) {
			return ioFile.length();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ICacheEntry#getLastAccessTimeStamp()
	 */
	public long getLastAccessTimeStamp() {
		return lastAccess;
	}

	/**
	 * Registers a hit on this cache entry. This updates the last access timestamp.
	 * Thsi method is intended to only be invokded from inside this class or the cahce itself.
	 * Other clients should not use it.
	 */
	protected void registerHit() {
		lastAccess = new Date().getTime();
	}

	public void dispose() {
		// Use a lock to avoid changing state while another thread may be writting
		try {
			beginOperation();
			state = DISPOSED;
			cache.purgeFromCache(this);
		} finally {
			endOperation();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ICacheEntry#getId()
	 */
	public String getId() {
		return id;
	}
}
