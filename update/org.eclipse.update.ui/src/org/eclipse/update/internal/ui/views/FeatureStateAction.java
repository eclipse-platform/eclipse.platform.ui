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

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.operations.*;

public class FeatureStateAction extends Action {
	private ConfiguredFeatureAdapter adapter;

	public void setFeature(ConfiguredFeatureAdapter adapter) {
		this.adapter = adapter;
		if (adapter.isConfigured()) {
			setText(UpdateUI.getString("FeatureStateAction.disable")); //$NON-NLS-1$
		} else {
			setText(UpdateUI.getString("FeatureStateAction.enable")); //$NON-NLS-1$
		}
	}

	public void run() {
		try {
			if (adapter == null || !confirm(adapter.isConfigured()))
				return;

			boolean isConfigured = adapter.isConfigured();
			IOperation toggleOperation =
				isConfigured
					? (IOperation)OperationsManager
						.getOperationFactory()
						.createUnconfigOperation(
						adapter.getInstallConfiguration(),
						adapter.getConfiguredSite(),
						adapter.getFeature(null))
					: OperationsManager
						.getOperationFactory()
						.createConfigOperation(
						adapter.getInstallConfiguration(),
						adapter.getConfiguredSite(),
						adapter.getFeature(null));

			boolean restartNeeded = toggleOperation.execute(null, null);
			if (restartNeeded)
				UpdateUI.requestRestart();

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

	private boolean confirm(boolean isConfigured) {
		String message =
			isConfigured
				? UpdateUI.getString("FeatureStateAction.disableQuestion") //$NON-NLS-1$
				: UpdateUI.getString("FeatureStateAction.EnableQuestion"); //$NON-NLS-1$
		return MessageDialog.openConfirm(
			UpdateUI.getActiveWorkbenchShell(),
			UpdateUI.getString("FeatureStateAction.dialogTitle"), //$NON-NLS-1$
			message);
	}

}
