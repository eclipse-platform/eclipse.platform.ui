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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.wizards.ReplaceFeatureVersionWizard;
import org.eclipse.update.operations.*;

public class ReplaceVersionAction extends Action {
	
	private IFeature currentFeature;
	private IFeature[] features;
	
	public ReplaceVersionAction(String text) {
		super(text);
	}
	
	public void setCurrentFeature(IFeature feature) {
		currentFeature = feature;
	}
	
	public void setFeatures(IFeature[] features) {
		this.features = features;
	}
		
	public void run() {
		if (currentFeature == null || features == null || features.length < 2)
			return;
			
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			ErrorDialog.openError(
					UpdateUI.getActiveWorkbenchShell(),
					null,
					null,
					status);
			return;
		}
		
		ReplaceFeatureVersionWizard wizard = new ReplaceFeatureVersionWizard(currentFeature, features);
		WizardDialog dialog = new WizardDialog(UpdateUI.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getActiveWorkbenchShell().getText());
		dialog.getShell().setSize(400,400);
		dialog.open();
	}


}
