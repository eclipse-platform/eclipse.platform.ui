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
public class InstallOperation
	extends FeatureOperation
	implements IInstallFeatureOperation {
	private static final String KEY_OLD = "OperationsManager.error.old";

	private IFeatureReference[] optionalFeatures;
	private IFeature[] unconfiguredOptionalElements;
	private IVerificationListener verifier;

	public InstallOperation(
		IInstallConfiguration config,
		IConfiguredSite site,
		IFeature feature,
		IFeatureReference[] optionalFeatures,
		IFeature[] unconfiguredOptionalElements,
		IVerificationListener verifier) {
		super(config, site, feature);
		IFeature[] installed = UpdateManager.getInstalledFeatures(feature);
		if (installed.length > 0)
			this.oldFeature = installed[0];
		this.unconfiguredOptionalElements = unconfiguredOptionalElements;
		this.optionalFeatures = optionalFeatures;
		this.verifier = verifier;
	}

	public IFeatureReference[] getOptionalFeatures() {
		return optionalFeatures;
	}

	public boolean execute(IProgressMonitor pm, IOperationListener listener) throws CoreException {

		if (optionalFeatures == null)
			targetSite.install(feature, verifier, pm);
		else
			targetSite.install(feature, optionalFeatures, verifier, pm);

		if (oldFeature != null) { //&& isOptionalDelta()) {
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

		if (oldFeature != null //&& !operations[i].isOptionalDelta()
			&& unconfiguredOptionalElements != null) {
			preserveOptionalState();
		}

		return true;
	}

	private void preserveOptionalState() {
		for (int i = 0; i < unconfiguredOptionalElements.length; i++) {
			try {
				targetSite.unconfigure(unconfiguredOptionalElements[i]);
			} catch (CoreException e) {
				// Ignore this - we will leave with it
			}
		}
	}
}