/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.PrepareForReplaceVisitor;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Thsi operation replaces the local resources with their remote contents
 */
public class ReplaceOperation extends UpdateOperation {

	public ReplaceOperation(IWorkbenchPart part, IResource[] resources, CVSTag tag, boolean recurse) {
		super(part, resources, new LocalOption[] { Update.IGNORE_LOCAL_CHANGES }, tag);
	}

	public ReplaceOperation(IWorkbenchPart part, ResourceMapping[] mappings, CVSTag tag, boolean b) {
        super(part, mappings, new LocalOption[] { Update.IGNORE_LOCAL_CHANGES }, tag);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("ReplaceOperation.taskName"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#executeCommand(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus executeCommand(
		Session session,
		CVSTeamProvider provider,
		ICVSResource[] resources,
		boolean recurse, IProgressMonitor monitor)
		throws CVSException, InterruptedException {
			
			monitor.beginTask(null, 100);
			ICVSResource[] managedResources = getResourcesToUpdate(resources);
			try {
				// Purge any unmanaged or added files
				new PrepareForReplaceVisitor().visitResources(
					provider.getProject(), 
					resources, 
					"CVSTeamProvider.scrubbingResource", // TODO: This is a key in CVS core! //$NON-NLS-1$
					recurse ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, 
					Policy.subMonitorFor(monitor, 30)); //$NON-NLS-1$
				
				// Only perform the remote command if some of the resources being replaced were managed
				IStatus status = OK;
				if (managedResources.length > 0) {
					// Perform an update, ignoring any local file modifications
					status = super.executeCommand(session, provider, managedResources, recurse, Policy.subMonitorFor(monitor, 70));
				}
				
				// Prune any empty folders left after the resources were purged.
				// This is done to prune any empty folders that contained only unmanaged resources
				if (status.isOK() && CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) {
					new PruneFolderVisitor().visit(session, resources);
				}
				
				return status;
			} finally {
				monitor.done();
			}
	}

	/**
	 * Return the resources that need to be updated from the server.
	 * By default, this is all resources that are managed.
	 * @param resources all resources being replaced
	 * @return resources that ae to be updated from the server
	 * @throws CVSException
	 */
	protected ICVSResource[] getResourcesToUpdate(ICVSResource[] resources) throws CVSException {
		// Accumulate the managed resources from the list of provided resources
		List managedResources = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			ICVSResource resource = resources[i];
			if ((resource.isFolder() && ((ICVSFolder)resource).isCVSFolder())) {
				managedResources.add(resource);
			} else if (!resource.isFolder()){
				byte[] syncBytes = ((ICVSFile)resource).getSyncBytes();
				if (syncBytes != null && !ResourceSyncInfo.isAddition(syncBytes)) {
					managedResources.add(resource);
				}
			}
		}
		return (ICVSResource[]) managedResources.toArray(new ICVSResource[managedResources.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation#getUpdateCommand()
	 */
	protected Update getUpdateCommand() {
		return Command.REPLACE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(CVSTeamProvider provider) {
		return Policy.bind("ReplaceOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}
}
