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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.HasProjectMetaFileOperation;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutAsWizard;

/**
 * Add a remote resource to the workspace. Current implementation:
 * -Works only for remote folders
 * -Does not prompt for project name; uses folder name instead
 */
public class CheckoutAsAction extends AddToWorkspaceAction {
	
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		CheckoutAsWizard wizard = new CheckoutAsWizard(getTargetPart(), folders, allowProjectConfiguration(folders));
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();
	}
	
	protected boolean allowProjectConfiguration(ICVSRemoteFolder[] folders) throws InvocationTargetException, InterruptedException {
		if (folders.length != 1) return false;
		return !HasProjectMetaFileOperation.hasMetaFile(getTargetPart(), folders[0]);	
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length > 0;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CheckoutAsAction.checkoutFailed"); //$NON-NLS-1$
	}
}
