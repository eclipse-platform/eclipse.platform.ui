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

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;

public class FindUpdatesAction extends Action {

	private IFeature feature;
	private Shell shell;

	public FindUpdatesAction(Shell shell, String text) {
		super(text);
		this.shell = shell;
	}
	
	public void setFeature(IFeature feature) {
		this.feature = feature;
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
		
		// If current config is broken, confirm with the user to continue
		if (OperationsManager.getValidator().validateCurrentState() != null &&
				!confirm(UpdateUI.getString("Actions.brokenConfigQuestion")))
			return;
			
		
		IFeature [] features=null;
		if (feature!=null)
			features = new IFeature[] { feature };
		final UpdateSearchRequest searchRequest = UpdateUtils.createNewUpdatesRequest(features);

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				InstallWizard wizard = new InstallWizard(searchRequest);
				WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
				dialog.create();
				dialog.getShell().setText(UpdateUI.getString("FindUpdatesAction.updates")); //$NON-NLS-1$
				SWTUtil.setDialogSize(dialog, 600, 500);
				if (dialog.open() == IDialogConstants.OK_ID)
					UpdateUI.requestRestart(wizard.isRestartNeeded());				
			}
		});
	}
	
	private boolean confirm(String message) {
		return MessageDialog.openConfirm(
			UpdateUI.getActiveWorkbenchShell(),
			UpdateUI.getString("FeatureStateAction.dialogTitle"), //$NON-NLS-1$
			message);
	}
}
