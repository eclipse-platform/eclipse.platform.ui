package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.CVSResourceNode;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * Action for container compare with stream.
 */
public class CompareWithRemoteAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				String title = Policy.bind("CompareWithRemoteAction.compare");
				try {
					IResource[] resources = getSelectedResources();
					if (resources.length != 1) return;
					IResource resource = resources[0];
					
					CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resources[0].getProject());
		
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					CVSTag tag = null;
					if (cvsResource.isFolder()) {
						FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
						if (folderInfo!=null) {
							tag = folderInfo.getTag();
						}
					} else {
						ResourceSyncInfo info = cvsResource.getSyncInfo();
						if (info!=null) {					
							tag = info.getTag();
						}
					}
					if (tag==null) {
						if (cvsResource.getParent().isCVSFolder()) {
							tag = cvsResource.getParent().getFolderSyncInfo().getTag();
						} else {
							// XXX: this is wrong :> should return an error
							tag = CVSTag.DEFAULT;
						}
					}
					
					ICVSRemoteResource remoteResource = CVSWorkspaceRoot.getRemoteTree(resource, tag, new NullProgressMonitor());
					// Just to be safe...
					if (remoteResource == null) {
						MessageDialog.openInformation(getShell(), Policy.bind("CompareWithRemoteAction.noRemote"), Policy.bind("CompareWithRemoteAction.noRemoteLong"));
						return;
					}
					CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource), new ResourceEditionNode(remoteResource)));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CompareWithRemoteAction.compare"), PROGRESS_BUSYCURSOR);
	}
	
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return false;
		ITeamProvider provider = TeamPlugin.getManager().getProvider(resources[0].getProject());
		if (provider == null) return false;
		return provider.hasRemote(resources[0]);
	}
}
