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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTreeBuilder;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CompareRemoteWithTagAction extends CVSAction {

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		ICVSRemoteResource[] editions = getSelectedRemoteResources();
		if (editions.length == 0) return;
		final ICVSRemoteResource resource = editions[0];
		
		final ResourceEditionNode[] input = new ResourceEditionNode[] { null /* left */, null /* right */};
		final CVSTag[] tag = new CVSTag[] { null};

		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSFolder folder;
				if (resource instanceof ICVSRemoteFolder) {
					folder = (ICVSFolder)resource;
				} else {
					folder = resource.getParent();
				}
				tag[0] = TagSelectionDialog.getTagToCompareWith(getShell(), new ICVSFolder[] {folder});
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
		
		if (tag[0] == null) return;
		
		final ICVSRemoteResource[] remote = new ICVSRemoteResource[] { null };
		if (resource.isFolder()) {
			remote[0] = resource.forTag(tag[0]);
		} else {
			run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						ICVSRepositoryLocation location = resource.getRepository();
						remote[0] = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFile)resource, tag[0], monitor);
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					}
				}
			}, false /* cancelable */, PROGRESS_DIALOG);
		}
		
		input[0] = new ResourceEditionNode(resource);
		input[1] = new ResourceEditionNode(remote[0]);
		
		if (input[0] == null || input[1] == null) return;
		
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CompareUI.openCompareEditorOnPage(
				  new CVSCompareEditorInput(input[0] /* left */, input[1] /* right */),
				  getTargetPage());
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteResource[] resources = getSelectedRemoteResources();
		// Only support single select for now.
		// Need to avoid overlap if multi-select is supported
		return resources.length == 1;
	}

}
