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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard;

/**
 * Action to generate a patch file using the CVS diff command.
 * 
 * NOTE: This is a temporary action and should eventually be replaced
 * by a create patch command in the compare viewer.
 */
public class GenerateDiffFileAction extends WorkspaceAction {
	
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void execute(IAction action) {
		final String title = Policy.bind("GenerateCVSDiff.title"); //$NON-NLS-1$
		final IResource[] resources = getSelectedResources();
		final GenerateDiffFileWizard wizard = new GenerateDiffFileWizard(resources[0]);
		wizard.setWindowTitle(title);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.setMinimumPageSize(350, 250);
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

}
