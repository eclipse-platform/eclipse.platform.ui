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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;

/**
 * This action is used for comparing two arbitrary remote resources. This is
 * enabled in the repository explorer.
 */
public class CompareRemoteResourcesAction extends CVSAction {

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRemoteResource[] editions = getSelectedRemoteResources();
				if (editions == null || editions.length != 2) {
					MessageDialog.openError(getShell(), Policy.bind("CompareRemoteResourcesAction.unableToCompare"), Policy.bind("CompareRemoteResourcesAction.selectTwoResources")); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				ResourceEditionNode left = new ResourceEditionNode(editions[0]);
				ResourceEditionNode right = new ResourceEditionNode(editions[1]);
				CompareUI.openCompareEditorOnPage(
				  new CVSCompareEditorInput(left, right),
				  getTargetPage());
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteResource[] resources = getSelectedRemoteResources();
		if (resources.length != 2) return false;
		return resources[0].isContainer() == resources[1].isContainer();
	}

}
