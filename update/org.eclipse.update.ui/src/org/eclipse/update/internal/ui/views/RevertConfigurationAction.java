/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.operations.*;

public class RevertConfigurationAction extends Action {
	public RevertConfigurationAction(String text) {
		super(text);
	}
	
	public void run() {
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			ErrorDialog.openError(
					UpdateUI.getActiveWorkbenchShell(),
					null,
					null,
					status);
			return;
		}
		
		RevertConfigurationWizard wizard = new RevertConfigurationWizard();
		WizardDialog dialog = new WizardDialog(UpdateUI.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getActiveWorkbenchShell().getText());
		dialog.getShell().setSize(600,500);
		dialog.open();
	}
}
