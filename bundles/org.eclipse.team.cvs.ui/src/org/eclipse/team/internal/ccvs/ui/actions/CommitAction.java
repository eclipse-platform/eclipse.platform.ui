/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for checking in files to a CVS provider.
 * Prompts the user for a release comment.
 */
public class CommitAction extends WorkspaceAction {
	
	/*
	 * @see CVSAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				int ticks=100;
				monitor.beginTask(null, ticks);
				try {
					final IResource[] resources = getSelectedResources();
					final RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
					
					// search for any non-added, non-ignored resources in the selection
					IResource[] unadded = getUnaddedResources(resources, Policy.subMonitorFor(monitor,10));
					ticks-=10;
					if (unadded.length > 0) {
						// ask the user what files they want to add
						unadded = promptToAddResources(unadded);
						if (unadded.length > 0) {
							manager.add(unadded, Policy.subMonitorFor(monitor, 10));
							ticks-=10;
						}
					}
					IResource[] shared = getSharedResources(resources);
					if (shared.length == 0) return;
					String comment = promptForComment(manager);
					if (comment != null) {
						manager.commit(resources, comment, Policy.subMonitorFor(monitor,ticks));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG); //$NON-NLS-1$
	}

	/**
	 * Method promptToAddResources.
	 * @param unadded
	 * @return IResource[]
	 */
	private IResource[] promptToAddResources(IResource[] unadded) throws InterruptedException {
		final int[] r = new int[1];
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog dialog = new MessageDialog(
					getShell(),
					Policy.bind("CommitAction.UnaddedTitle"),  //$NON-NLS-1$
					null,
					Policy.bind("CommitAction.UnaddedMessage"),  //$NON-NLS-1$
					MessageDialog.QUESTION, 
					new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 
					0);
				r[0] = dialog.open();
			}
		});
		if (r[0] == 0)
			return unadded;
		if (r[0] == 2)
			throw new InterruptedException();
		return new IResource[0];
		
	}


	/**
	 * Method getUnaddedResources.
	 * @param resources
	 * @param iProgressMonitor
	 * @return IResource[]
	 */
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
						// resource is unshared so record it
						unadded.add(resource);
						// no need to go into children because add is deep
						return false;
					}
				}, IResource.DEPTH_INFINITE, false /* include phantoms */);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
			if (exception[0] != null) throw exception[0];
		}
		return (IResource[]) unadded.toArray(new IResource[unadded.size()]);
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

	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
			if (provider == null) return false;
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			// enable commit for any non-ignored resources
			if (resource.getType()!=IResource.PROJECT&&cvsResource.isIgnored()) return false;
		}
		return true;
	}
	
	/**
	 * Prompts the user for a release comment.
	 * @return the comment, or null to cancel
	 */
	protected String promptForComment(RepositoryManager manager) {
		return manager.promptForComment(getShell());
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CommitAction.commitFailed");
	}

}
