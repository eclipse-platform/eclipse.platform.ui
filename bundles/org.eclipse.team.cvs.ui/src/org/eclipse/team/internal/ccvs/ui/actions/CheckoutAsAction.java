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
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.HasProjectMetaFileOperation;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutAsWizard;
import org.eclipse.ui.PlatformUI;

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
		final ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		try {
			CheckoutAsWizard wizard = new CheckoutAsWizard(folders, allowProjectConfiguration(folders));
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	protected boolean allowProjectConfiguration(ICVSRemoteFolder[] folders) throws CVSException, InterruptedException {
		if (folders.length != 1) return false;
		return !HasProjectMetaFileOperation.hasMetaFile(getShell(), folders[0], PlatformUI.getWorkbench().getActiveWorkbenchWindow());	
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
