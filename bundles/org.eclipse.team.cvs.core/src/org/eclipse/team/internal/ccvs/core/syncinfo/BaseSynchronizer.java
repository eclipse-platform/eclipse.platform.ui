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
package org.eclipse.team.internal.ccvs.core.syncinfo;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;

/**
 * A base sychronizer provides access to the base sync bytes for the 
 * resources in the local workspace
 */
public class BaseSynchronizer extends ResourceSynchronizer {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer#getSyncBytes(org.eclipse.core.resources.IResource)
	 */
	public byte[] getSyncBytes(IResource resource) throws CVSException {
		if (resource.getType() == IResource.FILE) {
			// For a file, return the entry line
			byte[] bytes =  EclipseSynchronizer.getInstance().getSyncBytes(resource);
			if (bytes != null) {
				// Use the base sync info (i.e. no deletion or addition)
				if (ResourceSyncInfo.isDeletion(bytes)) {
					bytes = ResourceSyncInfo.convertFromDeletion(bytes);
				} else if (ResourceSyncInfo.isAddition(bytes)) {
					bytes = null;
				}
			}
			return bytes;
		} else {
			// For a folder, return the folder sync info bytes
			FolderSyncInfo info = EclipseSynchronizer.getInstance().getFolderSync((IContainer)resource);
			if (info == null) return null;
			return info.getBytes();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer#getSyncName()
	 */
	protected QualifiedName getSyncName() {
		return new QualifiedName(CVSProviderPlugin.ID, "workspace"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer#refresh(org.eclipse.core.resources.IResource[], int, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IResource[] refresh(
		IResource[] resources,
		int depth,
		boolean cacheFileContentsHint,
		IProgressMonitor monitor)
		throws TeamException {
			
		// TODO Ensure that file contents are cached for modified local files
		return super.refresh(resources, depth, cacheFileContentsHint, monitor);
	}
}
