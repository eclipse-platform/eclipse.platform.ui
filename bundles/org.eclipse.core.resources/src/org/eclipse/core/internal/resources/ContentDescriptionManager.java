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

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.internal.utils.Cache;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * A helper class for File#getContentDescription. Keeps a cache of 
 * recently read content descriptions.
 */
public class ContentDescriptionManager implements IManager {
	private Cache cache;

	public synchronized IContentDescription getDescriptionFor(IFile file) throws CoreException {
		// tries to get a description from the cache
		Cache.Entry entry = cache.getEntry(file.getFullPath());
		long modificationStamp = file.getModificationStamp();
		if (entry == null)
			// there was none - creates one
			entry = cache.addEntry(file.getFullPath(), null, modificationStamp);
		else if (entry.getTimestamp() == modificationStamp)
			// there was a description in the cache, and it was up to date
			return (IContentDescription) entry.getCached();
		// either we didn't find a description in the cache, or it was not up-to-date - has to be read again
		IContentDescription newDescription = readDescription(file);
		entry.setCached(newDescription);
		entry.setTimestamp(modificationStamp);
		return newDescription;
	}

	/**
	 * Tries to obtain a content description for the given file.  
	 */
	private IContentDescription readDescription(IFile file) throws CoreException {
		// tries to obtain a description for this file contents
		IContentTypeManager contentTypeManager = org.eclipse.core.runtime.Platform.getContentTypeManager();
		InputStream contents = file.getContents();
		boolean failed = false;
		try {
			IContentDescription newDescription = contentTypeManager.getDescriptionFor(contents, file.getName(), IContentDescription.ALL);
			// update or initialize description and modification stamp
			return newDescription;
		} catch (IOException e) {
			failed = true;
			String message = Policy.bind("resources.errorContentDescription", file.getFullPath().toString()); //$NON-NLS-1$		
			throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, file.getFullPath(), message, e);
		} finally {
			if (contents != null)
				try {
					contents.close();
				} catch (IOException e) {
					if (!failed) {
						String message = Policy.bind("resources.errorContentDescription", file.getFullPath().toString()); //$NON-NLS-1$		
						throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, file.getFullPath(), message, e);
					}
				}
		}
	}

	public void shutdown(IProgressMonitor monitor) throws CoreException {
		cache.discardAll();
		cache = null;
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		cache = new Cache(100, 1000, 0.1);
	}
}