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

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSLocalCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class CompareWithRemoteAction extends WorkspaceAction {

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		final CVSTag[] tags = new CVSTag[resources.length];
		try {
			for (int i = 0; i < resources.length; i++) {
				tags[i] = getTag(resources[i]);
			}
		} catch(CVSException e) {
			throw new InvocationTargetException(e);
		}	
		
		// Show the compare viewer
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CompareUI.openCompareEditorOnPage(
				  new CVSLocalCompareEditorInput(resources, tags),
				  getTargetPage());
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);		
	}
	
	protected CVSTag getTag(IResource resource) throws CVSException {
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		return getTag(cvsResource);
	}
		
	protected CVSTag getTag(ICVSResource cvsResource) throws CVSException {
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
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CompareWithRemoteAction.compare"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		if (super.isEnabledForCVSResource(cvsResource)) {
			// Don't enable if there are sticky file revisions in the lineup
			if (!cvsResource.isFolder()) {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if (info != null && info.getTag() != null) {
					String revision = info.getRevision();
					String tag = info.getTag().getName();
					if (revision.equals(tag)) return false;
				}
			}
			return true;
		} else {
			return getTag(cvsResource) != null;
		}
	}
	
	/*
	 * Update the text label for the action based on the tags in the
	 * selection.
	 * @see TeamAction#setActionEnablement(org.eclipse.jface.action.IAction)
	 */
	protected void setActionEnablement(IAction action) {
		super.setActionEnablement(action);
		action.setText(calculateActionTagValue());
	}

}
