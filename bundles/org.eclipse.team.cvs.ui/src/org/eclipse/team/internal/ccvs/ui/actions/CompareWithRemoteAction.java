package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSLocalCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class CompareWithRemoteAction extends CompareWithTagAction {

	public void execute(IAction action) {
		IResource[] resources;
		resources = getSelectedResources();
		CVSTag[] tags = new CVSTag[resources.length];
		try {
			for (int i = 0; i < resources.length; i++) {
				tags[i] = getTag(resources[i]);
			}
			CompareUI.openCompareEditor(new CVSLocalCompareEditorInput(resources, tags));
		} catch(CVSException e) {
			ErrorDialog.openError(getShell(), Policy.bind("CompareWithRemoteAction.compare"),  //$NON-NLS-1$
								  Policy.bind("CompareWithRemoteAction.noRemoteLong"), e.getStatus()); //$NON-NLS-1$
		}			
	}
	
	protected CVSTag getTag(IResource resource) throws CVSException {
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		CVSTag tag = null;
		if (cvsResource.isFolder()) {
			FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
			if (folderInfo!=null) {
				tag = folderInfo.getTag();
				if(tag == null) {
					tag = CVSTag.DEFAULT;
				}
			} 
		} else {
			ResourceSyncInfo info = cvsResource.getSyncInfo();
			if (info != null) {					
				tag = info.getTag();
			}
		}
		if (tag==null) {
			if (cvsResource.getParent().isCVSFolder()) {
				tag = cvsResource.getParent().getFolderSyncInfo().getTag();
				if(tag == null) {
					tag = CVSTag.DEFAULT;
				}
			}
		}
		return tag;
	}
	
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if(resources.length>0) {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if(RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId()) == null) {
					return false;
				}
				try {
					if(getTag(resource) == null) {
						return false;
					}
				} catch(CVSException e) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}