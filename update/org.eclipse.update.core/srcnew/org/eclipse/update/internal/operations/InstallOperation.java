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
package org.eclipse.update.internal.operations;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.operations.*;

/**
 * Configure a feature.
 * ConfigOperation
 */
public class InstallOperation extends SingleOperation implements IInstallOperation {
	private static final String KEY_OLD = "OperationsManager.error.old";

	private IFeatureReference[] optionalFeatures;
	private FeatureHierarchyElement2[] optionalElements;
	private IVerificationListener verifier;

	public InstallOperation(IInstallConfiguration config, IConfiguredSite site, IFeature feature, FeatureHierarchyElement2[] optionalElements,IFeatureReference[] optionalFeatures, IVerificationListener verifier, IOperationListener listener) {
		super(config, site, feature, listener);
		IFeature[] installed = UpdateManager.getInstalledFeatures(feature);
				if (installed.length > 0)
					this.oldFeature = installed[0];
		this.optionalElements = optionalElements;
		this.optionalFeatures = optionalFeatures;
		this.verifier = verifier;
	}

	public FeatureHierarchyElement2[] getOptionalElements() {
		return optionalElements;
	}
	
	public IFeatureReference[] getOptionalFeatures() {
		return optionalFeatures;
	}

	public boolean execute(IProgressMonitor pm) throws CoreException {

		if (optionalFeatures == null)
			targetSite.install(feature, verifier, pm);
		else
			targetSite.install(feature, optionalFeatures, verifier, pm);

		if (oldFeature != null ){ //&& isOptionalDelta()) {
			boolean oldSuccess = unconfigure(config, oldFeature);
			if (!oldSuccess) {
				if (!UpdateManager.isNestedChild(config, oldFeature)) {
					// "eat" the error if nested child
					String message =
						UpdateManager.getFormattedMessage(
							KEY_OLD,
							oldFeature.getLabel());
					IStatus status =
						new Status(
							IStatus.ERROR,
							UpdateManager.getPluginId(),
							IStatus.OK,
							message,
							null);
					throw new CoreException(status);
				}
			}
		}
		if (oldFeature == null) {
			ensureUnique();
		}

		if (oldFeature != null
			//&& !operations[i].isOptionalDelta()
			&& optionalElements != null) {
			preserveOptionalState(config, targetSite,
				UpdateManager.isPatch(feature),
				optionalElements);
		}
		
		return true;
	}

	private void preserveOptionalState(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		boolean patch,
		FeatureHierarchyElement2[] optionalElements) {
		for (int i = 0; i < optionalElements.length; i++) {
			FeatureHierarchyElement2[] children =
				optionalElements[i].getChildren(true, patch, config);
			preserveOptionalState(config, targetSite, patch, children);
			if (!optionalElements[i].isEnabled(config)) {
				IFeature newFeature = optionalElements[i].getFeature();
				try {
					IFeature localFeature =
						UpdateManager.getLocalFeature(targetSite, newFeature);
					if (localFeature != null)
						targetSite.unconfigure(localFeature);
				} catch (CoreException e) {
					// Ignore this - we will leave with it
				}
			}
		}
	}
}
