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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
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
		
					LocalResource cvsResource = null;
					if (resources[0].getType()==IResource.FILE) {
						cvsResource = new LocalFile(resource.getLocation().toFile());
					} else {
						cvsResource = new LocalFolder(resource.getLocation().toFile());
					}
					
					
					CVSTag tag = null;
					if (cvsResource.isFolder()) {
						FolderSyncInfo folderInfo = ((LocalFolder)cvsResource).getFolderSyncInfo();
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
					
					ICVSRemoteResource remoteResource = (ICVSRemoteResource)provider.getRemoteTree(resource, tag, new NullProgressMonitor());
					CompareUI.openCompareEditor(new CVSCompareEditorInput(new CVSResourceNode(resource), new ResourceEditionNode(remoteResource)));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CompareWithRemoteAction.compare"), PROGRESS_BUSYCURSOR);
	}
	
	protected boolean isEnabled() {
		return getSelectedResources().length == 1;
	}
}
