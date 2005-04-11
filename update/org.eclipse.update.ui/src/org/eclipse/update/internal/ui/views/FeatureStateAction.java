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
    private ConfigurationView parent;

    public FeatureStateAction(ConfigurationView parent) {
        this.parent = parent;
    }
    
	public void setFeature(ConfiguredFeatureAdapter adapter) {
		this.adapter = adapter;
		if (adapter.isConfigured()) {
			setText(UpdateUIMessages.FeatureStateAction_disable); 
		} else {
			setText(UpdateUIMessages.FeatureStateAction_enable); 
		}
	}

	public void run() {
		try {
			if (adapter == null)
				return;
			
			IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
			if (status != null)
				throw new CoreException(status);
			
			boolean isConfigured = adapter.isConfigured();
			// Ask user to confirm the operation
			String message =
				isConfigured
					? UpdateUIMessages.FeatureStateAction_disableQuestion
					: UpdateUIMessages.FeatureStateAction_EnableQuestion; 

			if (!confirm(message))
				return;

			// If current config is broken, confirm with the user to continue
			if (OperationsManager.getValidator().validateCurrentState() != null &&
					!confirm(UpdateUIMessages.Actions_brokenConfigQuestion)) 
				return;
			
			IOperation toggleOperation =
				isConfigured
					? (IOperation)OperationsManager
						.getOperationFactory()
						.createUnconfigOperation(
						adapter.getConfiguredSite(),
						adapter.getFeature(null))
					: OperationsManager
						.getOperationFactory()
						.createConfigOperation(
						adapter.getConfiguredSite(),
						adapter.getFeature(null));

			boolean restartNeeded = toggleOperation.execute(null, null);
			UpdateUI.requestRestart(restartNeeded);

		} catch (CoreException e) {
			ErrorDialog.openError(parent.getConfigurationWindow().getShell(),
                    null, null, e.getStatus());
		} catch (InvocationTargetException e) {
			// This should not happen
			UpdateUtils.logException(e.getTargetException());
		}
	}

	private boolean confirm(String message) {
		return MessageDialog.openConfirm(
            parent.getConfigurationWindow().getShell(),
			UpdateUIMessages.FeatureStateAction_dialogTitle, 
			message);
	}

}
