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
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.operations.*;

/**
 * Command to uninstall a feature. The feature must be disabled before uninstalling it.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public class UninstallCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private IFeature feature;

	public UninstallCommand(
		String featureId,
		String version,
		String toSite,
		String verifyOnly)
		throws Exception {

		super(verifyOnly);

		try {
			IConfiguredSite[] sites = getConfiguration().getConfiguredSites();

			// Get site that contains the feature to uninstall
			if (toSite != null) {
				URL toSiteURL = new File(toSite).toURL();
				if (SiteManager.getSite(toSiteURL, null) == null) {
					throw new Exception(Policy.bind("Standalone.noSite") + toSite); //$NON-NLS-1$
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
				throw new Exception(Policy.bind("Standalone.noFeatures1", featureId)); //$NON-NLS-1$
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
				throw new Exception(Policy.bind("Standalone.noFeatures2", featureId, version)); //$NON-NLS-1$
			}

		} catch (MalformedURLException e) {
			throw e;
		} catch (CoreException e) {
			throw e;
		}
	}

	/**
	 */
	public boolean run(IProgressMonitor monitor) {
		// check if the config file has been modifed while we were running
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			UpdateCore.log(status);
			return false;
		}
		if (InstallRegistry.getInstance().get("feature_"+ feature.getVersionedIdentifier()) == null) { //$NON-NLS-1$
			StandaloneUpdateApplication.exceptionLogged();
			UpdateCore.log(Utilities.newCoreException(Policy.bind("UninstallCommand.featureNotInstalledByUM", feature.toString()),null)); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
							
		if (isVerifyOnly()) {
			// if reached this point, it is safe to uninstall
			return true;
		}

		final IUninstallFeatureOperation uninstallOperation =
			OperationsManager.getOperationFactory().createUninstallOperation(
				targetSite,
				feature);

		try {
			uninstallOperation.execute(monitor, this);
			return true;
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
