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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.UpdateMergableOnly;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This operation performs an update that will only effect files
 * that have no conflicts or automergable conflicts.
 */
public class UpdateOnlyMergableOperation extends RepositoryProviderOperation {

	private LocalOption[] localOptions;

	List skippedFiles = new ArrayList();
	
	public UpdateOnlyMergableOperation(Shell shell, IResource[] resources, LocalOption[] localOptions) {
		super(shell, resources);
		this.localOptions = localOptions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("UpdateOnlyMergeable.taskName");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#execute(org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(CVSTeamProvider provider, final IResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException {
		CVSWorkspaceRoot workspaceRoot = provider.getCVSWorkspaceRoot();
		Session.run(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot(), true /* output to console */,
			new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					UpdateMergableOnly update = new UpdateMergableOnly();
					IStatus status = update.execute(
						Command.NO_GLOBAL_OPTIONS, 
						localOptions, 
						getCVSArguments(resources),
						null, 
						monitor);
					if (status.getCode() != IStatus.ERROR) {
						addSkippedFiles(update.getSkippedFiles());
					} else {
						addError(status);
					}
				}
			}, monitor);

	}

	protected void addSkippedFiles(IFile[] files) {
		skippedFiles.addAll(Arrays.asList(files));
	}
	
	public IFile[] getSkippedFiles() {
		return (IFile[]) skippedFiles.toArray(new IFile[skippedFiles.size()]);
	}
}
