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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.model.ConfiguredFeatureAdapter;
import org.eclipse.update.operations.IOperation;
import org.eclipse.update.operations.OperationsManager;

public class FeatureStateAction extends FeatureAction {

	private ConfiguredFeatureAdapter adapter;

    public FeatureStateAction(Shell shell, String text) {
        super(shell, text);
        setWindowTitle(UpdateUIMessages.FeatureStateAction_dialogTitle);
    }
    
	public void setSelection(IStructuredSelection selection) {
		
		this.adapter = (ConfiguredFeatureAdapter) selection.getFirstElement();
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
			ErrorDialog.openError(shell,
                    null, null, e.getStatus());
		} catch (InvocationTargetException e) {
			// This should not happen
			UpdateUtils.logException(e.getTargetException());
		}
	}
	
	public boolean canExecuteAction() {
		return true;
	}

}
