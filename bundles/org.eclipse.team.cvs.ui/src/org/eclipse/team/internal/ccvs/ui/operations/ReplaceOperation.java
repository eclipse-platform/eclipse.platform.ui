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
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.util.PrepareForReplaceVisitor;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Thsi operation replaces the local resources with their remote contents
 */
public class ReplaceOperation extends UpdateOperation {

	boolean recurse = true; 

	public ReplaceOperation(Shell shell, IResource[] resources, CVSTag tag, boolean recurse) {
		super(shell, resources, getReplaceOptions(recurse), tag);
		this.recurse = recurse;
	}

	/*
	 * Create the local options required to do a replace
	 */
	private static LocalOption[] getReplaceOptions(boolean recurse) {
		List options = new ArrayList();
		options.add(Update.IGNORE_LOCAL_CHANGES);
		if(!recurse) {
			options.add(Command.DO_NOT_RECURSE);
		}
		return (LocalOption[]) options.toArray(new LocalOption[options.size()]);
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
		IProgressMonitor monitor)
		throws CVSException, InterruptedException {
			
			monitor.beginTask(null, 100);
			// Accumulate the managed resources from the list of provided resources
			List managedResources = new ArrayList();
			for (int i = 0; i < resources.length; i++) {
				ICVSResource resource = resources[i];
				if (resource.isManaged() || 
						(resource.isFolder() && ((ICVSFolder)resource).isCVSFolder())) {
					managedResources.add(resource);
				}
			}
			try {
				new PrepareForReplaceVisitor().visitResources(
					provider.getProject(), 
					resources, 
					"CVSTeamProvider.scrubbingResource", // TODO: This is a key in CVS core! //$NON-NLS-1$
					recurse ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, 
					Policy.subMonitorFor(monitor, 30)); //$NON-NLS-1$
				
				// Only perform the remote command if some of the resources being replaced were managed
				if (managedResources.isEmpty()) {
					return OK;
				} else {
					// Perform an update, ignoring any local file modifications
					return super.executeCommand(session, provider, resources, Policy.subMonitorFor(monitor, 70));
				}
			} finally {
				monitor.done();
			}
	}
	
}
