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

public class InstallCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private IFeature feature;
	private int installCount = 0;

	public InstallCommand(
		String featureId,
		String version,
		String fromSite,
		String toSite) {
		try {
			IConfiguredSite[] sites = config.getConfiguredSites();

			// Get remote site
			ISite remoteSite = SiteManager.getSite(new URL(URLDecoder.decode(fromSite)), null);
			if (remoteSite == null) {
				System.out.println("Cannot find site " + fromSite);
				return;
			}

			// Get feature
			ISiteFeatureReference[] featureRefs =
				remoteSite.getFeatureReferences();
			for (int i = 0; i < featureRefs.length; i++) {
				if (featureRefs[i]
					.getFeature(null)
					.getVersionedIdentifier()
					.getIdentifier()
					.equals(featureId)
					&& featureRefs[i]
						.getFeature(null)
						.getVersionedIdentifier()
						.getVersion()
						.toString()
						.equals(
						version)) {
					feature = featureRefs[i].getFeature(null);
					break;
				}
			}
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
			new IInstallFeatureOperation[] { OperationsManager.getOperationFactory().createInstallOperation(config, targetSite,feature,null, null, null)};
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

		IOperation installOperation =
			OperationsManager
				.getOperationFactory()
				.createBatchInstallOperation(
				operations);
		try {
			return installOperation.execute(null, InstallCommand.this);
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

}
