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
package org.eclipse.team.internal.ccvs.ui.merge;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ccvs.ui.sync.UpdateSyncAction;
import org.eclipse.team.internal.ui.sync.ITeamNode;

/*
 * To be done:
 * 1. add another action that allows a force merge merging since we can't tell the manual vs automatic conflicts when building the sync tree.
 * 2. fix progress monitoring
 */
public class UpdateMergeAction extends UpdateSyncAction {
	public UpdateMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}
		 
	 protected void runUpdateDeletions(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
	 		 runUpdateDeep(nodes, manager, monitor);
	 }
		 
	/*
	 * @see UpdateSyncAction#runUpdateDeep(IProgressMonitor, List, RepositoryManager)
 	 * incoming-change
 	 * incoming-deletion
	 */
	protected void runUpdateDeep(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {		
		ITeamNode[] incoming = removeOutgoing(nodes);
		monitor.beginTask(null, 1000 * incoming.length);
		try {
			for (int i = 0; i < incoming.length; i++) {
				CVSRemoteSyncElement element = CVSSyncCompareInput.getSyncElementFrom(incoming[i]);
				if(element!=null) {
					makeRemoteLocal(element, new SubProgressMonitor(monitor, 1000));
				}
			}
		} finally {
			monitor.done();
		}
	}
		
	/*
	 * @see UpdateSyncAction#runUpdateIgnoreLocalShallow(IProgressMonitor, List, RepositoryManager)
	 * incoming-addition
	 * incoming-conflict (no-merge)
	 */
	protected void runUpdateIgnoreLocalShallow(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor)	throws TeamException {
		runUpdateDeep(nodes, manager, monitor);
	}

	/*
	 * @see UpdateSyncAction#runUpdateShallow(ITeamNode[], RepositoryManager, IProgressMonitor)
	 * incoming-conflict (auto-mergeable)
	 */
	protected void runUpdateShallow(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {	
		mergeWithLocal(nodes, manager, false, monitor);
	}

	protected void mergeWithLocal(ITeamNode[] nodes, RepositoryManager manager, boolean createBackup, IProgressMonitor monitor) throws TeamException {
		CVSTag startTag = ((MergeEditorInput)getDiffModel()).getStartTag();
		CVSTag endTag = ((MergeEditorInput)getDiffModel()).getEndTag();
	
		Command.LocalOption[] options = new Command.LocalOption[] {
			Command.DO_NOT_RECURSE,
			Update.makeArgumentOption(Update.JOIN, startTag.getName()),
			Update.makeArgumentOption(Update.JOIN, endTag.getName()) };

		// run a join update using the start and end tags and the join points
		manager.update(getIResourcesFrom(nodes), options, createBackup, monitor);
	}
	
	private ITeamNode[] removeOutgoing(ITeamNode[] nodes) {		
		// no filter done yet
		return nodes;
	}
	
	/*
	 * If called on a new folder, the folder will become an outgoing addition.
	 */
	private void makeRemoteLocal(CVSRemoteSyncElement element, IProgressMonitor monitor) throws CVSException {
		IRemoteResource remote = element.getRemote();
		final IResource local = element.getLocal();
		try {
			if(remote==null) {
				local.delete(false, monitor);
			} else {
				if(remote.isContainer()) {
					if(!local.exists()) {
						((IFolder)local).create(false /*don't force*/, true /*local*/, monitor);
					}
				} else {
					monitor.beginTask(null, 200);
					try {
						IFile localFile = (IFile)local;
						if(local.exists()) {
							localFile.setContents(remote.getContents(Policy.subMonitorFor(monitor, 100)), false /*don't force*/, true /*keep history*/, Policy.subMonitorFor(monitor, 100));
						} else {
							if (!localFile.getParent().exists()) {
								ensureParentExists(localFile);
							}
							localFile.create(remote.getContents(Policy.subMonitorFor(monitor, 100)), false /*don't force*/, Policy.subMonitorFor(monitor, 100));
						}
					} finally {
						monitor.done();
					}
				}
			}
		} catch(CoreException e) {
			throw new CVSException(Policy.bind("UpdateMergeActionProblems_merging_remote_resources_into_workspace_1"), e); //$NON-NLS-1$
		}
	}

	private void ensureParentExists(IResource resource) throws CoreException {
		IContainer parent = resource.getParent();
		if (!parent.exists()) {
			ensureParentExists(parent);
			IFolder folder = (IFolder)parent;
			folder.create(false, true, null);
		}
	}	
	/**
	 * @see MergeAction#getHelpContextID()
	 */
	protected String getHelpContextID() {
		return IHelpContextIds.MERGE_UPDATE_ACTION;
	}

}
