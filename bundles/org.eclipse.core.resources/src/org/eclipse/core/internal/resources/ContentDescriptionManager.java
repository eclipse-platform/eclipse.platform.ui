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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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

	public IContentDescription getDescriptionFor(File file) throws CoreException {
		ResourceInfo info = file.getResourceInfo(false, true);
		int flags = info.getFlags();
		if ((flags & ICoreConstants.M_NO_CONTENT_DESCRIPTION) != 0)
			return null;
		if ((flags & ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION) != 0) {
			IContentTypeManager contentTypeManager = org.eclipse.core.runtime.Platform.getContentTypeManager();
			IContentType type = contentTypeManager.findContentTypeFor(file.getName());
			if (type != null)
				return ((ContentType) type).getDefaultDescription();
			// it seems no content type is associated to this file name anymore... fix that 
			info.clear(ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION);
		}
		// tries to get a description from the cache		
		synchronized (this) {
			Cache.Entry entry = cache.getEntry(file.getFullPath());
			if (entry != null && entry.getTimestamp() == info.getContentId())
				// there was a description in the cache, and it was up to date
				return (IContentDescription) entry.getCached();
			// either we didn't find a description in the cache, or it was not up-to-date - has to be read again
			IContentDescription newDescription = readDescription(file);
			if (newDescription == null)
				// no content type exists for this file name
				info.set(ICoreConstants.M_NO_CONTENT_DESCRIPTION);
			else if (((ContentType) newDescription.getContentType()).getDefaultDescription() == newDescription)
				// the default content description is enough fo this file
				info.set(ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION);
			else {
				// we actually got a description filled by a describer
				if (entry == null)
					// there was none - creates one
					entry = cache.addEntry(file.getFullPath(), newDescription, info.getContentId());
				else {
					entry.setTimestamp(info.getContentId());
					entry.setCached(newDescription);
				}
			}
			return newDescription;
		}
	}

	/**
	 * Tries to obtain a content description for the given file.  
	 */
	private IContentDescription readDescription(File file) throws CoreException {
		// tries to obtain a description for this file contents
		IContentTypeManager contentTypeManager = org.eclipse.core.runtime.Platform.getContentTypeManager();
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