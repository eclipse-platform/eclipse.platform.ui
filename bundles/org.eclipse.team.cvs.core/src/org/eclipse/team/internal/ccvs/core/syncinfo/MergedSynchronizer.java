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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.RemoteBytesSynchronizer;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * This synchronizer keeps track of which resources have been merged.
 * It is to be used only by the CVSMergeSubscriber.
 */
public class MergedSynchronizer extends RemoteBytesSynchronizer {

	public MergedSynchronizer(String id) {
		super(new QualifiedName(CVSRemoteSynchronizer.SYNC_KEY_QUALIFIER, id));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer#getRemoteResource(org.eclipse.core.resources.IResource)
	 */
	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ResourceSynchronizer#refresh(org.eclipse.core.resources.IResource[], int, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IResource[] refresh(IResource[] resources, int depth, boolean cacheFileContentsHint, IProgressMonitor monitor) throws TeamException {
		try {
			monitor.beginTask(null, 100);
			return new IResource[0];
		} finally {
			monitor.done();
		}
	}

}
