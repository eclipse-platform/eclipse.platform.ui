/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
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

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
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
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRemoteResource[] editions = getSelectedRemoteResources();
				if (editions.length == 0) return;
				ICVSFolder folder;
				if (editions[0] instanceof ICVSRemoteFolder) {
					folder = (ICVSFolder)editions[0];
				} else {
					folder = editions[0].getParent();
				}
				final CVSTag tag = TagSelectionDialog.getTagToCompareWith(getShell(), new ICVSFolder[] {folder});
				if (tag == null) return;
				ResourceEditionNode left = new ResourceEditionNode(editions[0]);
				ResourceEditionNode right = new ResourceEditionNode(editions[0].forTag(tag));
				CompareUI.openCompareEditorOnPage(
				  new CVSCompareEditorInput(left, right),
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
