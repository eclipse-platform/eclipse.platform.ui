package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
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
	
	private void makeRemoteLocal(CVSRemoteSyncElement element, IProgressMonitor monitor) throws CVSException {
		IRemoteResource remote = element.getRemote();
		final IResource local = element.getLocal();
		try {
			if(remote==null) {
				// Need a runnable so that move/delete hook is disabled
				final CoreException[] exception = new CoreException[] { null };
				CVSWorkspaceRoot.getCVSFolderFor(local.getParent()).run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							local.delete(false, monitor);
						} catch(CoreException e) {
							exception[0] = e;
						}
					}
				}, monitor);
				if (exception[0] != null) {
					throw exception[0];
				}
			} else {
				if(remote.isContainer()) {
					if(!local.exists()) {
						((IFolder)local).create(false /*don't force*/, true /*local*/, monitor);
					}
					CVSWorkspaceRoot.getCVSFolderFor((IContainer)local).setFolderSyncInfo(((ICVSFolder)remote).getFolderSyncInfo());
				} else {
					monitor.beginTask(null, 200);
					try {
						IFile localFile = (IFile)local;
						if(local.exists()) {
							localFile.setContents(remote.getContents(Policy.subMonitorFor(monitor, 100)), false /*don't force*/, true /*keep history*/, Policy.subMonitorFor(monitor, 100));
						} else {
							if (!localFile.getParent().exists()) {
								IContainer parent = localFile.getParent();
								while (!parent.exists()) {
									IFolder folder = (IFolder)parent;
									folder.create(false, true, null);
									parent = parent.getParent();
								}
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
		} catch(TeamException e) {
			throw new CVSException(Policy.bind("UpdateMergeActionProblems_merging_remote_resources_into_workspace_2"), e); //$NON-NLS-1$
		}
	}	
}