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
package org.eclipse.update.standalone;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.operations.*;

public class UpdateCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private IFeature feature;
	private int installCount = 0;

	public UpdateCommand(
		String featureId,
		String version,
		String fromSite,
		String toSite) {
		try {
			IConfiguredSite[] sites = config.getConfiguredSites();

			// Get remote site
			ISite remoteSite =
				SiteManager.getSite(new URL(URLDecoder.decode(fromSite)), null);
			if (remoteSite == null) {
				System.out.println("Cannot find site " + fromSite);
				return;
			}

			// Get feature
			feature =
				getFeatureFromServer(
					remoteSite,
					new VersionedIdentifier(featureId, version));
			if (feature == null) {
				System.out.println(
					"Cannot find feature " + feature + " on site " + fromSite);
				return;
			}

			// Get site to install to
			if (toSite != null) {
				URL toSiteURL = new URL(toSite);
				if (SiteManager.getSite(toSiteURL, null) == null) {
					System.out.println(
						"Cannot find site to install to: " + toSite);
					return;
				}
				targetSite =
					SiteManager
						.getSite(toSiteURL, null)
						.getCurrentConfiguredSite();
			}
			if (targetSite == null) {
				for (int i = 0; i < sites.length; i++) {
					if (sites[i].isProductSite()) {
						targetSite = sites[i];
						break;
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public boolean isSuccessfulInstall() {
		return installCount > 0; // or == selectedJobs.length
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean run() {
		final IInstallFeatureOperation[] operations =
			new IInstallFeatureOperation[] {
				OperationsManager
					.getOperationFactory()
					.createInstallOperation(
					config,
					targetSite,
					feature,
					null,
					null,
					null)};
		installCount = 0;

		// Check for duplication conflicts
		JobTargetSite jobTargetSite = new JobTargetSite();
		jobTargetSite.job = operations[0];
		jobTargetSite.targetSite = targetSite;
		ArrayList conflicts =
			DuplicateConflictsValidator.computeDuplicateConflicts(
				new JobTargetSite[] { jobTargetSite },
				config);
		if (conflicts != null) {
			System.out.println("duplicate conflicts");
		}

		IFeature latestOldFeature = findLatestOldFeature(feature);
		if (latestOldFeature != null) {
			if (latestOldFeature
				.getVersionedIdentifier()
				.equals(feature.getVersionedIdentifier())) {
				// Already installed.
				System.out.println(
					"Feature "
						+ feature
						+ " version "
						+ feature.getVersionedIdentifier().getVersion()
						+ " is already installed.");
				return false;
			}
			if (latestOldFeature
				.getVersionedIdentifier()
				.getVersion()
				.isGreaterThan(feature.getVersionedIdentifier().getVersion())) {
				System.out.println(
					"A feature with a newer version "
						+ latestOldFeature.getVersionedIdentifier().getVersion()
						+ " is already installed.");
				return false;
			}
		}

		IOperation installOperation =
			OperationsManager
				.getOperationFactory()
				.createBatchInstallOperation(
				operations);
		try {
			return installOperation.execute(null, UpdateCommand.this);
		} catch (CoreException e) {
			System.out.println(e.getStatus().getMessage());
			return false;
		} catch (InvocationTargetException e) {
			System.out.println(e.getTargetException());
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#afterExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean afterExecute(IOperation operation, Object data) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#beforeExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean beforeExecute(IOperation operation, Object data) {
		return true;
	}

	private static IFeature getFeatureFromServer(
		ISite site,
		VersionedIdentifier vid) {
		// Locate remote site, find the optional feature
		// and install it

		IFeatureReference[] refs = site.getFeatureReferences();
		return findFeature(vid, refs);
	}

	private static IFeature findFeature(
		VersionedIdentifier vid,
		IFeatureReference[] refs) {

		for (int i = 0; i < refs.length; i++) {
			IFeatureReference ref = refs[i];
			try {
				VersionedIdentifier refVid = ref.getVersionedIdentifier();
				if (refVid.equals(vid)) {
					return ref.getFeature(null);
				}
				// Try children
				IFeature feature = ref.getFeature(null);
				IFeatureReference[] irefs =
					feature.getIncludedFeatureReferences();
				IFeature result = findFeature(vid, irefs);
				if (result != null)
					return result;
			} catch (CoreException e) {
			}
		}
		return null;
	}

	private static IFeature findLatestOldFeature(IFeature feature) {
		IFeature[] oldFeatures = UpdateManager.getInstalledFeatures(feature);
		if (oldFeatures.length == 0)
			return null;

		IFeature latest = null;
		for (int i = 0; i < oldFeatures.length; i++) {
			IFeature curr = oldFeatures[i];
			if (latest == null
				|| curr.getVersionedIdentifier().getVersion().isGreaterThan(
					latest.getVersionedIdentifier().getVersion())) {
				latest = curr;
			}
		}
		return latest;
	}
}
