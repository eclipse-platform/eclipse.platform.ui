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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.BranchPromptDialog;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * BranchAction tags the selected resources with a branch tag specified by the user,
 * and optionally updates the local resources to point to the new branch.
 */
public class BranchAction extends WorkspaceAction {

	/*
	 * @see CVSAction#execute()
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {

		// Prompt for the branch tag and whether to start working in the branch
		final IResource[] resources = getSelectedResources();
		boolean allSticky = areAllResourcesSticky(resources);
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(resources[0].getProject());
		final BranchPromptDialog dialog = new BranchPromptDialog(getShell(),
											Policy.bind("BranchWizard.title"), //$NON-NLS-1$
											folder, 
											allSticky, 
											calculateInitialVersionName(resources,allSticky));
		if (dialog.open() != InputDialog.OK) return;		
		
		// Capture the dialog info in local variables
		final String tagString = dialog.getBranchTagName();
		final boolean update = dialog.getUpdate();
		final String versionString = dialog.getVersionTagName();
		final CVSTag rootVersionTag = (versionString == null) ? null : new CVSTag(versionString, CVSTag.VERSION);
		final CVSTag branchTag = new CVSTag(tagString, CVSTag.BRANCH);
								
		// For non-projects determine if the tag being loaded is the same as the resource's parent
		// If it's not, warn the user that they will be mixing tags
		if (update) {
			try {
				if(!CVSAction.checkForMixingTags(getShell(), resources, branchTag)) {
					return;
				}
			} catch (CVSException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		final RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		
		// perform the branch
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				Hashtable table = getProviderMapping(resources);
				Set keySet = table.keySet();
				monitor.beginTask(null, keySet.size() * 1000);
				Iterator iterator = keySet.iterator();
				while (iterator.hasNext()) {
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
					CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
					List list = (List)table.get(provider);
					IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);											
					try {
						ICVSRepositoryLocation root = provider.getCVSWorkspaceRoot().getRemoteLocation();
						if (!areAllResourcesSticky(resources)) {													
							// version everything in workspace with the root version tag specified in dialog
							provider.makeBranch(providerResources, rootVersionTag, branchTag, update, subMonitor);
						} else {
							// all resources are versions, use that version as the root of the branch
							provider.makeBranch(providerResources, null, branchTag, update, subMonitor);										
						}
						if (rootVersionTag != null || update) {
							for (int i = 0; i < providerResources.length; i++) {
								ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(providerResources[i]);
								if (rootVersionTag != null) {
									manager.addTags(cvsResource, new CVSTag[] { rootVersionTag });
								}
								if (update) {
									manager.addTags(cvsResource, new CVSTag[] { branchTag });
								}
							}
						}
					} catch (TeamException e) {
						// Accumulate the status which will be displayed by CVSAction#endOperation(IAction)
						addStatus(e.getStatus());
					}
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}
	
	/**
	 * Answers <code>true</code> if all resources in the array have a sticky tag
	 */
	private boolean areAllResourcesSticky(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if(!hasStickyTag(resources[i])) return false;
		}
		return true;
	}
	
	/**
	 * Answers <code>true</code> if the resource has a sticky tag
	 */
	private boolean hasStickyTag(IResource resource) {
		try {
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);			
			CVSTag tag;
			if(cvsResource.isFolder()) {
				FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
				tag = folderInfo.getTag();
			} else {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				tag = info.getTag();
			}
			if(tag!=null) {
				int tagType = tag.getType();
				if(tagType==CVSTag.VERSION) {
					return true;
				}
			}
		} catch(CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return false;
		}
		return false;
	}
	
	private String calculateInitialVersionName(IResource[] resources, boolean allSticky) {
		String versionName = "";		 //$NON-NLS-1$
		try {
			if(allSticky) {
				IResource stickyResource = resources[0];									
				if(stickyResource.getType()==IResource.FILE) {
					ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)stickyResource);
					versionName = cvsFile.getSyncInfo().getTag().getName();
				} else {
					ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)stickyResource);
					versionName = cvsFolder.getFolderSyncInfo().getTag().getName();
				}
			}
		} catch(CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			versionName = ""; //$NON-NLS-1$
		}
		return versionName;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("BranchWizard.errorBranching"); //$NON-NLS-1$
	}

}

