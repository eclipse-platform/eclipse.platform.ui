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
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.operations.*;

public class DisableCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private IFeature feature;

	public DisableCommand(
		String featureId,
		String version,
		String toSite,
		String verifyOnly)
		throws Exception {

		super(verifyOnly);

		try {
			IConfiguredSite[] sites = getConfiguration().getConfiguredSites();

			// Get site containing the feature to disable
			if (toSite != null) {
				URL toSiteURL = new File(toSite).toURL();
				if (SiteManager.getSite(toSiteURL, null) == null) {
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
				UpdateUtils.searchSite(featureId, targetSite, true);
			if (features == null || features.length == 0) {
				throw new Exception(
					"There are no configured features with id:" + featureId);
			}
			if (version == null || version.trim().length() == 0)
				feature = features[0]; // pick the first feature
			else
				for (int i = 0; features != null && i < features.length; i++) {
					if (features[i]
						.getVersionedIdentifier()
						.getVersion()
						.toString()
						.equals(version)) {
						feature = features[i];
						break;
					}
				}
			if (feature == null) {
				throw new Exception(
					"Cannot find configured feature "
						+ featureId
						+ " with version "
						+ version);
			}

		} catch (MalformedURLException e) {
			throw e;
		} catch (CoreException e) {
			throw e;
		}
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean run() {
		if (isVerifyOnly()) {
			IStatus status =
				OperationsManager.getValidator().validatePendingUnconfig(
					feature);
			return status == null;
		}

		final IUnconfigFeatureOperation configOperation =
			OperationsManager.getOperationFactory().createUnconfigOperation(
				getConfiguration(),
				targetSite,
				feature);

		try {
			return configOperation.execute(null, null);
		} catch (CoreException e) {
			StandaloneUpdateApplication.exceptionLogged();
			UpdateCore.log(e);
			return false;
		} catch (InvocationTargetException e) {
			StandaloneUpdateApplication.exceptionLogged();
			UpdateCore.log(e);
			return false;
		}
	}

}
