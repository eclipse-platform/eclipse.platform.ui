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
import org.eclipse.core.internal.content.ContentType;
import org.eclipse.core.internal.utils.Cache;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;

/**
 * A helper class for File#getContentDescription. Keeps a cache of 
 * recently read content descriptions.
 */
public class ContentDescriptionManager implements IManager {
	private Cache cache;

	Cache getCache() {
		return cache;
	}

	public IContentDescription getDescriptionFor(File file, ResourceInfo info) throws CoreException {
		//first look for cached description information to avoid looking in the cache
		// don't need to copy the info because the modified bits are not in the deltas
		if (info == null)
			return null;
		int flags = info.getFlags();
		if ((flags & ICoreConstants.M_NO_CONTENT_DESCRIPTION) != 0)
			return null;
		if ((flags & ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION) != 0) {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			IContentType type = contentTypeManager.findContentTypeFor(file.getName());
			if (type != null)
				return ((ContentType) type).getDefaultDescription();
		}
		//make sure no cached information is set on the info
		info.clear(ICoreConstants.M_CONTENT_CACHE);
		// tries to get a description from the cache	
		// synchronized to prevent concurrent modification in the cache 
		synchronized (this) {
			Cache.Entry entry = cache.getEntry(file.getFullPath());
			if (entry != null && entry.getTimestamp() == info.getContentId())
				// there was a description in the cache, and it was up to date
				return (IContentDescription) entry.getCached();
			// either we didn't find a description in the cache, or it was not up-to-date - has to be read again
			IContentDescription newDescription = readDescription(file);
			if (newDescription == null) {
				// no content type exists for this file name/contents
				info.set(ICoreConstants.M_NO_CONTENT_DESCRIPTION);
				return null;
			}
			// if it is a default description for the default type, we don't have to cache 
			if (((ContentType) newDescription.getContentType()).getDefaultDescription() == newDescription) {
				IContentType defaultForName = Platform.getContentTypeManager().findContentTypeFor(file.getName());
				if (newDescription.getContentType() == defaultForName) {
					// the default content description is enough for this file
					info.set(ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION);
					return newDescription;
				}
			}
			// we actually got a description filled by a describer (or a default description for a non-obvious type)
			if (entry == null)
				// there was none - creates one
				entry = cache.addEntry(file.getFullPath(), newDescription, info.getContentId());
			else {
				entry.setTimestamp(info.getContentId());
				entry.setCached(newDescription);
			}
			return newDescription;
		}
	}

	/**
	 * Tries to obtain a content description for the given file.  
	 */
	private IContentDescription readDescription(File file) throws CoreException {
		// tries to obtain a description for this file contents
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		InputStream contents = file.getContents(true);
		try {
			IContentDescription newDescription = contentTypeManager.getDescriptionFor(contents, file.getName(), IContentDescription.ALL);
			// update or initialize description and modification stamp
			return newDescription;
		} catch (IOException e) {
			String message = Policy.bind("resources.errorContentDescription", file.getFullPath().toString()); //$NON-NLS-1$		
			throw new ResourceException(IResourceStatus.FAILED_DESCRIBING_CONTENTS, file.getFullPath(), message, e);
		} finally {
			file.ensureClosed(contents);
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