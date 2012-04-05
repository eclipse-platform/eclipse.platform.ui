/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;


public class InstallOptionalFeatureAction extends Action {
	private MissingFeature missingFeature;
	private Shell shell;

	public InstallOptionalFeatureAction(Shell shell, String text) {
		super(text);
		this.shell = shell;
	}

	public void setFeature(MissingFeature feature) {
		this.missingFeature = feature;
	}

	public void run() {
		if (missingFeature == null)
			return;
		
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			ErrorDialog.openError(shell, null, null, status);
			return;
		}
		
		// If current config is broken, confirm with the user to continue
		if (OperationsManager.getValidator().validateCurrentState() != null &&
				!confirm(UpdateUIMessages.Actions_brokenConfigQuestion)) 
			return;
			
		
		VersionedIdentifier vid = missingFeature.getVersionedIdentifier();
		URL originatingURL = missingFeature.getOriginatingSiteURL();

		UpdateSearchScope scope = new UpdateSearchScope();
		scope.addSearchSite(originatingURL.toString(), originatingURL, null);

		OptionalFeatureSearchCategory category = new OptionalFeatureSearchCategory();
		category.addVersionedIdentifier(vid);
		final UpdateSearchRequest searchRequest =
			new UpdateSearchRequest(category, scope);

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				openWizard(searchRequest);
			}
		});
	}
	private void openWizard(UpdateSearchRequest searchRequest) {
		if (InstallWizard.isRunning()) {
			MessageDialog.openInformation(shell, UpdateUIMessages.InstallWizard_isRunningTitle, UpdateUIMessages.InstallWizard_isRunningInfo);
			return;
		}
		InstallWizard wizard = new InstallWizard(searchRequest);
		WizardDialog dialog = new ResizableInstallWizardDialog(shell, wizard, UpdateUIMessages.FeaturePage_optionalInstall_title);
		dialog.create();
		dialog.open();
	}
	
	private boolean confirm(String message) {
		return MessageDialog.openConfirm(
			shell,
			UpdateUIMessages.FeatureStateAction_dialogTitle, 
			message);
	}
}
