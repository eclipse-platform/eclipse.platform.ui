package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
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
		
		// Setup the holders
		final IResource[] resource = new IResource[] {null};
		final CVSTag[] tag = new CVSTag[] {null};
		final ICVSRemoteResource[] remoteResource = new ICVSRemoteResource[] { null };
		
		// Fetch the remote tree
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IResource[] resources = getSelectedResources();
					if (resources.length != 1) return;
					resource[0] = resources[0];
					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource[0]);
					if (cvsResource.isFolder()) {
						FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
						if (folderInfo!=null) {
							tag[0] = folderInfo.getTag();
						}
					} else {
						ResourceSyncInfo info = cvsResource.getSyncInfo();
						if (info!=null) {					
							tag[0] = info.getTag();
						}
					}
					if (tag[0]==null) {
						if (cvsResource.getParent().isCVSFolder()) {
							tag[0] = cvsResource.getParent().getFolderSyncInfo().getTag();
						} else {
							// XXX: this is wrong :> should return an error
							tag[0] = CVSTag.DEFAULT;
						}
					}
					
					monitor.beginTask(Policy.bind("CompareWithRemoteAction.fetching"), 100);
					remoteResource[0] = CVSWorkspaceRoot.getRemoteTree(resource[0], tag[0], Policy.subMonitorFor(monitor, 100));
					monitor.done();
					
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CompareWithRemoteAction.compare"), PROGRESS_DIALOG);
		
		// Just to be safe...
		if (remoteResource[0] == null) {
			MessageDialog.openInformation(getShell(), Policy.bind("CompareWithRemoteAction.noRemote"), Policy.bind("CompareWithRemoteAction.noRemoteLong"));
			return;
		}
					
		// Open the compare view
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource[0]), new ResourceEditionNode(remoteResource[0])));
			}
		}, Policy.bind("CompareWithRemoteAction.compare"), PROGRESS_BUSYCURSOR);
		
	}
	
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return false;
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resources[0].getProject(), CVSProviderPlugin.getTypeId());
		if(provider==null) return false;
		return provider.hasRemote(resources[0]);
	}
}
