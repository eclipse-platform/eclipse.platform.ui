/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.core.InstallRegistry;
import org.eclipse.update.internal.operations.OperationFactory;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.model.ConfiguredFeatureAdapter;
import org.eclipse.update.operations.IFeatureOperation;
import org.eclipse.update.operations.IOperation;
import org.eclipse.update.operations.OperationsManager;

public class UninstallFeaturesAction extends FeatureAction {

	private ConfiguredFeatureAdapter[] adapters;

	public UninstallFeaturesAction( Shell shell, String text) {
		super(shell, text);
		setWindowTitle(UpdateUIMessages.FeatureUninstallAction_dialogTitle);
	}

	
	public void run() {
		try {
			IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
			if (status != null)
				throw new CoreException(status);
			
			if (adapters == null || !confirm(UpdateUIMessages.FeatureUninstallAction_uninstallQuestion)) 
				return;

			// If current config is broken, confirm with the user to continue
			if (OperationsManager.getValidator().validateCurrentState() != null &&
					!confirm(UpdateUIMessages.Actions_brokenConfigQuestion)) 
				return;
			
			IFeature[] features = new IFeature[adapters.length];
			IConfiguredSite[] configuredSites = new IConfiguredSite[adapters.length];
			for( int i = 0; i < adapters.length; i++) {
				features[i] = adapters[i].getFeature(null);
				configuredSites[i] = adapters[i].getConfiguredSite();
			}

			IOperation uninstallFeaturesOperation =
				((OperationFactory)OperationsManager.getOperationFactory()).createUninstallFeaturesOperation( configuredSites, features);

			boolean restartNeeded = uninstallFeaturesOperation.execute(null, null);
			UpdateUI.requestRestart(restartNeeded);

		} catch (CoreException e) {
			ErrorDialog.openError(shell, null, null, e.getStatus());
		} catch (InvocationTargetException e) {
			// This should not happen
			UpdateUtils.logException(e.getTargetException());
		}
	}


	public void setSelection(IStructuredSelection selection) {
		
		this.adapters = (ConfiguredFeatureAdapter[]) selection.toList().toArray(new ConfiguredFeatureAdapter[selection.size()]);
		setText(UpdateUIMessages.FeatureUninstallAction_uninstall); 
	}
	
	public boolean canExecuteAction() {
		
		if (adapters == null || adapters.length == 0)
			return false;
		
		for( int i = 0; i < adapters.length; i++) {
			if (adapters[i].isConfigured())
				return false;
		
			try {
				// check for pending changes (e.g. if the feature has just been disabled)
				IFeatureOperation pendingOperation = OperationsManager.findPendingOperation(adapters[i].getFeature(null));
				if (pendingOperation != null)
					return false;

				if (InstallRegistry.getInstance().get("feature_"+adapters[i].getFeature(null).getVersionedIdentifier()) == null) //$NON-NLS-1$
					return false;
			} catch (CoreException e) {
				return false;
			}
		}
				
		return true;	
	}
}
