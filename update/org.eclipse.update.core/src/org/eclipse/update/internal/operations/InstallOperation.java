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
package org.eclipse.update.internal.operations;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;

/**
 * Configure a feature.
 * ConfigOperation
 */
public class InstallOperation
	extends FeatureOperation
	implements IInstallFeatureOperation {
	private static final String KEY_OLD = "OperationsManager.error.old"; //$NON-NLS-1$

	private IFeatureReference[] optionalFeatures;
	private IFeature[] unconfiguredOptionalFeatures;
	private IVerificationListener verifier;

	/**
	 * Constructor
	 * @param site
	 * @param feature
	 * @param optionalFeatures optional features to install. If null, the operation will install them all (if any)
	 * @param unconfiguredOptionalElements optional features unconfigured before the operation. They should remain unconfigured after the install.
	 * @param verifier
	 */
	public InstallOperation(
		IConfiguredSite site,
		IFeature feature,
		IFeatureReference[] optionalFeatures,
		IFeature[] unconfiguredOptionalElements,
		IVerificationListener verifier) {
		super(site, feature);
		IFeature[] installed = UpdateUtils.getInstalledFeatures(feature);
		if (installed.length > 0)
			this.oldFeature = installed[0];
		this.unconfiguredOptionalFeatures = unconfiguredOptionalElements;
		this.optionalFeatures = optionalFeatures;
		this.verifier = verifier;
	}

	public IFeatureReference[] getOptionalFeatures() {
		return optionalFeatures;
	}

	public boolean execute(IProgressMonitor pm, IOperationListener listener)
		throws CoreException {

		boolean reinstall = false;
		if (oldFeature != null
			&& feature.getVersionedIdentifier().equals(
				oldFeature.getVersionedIdentifier()))
			reinstall = true;

		setOptionalFeatures();

		if (optionalFeatures == null)
			targetSite.install(feature, verifier, pm);
		else
			targetSite.install(feature, optionalFeatures, verifier, pm);

		if (!reinstall) {

			if (oldFeature != null) { //&& isOptionalDelta()) {
				preserveOptionalState();

				boolean oldSuccess = unconfigure(oldFeature, null); // pick any site containing old feature
				if (!oldSuccess) {
					IInstallConfiguration config = SiteManager.getLocalSite().getCurrentConfiguration();
					if (!UpdateUtils.isNestedChild(config, oldFeature)) {
						// "eat" the error if nested child
						String message =
							Policy.bind(
								KEY_OLD,
								oldFeature.getLabel());
						IStatus status =
							new Status(
								IStatus.ERROR,
								UpdateUtils.getPluginId(),
								IStatus.OK,
								message,
								null);
						throw new CoreException(status);
					}
				}
			}

//			if (oldFeature == null) {
//				ensureUnique();
//			}
		}
		return true;
	}

	private void preserveOptionalState() {
		if (unconfiguredOptionalFeatures == null)
			return;

		for (int i = 0; i < unconfiguredOptionalFeatures.length; i++) {
			try {
				// Get the feature that matches the original unconfigured ones.
				IFeature localFeature =
					UpdateUtils.getLocalFeature(
						targetSite,
						unconfiguredOptionalFeatures[i]);
				if (localFeature != null)
					targetSite.unconfigure(localFeature);

			} catch (CoreException e) {
				// Ignore this - we will leave with it
			}
		}
	}
	
	private void setOptionalFeatures() {
		try {
			// Ensure optional features are correctly set
			if (optionalFeatures == null && UpdateUtils.hasOptionalFeatures(feature) ) {
				JobRoot jobRoot = new JobRoot(this);
				IInstallConfiguration config = SiteManager.getLocalSite().getCurrentConfiguration();
				HashSet set = new HashSet();
				boolean update = oldFeature != null;
				boolean patch = UpdateUtils.isPatch(feature);
				FeatureHierarchyElement[] elements = jobRoot.getElements();
				for (int i = 0; i < elements.length; i++) {
					elements[i].addCheckedOptionalFeatures(update, patch, config, set);
				}
				optionalFeatures = new IFeatureReference[set.size()];
				set.toArray(optionalFeatures);
				unconfiguredOptionalFeatures = jobRoot.getUnconfiguredOptionalFeatures(config, targetSite);
			}
		} catch (CoreException e) {
			UpdateUtils.logException(e);
		}
	}
}
