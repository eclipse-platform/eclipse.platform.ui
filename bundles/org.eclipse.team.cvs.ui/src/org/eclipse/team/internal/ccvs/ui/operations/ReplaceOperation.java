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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.util.PrepareForReplaceVisitor;
import org.eclipse.team.internal.ccvs.core.util.ReplaceWithBaseVisitor;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Thsi operation replaces the local resources with their remote contents
 */
public class ReplaceOperation extends RepositoryProviderOperation {

	boolean recurse = true; 
	
	public ReplaceOperation(Shell shell, IResource[] resources, boolean recurse) {
		super(shell, resources);
		this.recurse = recurse;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("ReplaceOperation.taskName"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#execute(org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(CVSTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException {
		get(provider, resources, null, monitor);
	}

	public void get(final CVSTeamProvider provider, final IResource[] resources, final CVSTag tag, IProgressMonitor progress) throws CVSException {
			
		// Handle the retrival of the base in a special way
		if (tag != null && tag.equals(CVSTag.BASE)) {
			new ReplaceWithBaseVisitor().replaceWithBase(
				provider.getProject(), 
				resources, 
				recurse ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, 
				progress);
			return;
		}

		// Make a connection before preparing for the replace to avoid deletion of resources before a failed connection
		CVSWorkspaceRoot workspaceRoot = provider.getCVSWorkspaceRoot();
		Session.run(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot(), true /* output to console */,
			new ICVSRunnable() {
				public void run(IProgressMonitor progress) throws CVSException {
					// Prepare for the replace (special handling for "cvs added" and "cvs removed" resources
					progress.beginTask(null, 100);
					try {
						new PrepareForReplaceVisitor().visitResources(
							provider.getProject(), 
							resources, 
							"CVSTeamProvider.scrubbingResource", // TODO: This is a key in CVS core! //$NON-NLS-1$
							recurse ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, 
							Policy.subMonitorFor(progress, 30)); //$NON-NLS-1$
									
						// Perform an update, ignoring any local file modifications
						List options = new ArrayList();
						options.add(Update.IGNORE_LOCAL_CHANGES);
						if(!recurse) {
							options.add(Command.DO_NOT_RECURSE);
						}
						LocalOption[] commandOptions = (LocalOption[]) options.toArray(new LocalOption[options.size()]);
						try {
							update(provider, resources, commandOptions, tag, true /*createBackups*/, Policy.subMonitorFor(progress, 70));
						} catch (TeamException e) {
							throw CVSException.wrapException(e);
						}
					} finally {
						progress.done();
					}
				}
			}, progress);
	}
	
	/**
	 * Generally useful update.
	 * 
	 * The tag parameter determines any stickyness after the update is run. If tag is null, any tagging on the
	 * resources being updated remain the same. If the tag is a branch, version or date tag, then the resources
	 * will be appropriatly tagged. If the tag is HEAD, then there will be no tag on the resources (same as -A
	 * clear sticky option).
	 * 
	 * @param createBackups if true, creates .# files for updated files
	 */
	public void update(final CVSTeamProvider provider, IResource[] resources, LocalOption[] options, CVSTag tag, final boolean createBackups, IProgressMonitor progress) throws CVSException {
		// Build the local options
		List localOptions = new ArrayList();
		
		// Use the appropriate tag options
		if (tag != null) {
			localOptions.add(Update.makeTagOption(tag));
		}
		
		// Build the arguments list
		localOptions.addAll(Arrays.asList(options));
		final LocalOption[] commandOptions = (LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]);
		final ICVSResource[] arguments = getCVSArguments(resources);

		CVSWorkspaceRoot workspaceRoot = provider.getCVSWorkspaceRoot();
		Session.run(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot(), true /* output to console */,
			new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					IStatus status = Command.UPDATE.execute(Command.NO_GLOBAL_OPTIONS, commandOptions, arguments,
						null, monitor);
					if (status.getCode() == CVSStatus.SERVER_ERROR) {
						throw new CVSServerException(status);
					}
				}
			}, progress);
	}
}
