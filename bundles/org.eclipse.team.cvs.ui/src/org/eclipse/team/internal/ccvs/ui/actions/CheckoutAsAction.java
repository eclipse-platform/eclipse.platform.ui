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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.operations.HasProjectMetaFileOperation;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutAsWizard;

public class CheckoutAsAction extends CVSAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#execute(org.eclipse.jface.action.IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		CheckoutAsWizard wizard = new CheckoutAsWizard(getTargetPart(), folders, allowProjectConfiguration(folders));
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();
	}
	
	/*
	 * Return true if the remote project does not have a .project file
	 * so that the checkout wizard will give the option to launch
	 * the New Project wizard
	 */
	protected boolean allowProjectConfiguration(ICVSRemoteFolder[] folders) throws InvocationTargetException, InterruptedException {
		if (folders.length != 1) return false;
		return !HasProjectMetaFileOperation.hasMetaFile(getTargetPart(), folders[0]);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length > 0;
	}
}
