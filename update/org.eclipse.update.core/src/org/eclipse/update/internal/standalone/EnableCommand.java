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
import java.lang.reflect.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.operations.*;

public  class EnableCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private IFeature feature;

	public EnableCommand(
		String featureId,
		String version,
		String toSite,
		String verifyOnly) {
			
		super(verifyOnly);
		
		try {		
			IConfiguredSite[] sites = config.getConfiguredSites();

			// Get site to enable to
			if (toSite != null) {
				URL toSiteURL = new URL(toSite);
				if (SiteManager.getSite(toSiteURL, null) == null) {
					System.out.println(
						"Cannot find site " + toSite);
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
			
			IFeature[] features = UpdateUtils.searchSite(featureId, targetSite, false);
			if (features == null || features.length == 0) {
				System.out.println("There are no configured features with id:" + featureId);
				return;
			}
			if (version == null || version.trim().length() == 0)
				feature = features[0]; // pick the first feature
			else 
				for (int i=0; features!= null && i<features.length; i++) {
					if (features[i].getVersionedIdentifier().getVersion().toString().equals(version)  && !targetSite.isConfigured(features[i])) {
						feature = features[i];
						break;
					}
				}
			if (feature == null) {
				System.out.println("Cannot find unconfigured feature " + featureId + " with version " + version);
				return;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean run() {
		if (isVerifyOnly()) {
			IStatus status =
				UpdateUtils.getValidator().validatePendingConfig(feature);
			return status == null;
		}
		
		final IConfigFeatureOperation configOperation =
			OperationsManager.getOperationFactory().createConfigOperation(config, targetSite,feature);

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
