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

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.operations.*;

public class UninstallFeatureAction extends Action {
	private ConfiguredFeatureAdapter adapter;

	public UninstallFeatureAction(String text) {
		super(text);
	}

	public void run() {
		try {
			IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
			if (status != null)
				throw new CoreException(status);
			
			if (adapter == null || !confirm(UpdateUI.getString("FeatureUninstallAction.uninstallQuestion"))) //$NON-NLS-1$))
				return;

			// If current config is broken, confirm with the user to continue
			if (OperationsManager.getValidator().validateCurrentState() != null &&
					!confirm(UpdateUI.getString("Actions.brokenConfigQuestion"))) //$NON-NLS-1$
				return;

			IOperation uninstallOperation =
				(IOperation) OperationsManager
					.getOperationFactory()
					.createUninstallOperation(
					adapter.getConfiguredSite(),
					adapter.getFeature(null));

			boolean restartNeeded = uninstallOperation.execute(null, null);
			UpdateUI.requestRestart(restartNeeded);

		} catch (CoreException e) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				e.getStatus());
		} catch (InvocationTargetException e) {
			// This should not happen
			UpdateUtils.logException(e.getTargetException());
		}
	}

	private boolean confirm(String message) {
		return MessageDialog.openConfirm(
			UpdateUI.getActiveWorkbenchShell(),
			UpdateUI.getString("FeatureUninstallAction.dialogTitle"), //$NON-NLS-1$
			message);
	}

	public void setFeature(ConfiguredFeatureAdapter adapter) {
		this.adapter = adapter;
		setText(UpdateUI.getString("FeatureUninstallAction.uninstall")); //$NON-NLS-1$
	}

	public boolean canUninstall() {
		if (adapter == null)
			return false;
		
//		if (adapter.isConfigured())
//			return false;
			
		try {
			if (InstallRegistry.getInstance().get("feature_"+adapter.getFeature(null).getVersionedIdentifier()) == null) //$NON-NLS-1$
				return false;
		} catch (CoreException e) {
			return false;
		}
				
		return true;	
	}
}
