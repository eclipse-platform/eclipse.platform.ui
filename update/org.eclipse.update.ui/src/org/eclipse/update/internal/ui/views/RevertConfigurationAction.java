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
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.action.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.wizards.*;

public class RevertConfigurationAction extends Action {
	public RevertConfigurationAction(String text) {
		super(text);
	}
	
	public void run() {
		RevertConfigurationWizard wizard = new RevertConfigurationWizard();
		WizardDialog dialog = new WizardDialog(UpdateUI.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getActiveWorkbenchShell().getText());
		dialog.getShell().setSize(500,500);
		dialog.open();
	}
}
