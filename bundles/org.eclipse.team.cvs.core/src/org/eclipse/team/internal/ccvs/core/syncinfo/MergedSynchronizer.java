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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * Override RemoteSynchronizer to allow unparented remote resource sync info
 * since the CVSMergeSubscriber only needs to know if the resource sync info 
 * of the incoming change differs from the last merge. The parenting is used
 * to create IRemoteResources which this synchronizer is not used to do
 */
public class MergedSynchronizer extends RemoteSynchronizer {

	public MergedSynchronizer(String id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.RemoteSynchronizer#parentHasSyncBytes(org.eclipse.core.resources.IResource)
	 */
	protected boolean parentHasSyncBytes(IResource resource) throws CVSException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSynchronizer#getRemoteResource(org.eclipse.core.resources.IResource)
	 */
	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		throw new UnsupportedOperationException();
	}

}
