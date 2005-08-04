/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard;

/**
 * Action to generate a patch file using the CVS diff command.
 * 
 * NOTE: This is a temporary action and should eventually be replaced
 * by a create patch command in the compare viewer.
 */
public class GenerateDiffFileAction extends WorkspaceAction {
    
    // The initial size of this wizard.
    private final static int INITIAL_WIDTH = 300;
    private final static int INITIAL_HEIGHT = 350;
	
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void execute(IAction action) {
		final String title = CVSUIMessages.GenerateCVSDiff_title; 
		final IResource[] resources = getSelectedResources();
		final GenerateDiffFileWizard wizard = new GenerateDiffFileWizard(resources[0]);
		wizard.setWindowTitle(title);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.setMinimumPageSize(INITIAL_WIDTH, INITIAL_HEIGHT);
		dialog.open();
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getId()
	 */
	public String getId() {
		return ICVSUIConstants.CMD_CREATEPATCH;
	}
}
