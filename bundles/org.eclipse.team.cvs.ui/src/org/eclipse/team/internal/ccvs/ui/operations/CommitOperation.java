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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Perform a "cvs commit"
 */
public class CommitOperation extends SingleCommandOperation {

	public CommitOperation(IWorkbenchPart part, IResource[] resources, LocalOption[] options) {
		super(part, resources, options);
	}
	
	/**
	 * Perform prompting for unadded resources and comment
	 * @param monitor a progess monitor
	 * @return <code>true</code> if execution should continue
	 */
	public boolean performPrompting(IProgressMonitor monitor) throws CVSException, InvocationTargetException, InterruptedException {
		monitor.beginTask(null, 20);
		IResource[] resourcesToBeAdded = promptForResourcesToBeAdded(Policy.subMonitorFor(monitor, 10));
		String comment = promptForComment(getResources());
		if (comment == null) return false;
		addLocalOption(Commit.makeArgumentOption(Command.MESSAGE_OPTION, comment));
		if (resourcesToBeAdded.length > 0) {
			new AddOperation(getPart(), resourcesToBeAdded)
				.run(Policy.subMonitorFor(monitor, 10));
		}
		setResources(getSharedResources(getResources()));
		monitor.done();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		// Ensure that a comment has been provided
		if (!Command.MESSAGE_OPTION.isElementOf(getLocalOptions())) {
			String comment = promptForComment(getResources());
			if (comment == null) return;
			addLocalOption(Commit.makeArgumentOption(Command.MESSAGE_OPTION, comment));
		}
		super.execute(monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#executeCommand(org.eclipse.team.internal.ccvs.core.client.Session, org.eclipse.team.internal.ccvs.core.CVSTeamProvider, org.eclipse.team.internal.ccvs.core.ICVSResource[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus executeCommand(Session session, CVSTeamProvider provider, ICVSResource[] resources, IProgressMonitor monitor) throws CVSException, InterruptedException {
		return Command.COMMIT.execute(session,
				Command.NO_GLOBAL_OPTIONS,
				getLocalOptions(),
				resources, 
				null,
				monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#handleErrors(org.eclipse.core.runtime.IStatus[])
	 */
	protected void handleErrors(IStatus[] errors) throws CVSException {
		// We are only concerned with server errors
		List serverErrors = new ArrayList();
		for (int i = 0; i < errors.length; i++) {
			IStatus status = errors[i];
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				serverErrors.add(status);
			}
		}
		if (serverErrors.isEmpty()) return;
		super.handleErrors((IStatus[]) serverErrors.toArray(new IStatus[serverErrors.size()]));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("RepositoryManager.committing"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(CVSTeamProvider provider) {
		return Policy.bind("CommitOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getErrorMessage(org.eclipse.core.runtime.IStatus[], int)
	 */
	protected String getErrorMessage(IStatus[] failures, int totalOperations) {
		return Policy.bind("CommitAction.commitFailed"); //$NON-NLS-1$
	}
	
	private IResource[] getUnaddedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws CVSException {
		final List unadded = new ArrayList();
		final CVSException[] exception = new CVSException[] { null };
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// visit each resource deeply
			try {
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
						// skip ignored resources and their children
						try {
							if (cvsResource.isIgnored())
								return false;
							// visit the children of shared resources
							if (cvsResource.isManaged())
								return true;
							if (cvsResource.isFolder() && ((ICVSFolder)cvsResource).isCVSFolder())
								return true;
						} catch (CVSException e) {
							exception[0] = e;
						}
						// don't add folders to avoid comitting empty folders
						if (resource.getType() == IResource.FOLDER)
							return true;
						// file is unshared so record it
						unadded.add(resource);
						// no need to go into children because add is deep
						return false;
					}
				});
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
			if (exception[0] != null) throw exception[0];
		}
		return (IResource[]) unadded.toArray(new IResource[unadded.size()]);
	}
	
	protected IResource[] promptForResourcesToBeAdded(IProgressMonitor monitor) throws CVSException {
		IResource[] unadded = getUnaddedResources(getResources(), monitor);
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		return manager.promptForResourcesToBeAdded(getShell(), unadded);
	}
	
	protected String promptForComment(IResource[] resourcesToCommit) {
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		return manager.promptForComment(getShell(), resourcesToCommit, null);
	}
	
	/*
	 * Return all resources in the provided collection that are shared with a repo
	 * @param resources
	 * @return IResource[]
	 */
	private IResource[] getSharedResources(IResource[] resources) throws CVSException {
		List shared = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			if (cvsResource.isManaged() 
			  || (cvsResource.isFolder() && ((ICVSFolder)cvsResource).isCVSFolder())) {
			  	shared.add(resource);
			}
		}
		return (IResource[]) shared.toArray(new IResource[shared.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.SingleCommandOperation#isServerModificationOperation()
	 */
	protected boolean isServerModificationOperation() {
		return true;
	}
}
