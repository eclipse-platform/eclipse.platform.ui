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

/**
 * Configure a feature.
 * ConfigOperation
 */
public class UninstallOperation extends PendingOperation {
	private static final String KEY_OLD = "OperationsManager.error.old";

	private IInstallConfiguration config;
	private IConfiguredSite targetSite;
	private IFeatureReference[] optionalFeatures;
	private IVerificationListener verifier;

	public UninstallOperation(IFeature feature) {
		super(feature, INSTALL);

		IFeature[] installed = UpdateManager.getInstalledFeatures(feature);
		if (installed.length > 0)
			this.oldFeature = installed[0];
	}

	public void setInstallConfiguration(IInstallConfiguration config) {
		this.config = config;
	}

	public void setTargetSite(IConfiguredSite targetSite) {
		this.targetSite = targetSite;
	}

	public void setOptionalFeatures(IFeatureReference[] optionalFeatures) {
		this.optionalFeatures = optionalFeatures;
	}

	public void setVerificationListener(IVerificationListener verifier) {
		this.verifier = verifier;
	}

	public void execute(IProgressMonitor pm) throws CoreException {

		//find the  config site of this feature
//			IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
//			if (site != null) {
//				site.remove(feature, monitor);
//			} else {
//				// we should do something here
//				String message =
//					UpdateManager.getFormattedMessage(
//						KEY_UNABLE,
//						feature.getLabel());
//				IStatus status =
//					new Status(
//						IStatus.ERROR,
//						UpdateManager.getPluginId(),
//						IStatus.OK,
//						message,
//						null);
//				throw new CoreException(status);
//			}


		markProcessed();
		UpdateManager.getOperationsManager().fireObjectChanged(this, null);
	}

	public void undo() throws CoreException {
	}
}
