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
import org.eclipse.update.core.*;

/**
 * Configure a feature.
 * ConfigOperation
 */
public class InstallOperation extends PendingOperation {
	private static final String KEY_OLD = "OperationsManager.error.old";

	private IFeatureReference[] optionalFeatures;
	private IVerificationListener verifier;

	public InstallOperation(IFeature feature) {
		super(feature, INSTALL);

		IFeature[] installed = UpdateManager.getInstalledFeatures(feature);
		if (installed.length > 0)
			this.oldFeature = installed[0];
	}

	public void setOptionalFeatures(IFeatureReference[] optionalFeatures) {
		this.optionalFeatures = optionalFeatures;
	}

	public void setVerificationListener(IVerificationListener verifier) {
		this.verifier = verifier;
	}

	public boolean execute(IProgressMonitor pm) throws CoreException {

		if (optionalFeatures == null)
			targetSite.install(feature, verifier, pm);
		else
			targetSite.install(feature, optionalFeatures, verifier, pm);

		if (oldFeature != null && isOptionalDelta()) {
			boolean oldSuccess = UpdateManager.getOperationsManager().unconfigure(config, oldFeature);
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
			UpdateManager.getOperationsManager().ensureUnique(config, feature, targetSite);
		}
		
		return true;
	}

	public void undo() throws CoreException {
	}
}
