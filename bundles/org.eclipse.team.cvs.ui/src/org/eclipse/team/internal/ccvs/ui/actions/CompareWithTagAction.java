/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.ui.CVSLocalCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;

public class CompareWithTagAction extends WorkspaceAction {

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final ICVSRemoteResource[] remoteResource = new ICVSRemoteResource[] { null };
		final IResource[] resources = getSelectedResources();
		
		IProject[] projects = new IProject[resources.length];
		for (int i = 0; i < resources.length; i++) {
			projects[i] = resources[i].getProject();
		}
		TagSelectionDialog dialog = new TagSelectionDialog(getShell(), projects, 
			Policy.bind("CompareWithTagAction.message"),  //$NON-NLS-1$
			Policy.bind("TagSelectionDialog.Select_a_Tag_1"), //$NON-NLS-1$
			TagSelectionDialog.INCLUDE_ALL_TAGS, 
			false, /* show recurse*/
			IHelpContextIds.COMPARE_TAG_SELECTION_DIALOG);
		dialog.setBlockOnOpen(true);
		int result = dialog.open();
		if (result == Dialog.CANCEL || dialog.getResult() == null) {
			return;
		}
		final CVSTag tag = dialog.getResult();
		if (tag == null) return;
		// Show the compare viewer
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CompareUI.openCompareEditor(new CVSLocalCompareEditorInput(resources, tag));
			}
		}, false /* cancelable */, this.PROGRESS_BUSYCURSOR);
	}
	
	protected boolean isEnabled() throws TeamException {
		return isSelectionNonOverlapping();
	}

}
