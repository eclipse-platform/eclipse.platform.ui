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
import org.eclipse.team.internal.ccvs.core.resources.RemoteModule;
import org.eclipse.team.internal.ccvs.ui.ResizableWizardDialog;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutIntoWizard;

public class CheckoutIntoAction extends CVSAction {

	/**
	 * @see CVSAction#execute(IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final ICVSRemoteFolder remoteFolder = getSelectedRemoteFolders()[0];
		CheckoutIntoWizard wizard = new CheckoutIntoWizard(remoteFolder);
		WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
		dialog.setMinimumPageSize(350, 250);
		dialog.open();
	}

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		if (getSelectedRemoteFolders().length != 1) return false;
		if (folders[0] instanceof RemoteModule) return false;
		return true;
	}
}
