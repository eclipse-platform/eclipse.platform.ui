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

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * Configure a feature.
 * ConfigOperation
 */
public class InstallOperation extends PendingOperation {
	private static final String KEY_OLD = "OperationsManager.error.old";

	private IInstallConfiguration config;
	private IConfiguredSite targetSite;
	private IFeatureReference[] optionalFeatures;
	private IVerificationListener verifier;

	public InstallOperation(IFeature feature) {
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

		if (optionalFeatures == null)
			targetSite.install(feature, verifier, pm);
		else
			targetSite.install(feature, optionalFeatures, verifier, pm);

		if (oldFeature != null && isOptionalDelta()) {
			boolean oldSuccess = unconfigure(oldFeature);
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

		markProcessed();
		UpdateManager.getOperationsManager().fireObjectChanged(this, null);
	}

	public void undo() throws CoreException {
	}

	private void ensureUnique() throws CoreException {
		boolean patch = false;
		if (targetSite == null)
			targetSite = feature.getSite().getCurrentConfiguredSite();
		IImport[] imports = feature.getImports();
		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch()) {
				patch = true;
				break;
			}
		}
		// Only need to check features that patch other features.
		if (!patch)
			return;
		IFeature localFeature =
			UpdateManager.getLocalFeature(targetSite, feature);
		ArrayList oldFeatures = new ArrayList();
		// First collect all older active features that
		// have the same ID as new features marked as 'unique'.
		UpdateManager.collectOldFeatures(localFeature, targetSite, oldFeatures);
		// Now unconfigure old features to enforce uniqueness
		for (int i = 0; i < oldFeatures.size(); i++) {
			IFeature oldFeature = (IFeature) oldFeatures.get(i);
			unconfigure(oldFeature);
		}
	}

	private boolean unconfigure(IFeature feature) throws CoreException {
		IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
		if (site != null) {
			PatchCleaner2 cleaner = new PatchCleaner2(site, feature);
			boolean result = site.unconfigure(feature);
			cleaner.dispose();
			return result;
		}
		return false;
	}
}
