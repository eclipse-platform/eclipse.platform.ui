/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public class ConfiguredFeatureAdapter
	extends SimpleFeatureAdapter
	implements IConfiguredFeatureAdapter {
	private IConfiguredSiteAdapter adapter;
	private boolean configured;
	private boolean updated;

	public ConfiguredFeatureAdapter(
		IConfiguredSiteAdapter adapter,
		IFeature feature,
		boolean configured,
		boolean updated,
		boolean optional) {
		super(feature, optional);
		this.adapter = adapter;
		this.configured = configured;
		this.updated = updated;
	}

	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (object instanceof ConfiguredFeatureAdapter) {
			try {
				ConfiguredFeatureAdapter ad = (ConfiguredFeatureAdapter) object;
				return ad.getConfiguredSite().equals(getConfiguredSite())
					&& ad.getFeature(null).equals(getFeature(null));
			} catch (CoreException e) {
			}
		}
		return false;
	}

	public IConfiguredSite getConfiguredSite() {
		return adapter.getConfiguredSite();
	}
	public IInstallConfiguration getInstallConfiguration() {
		return adapter.getInstallConfiguration();
	}
	public boolean isConfigured() {
		return configured;
	}

	public boolean isUpdated() {
		return updated;
	}
	public IFeatureAdapter[] getIncludedFeatures(IProgressMonitor monitor) {
		try {
			IIncludedFeatureReference[] included =
				getFeature(null).getIncludedFeatureReferences();
			ConfiguredFeatureAdapter[] result =
				new ConfiguredFeatureAdapter[included.length];
			if (monitor == null)
				monitor = new NullProgressMonitor();
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
			subMonitor.beginTask("", included.length); //$NON-NLS-1$

			for (int i = 0; i < included.length; i++) {
				IIncludedFeatureReference fref = included[i];
				IFeature feature;
				boolean childConfigured = configured;
				boolean updated = false;
				try {
					feature = fref.getFeature(
							new SubProgressMonitor(subMonitor, 1));
					childConfigured =
						adapter.getConfiguredSite().isConfigured(feature);
					///*
					PluginVersionIdentifier refpid =
						fref.getVersionedIdentifier().getVersion();
					PluginVersionIdentifier fpid =
						feature.getVersionedIdentifier().getVersion();
					updated = !refpid.equals(fpid);
					//*/
				} catch (CoreException e) {
					feature = new MissingFeature(getFeature(null), fref);
					childConfigured = false;
				}

				result[i] =
					new ConfiguredFeatureAdapter(
						adapter,
						feature,
						childConfigured,
						updated,
						fref.isOptional());
				result[i].setIncluded(true);
			}
			return result;
		} catch (CoreException e) {
			return new IFeatureAdapter[0];
		}
	}
}
