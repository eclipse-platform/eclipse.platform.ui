/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A specialized Replace operation that will update managed resources and
 * unmanaged resources that are conflicting additions (so that the remote is fetched)
 */
public class OverrideAndUpdateOperation extends ReplaceOperation {

	private IResource[] conflictingAdditions;

	public OverrideAndUpdateOperation(IWorkbenchPart part, IResource[] allResources, IResource[] conflictingAdditions, CVSTag tag, boolean recurse) {
		super(part, allResources, tag, recurse);
		this.conflictingAdditions = conflictingAdditions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ReplaceOperation#getResourcesToUpdate(org.eclipse.team.internal.ccvs.core.ICVSResource[])
	 */
	protected ICVSResource[] getResourcesToUpdate(ICVSResource[] resources) throws CVSException {
		// Add the conflicting additions to the list of resources to update
		Set update = new HashSet();
		ICVSResource[] conflicts = getCVSArguments(conflictingAdditions);
		update.addAll(Arrays.asList(conflicts));
		update.addAll(Arrays.asList(super.getResourcesToUpdate(resources)));
		return (ICVSResource[]) update.toArray(new ICVSResource[update.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#updateWorkspaceSubscriber(org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.team.internal.ccvs.core.ICVSResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void updateWorkspaceSubscriber(CVSTeamProvider provider, ICVSResource[] resources, IProgressMonitor monitor) {
		// No need to update the workspace subscriber since we know this operation will do it properly by itself
	}

}
