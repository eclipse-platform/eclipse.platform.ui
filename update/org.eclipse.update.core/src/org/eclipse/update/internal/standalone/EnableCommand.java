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
package org.eclipse.update.internal.standalone;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.operations.*;

public class EnableCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private IFeature feature;

	public EnableCommand(
		String featureId,
		String version,
		String toSite,
		String verifyOnly)
		throws Exception {

		super(verifyOnly);

		try {
			IConfiguredSite[] sites = getConfiguration().getConfiguredSites();

			// Get site to enable to
			if (toSite != null) {
				URL toSiteURL = new File(toSite).toURL();
				if (SiteManager.getSite(toSiteURL, null) == null) {
					System.out.println("Cannot find site " + toSite);
					throw new Exception("Cannot find site " + toSite);
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

			IFeature[] features =
				UpdateUtils.searchSite(featureId, targetSite, false);
			if (features == null || features.length == 0) {
				System.out.println(
					"There are no unconfigured features with id:" + featureId);
				throw new Exception(
					"There are no unconfigured features with id:" + featureId);
			}
			if (version == null || version.trim().length() == 0)
				feature = features[0]; // pick the first feature
			else
				for (int i = 0; features != null && i < features.length; i++) {
					if (features[i]
						.getVersionedIdentifier()
						.getVersion()
						.toString()
						.equals(version)
						&& !targetSite.isConfigured(features[i])) {
						feature = features[i];
						break;
					}
				}
			if (feature == null) {
				System.out.println(
					"Cannot find unconfigured feature "
						+ featureId
						+ " with version "
						+ version);
				throw new Exception(
					"Cannot find unconfigured feature "
						+ featureId
						+ " with version "
						+ version);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw e;
		} catch (CoreException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean run() {
		if (isVerifyOnly()) {
			IStatus status =
				OperationsManager.getValidator().validatePendingConfig(feature);
			return status == null;
		}

		final IConfigFeatureOperation configOperation =
			OperationsManager.getOperationFactory().createConfigOperation(
				getConfiguration(),
				targetSite,
				feature);

		try {
			return configOperation.execute(null, null);
		} catch (CoreException e) {
			System.out.println(e.getStatus().getMessage());
			return false;
		} catch (InvocationTargetException e) {
			System.out.println(e.getTargetException());
			return false;
		}
	}

}
