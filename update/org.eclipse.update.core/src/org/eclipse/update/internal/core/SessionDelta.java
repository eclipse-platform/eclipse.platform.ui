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
package org.eclipse.update.internal.core;
import java.io.File;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.ModelObject;

/**
 *
 */
public class SessionDelta extends ModelObject implements ISessionDelta {

	private Date date;
	private List featureReferences;
	private File file;
	private int process;
	private boolean deleted = false;

	/**
	 * Constructor for SessionDelta.
	 */
	public SessionDelta() {
		super();
		process = ENABLE;
		deleted = false;
	}

	/**
	 * @see ISessionDelta#getFeatureReferences()
	 */
	public IFeatureReference[] getFeatureReferences() {
		if (featureReferences == null)
			return new IFeatureReference[0];

		return (IFeatureReference[]) featureReferences.toArray(arrayTypeFor(featureReferences));
	}

	/**
	 * @see ISessionDelta#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @see ISessionDelta#process(IProgressMonitor)
	 */
	public void process(IProgressMonitor pm) throws CoreException {
		if (featureReferences.isEmpty())
			return;
		IFeatureReference[] selected = new IFeatureReference[featureReferences.size()];
		featureReferences.toArray(selected);
		process(selected, pm);
	}

	/*
	 * 
	 */
	public void delete() {
		if (deleted) {
			UpdateCore.warn("Attempt to delete an already deleted session delta:" + file);
			return;
		}

		// remove the file from the file system
		if (file != null) {
			UpdateManagerUtils.removeFromFileSystem(file);
			UpdateCore.warn("Removing SessionDelta:" + file);
		} else {
			UpdateCore.warn("Unable to remove SessionDelta. File is null");
		}

		deleted = true;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 *
	 */
	public void addReference(IFeatureReference reference) {
		if (featureReferences == null)
			featureReferences = new ArrayList();
		featureReferences.add(reference);
	}

	/*
	 * 
	 */
	public void setCreationDate(Date date) {
		this.date = date;
	}

	/*
	 * Sets the file. 
	 * We will remove the file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/*@
	 * @see ISessionDelta#getType()
	 */
	public int getType() {
		return process;
	}

	private void createInstallConfiguration() throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.cloneCurrentConfiguration();
		config.setLabel(Utilities.format(config.getCreationDate()));
		localSite.addConfiguration(config);
	}

	private void saveLocalSite() throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		localSite.save();
	}

	/*
	 * return true if this feature should be configured A feature should be
	 * configure if it has the highest version across all configured features
	 * with the same identifier 
	 * 
	 * Disable all other lower versions of the same feature in all the
	 * configured sites
	 */
	private boolean enable(IFeature newlyConfiguredFeatures) throws CoreException {

		ILocalSite siteLocal = SiteManager.getLocalSite();
		IInstallConfiguration currentConfiguration = siteLocal.getCurrentConfiguration();
		IConfiguredSite[] configuredSites;
		IFeatureReference[] configuredFeaturesRef;
		IFeature feature;
		configuredSites = currentConfiguration.getConfiguredSites();
		for (int i = 0; i < configuredSites.length; i++) {
			configuredFeaturesRef = configuredSites[i].getConfiguredFeatures();
			for (int j = 0; j < configuredFeaturesRef.length; j++) {
				try {
					feature = configuredFeaturesRef[j].getFeature(null);
					int result = compare(newlyConfiguredFeatures, feature);
					if (result != 0) {
						if (result == 1) {
							ConfiguredSite cSite = (ConfiguredSite) configuredSites[i];
							cSite.unconfigure(feature);
							if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
								UpdateCore.debug("Found an old version of the feature to disable:" + feature);
						}
						if (result == 2) {
							//	we found at least one higher version, do not enable this feature
							if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
								UpdateCore.debug("Found an old version of the feature with a higher version:" + feature);
							return false;
						}
					}
				} catch (CoreException e) {
					UpdateCore.warn(null, e);
				}
			}
		}
		// feature not found, or no better version found, configure it then
		return true;
	}

	/**
	 * compare two feature references
	 * returns 0 if the feature are different
	 * returns 1 if the version of feature 1 is greater than the version of feature 2
	 * returns 2 if opposite
	 */
	private int compare(IFeature feature1, IFeature feature2) throws CoreException {

		// TRACE
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
			UpdateCore.debug("Compare: " + feature1 + " && " + feature2);
		}

		if (feature1 == null)
			return 0;

		if (feature1 == null || feature2 == null) {
			return 0;
		}

		VersionedIdentifier id1 = feature1.getVersionedIdentifier();
		VersionedIdentifier id2 = feature2.getVersionedIdentifier();

		if (id1 == null || id2 == null) {
			return 0;
		}

		if (id1.getIdentifier() != null && id1.getIdentifier().equals(id2.getIdentifier())) {
			PluginVersionIdentifier version1 = id1.getVersion();
			PluginVersionIdentifier version2 = id2.getVersion();
			if (version1 != null) {
				if (version1.isGreaterThan(version2)) {
					return 1;
				} else {
					return 2;
				}
			} else {
				return 2;
			}
		}
		return 0;
	};

	/**
	 * @see org.eclipse.update.configuration.ISessionDelta#process(org.eclipse.update.core.IFeatureReference, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void process(IFeatureReference[] selected, IProgressMonitor pm) throws CoreException {

		createInstallConfiguration();
		if (pm == null)
			pm = new NullProgressMonitor();

		// process all feature references to configure
		// find the configured site each feature belongs to
		if (process == ENABLE) {
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
				UpdateCore.warn("ENABLE SESSION DELTA");
			if (featureReferences != null && featureReferences.size() > 0) {
				// manage ProgressMonitor
				int nbFeatures = featureReferences.size();
				pm.beginTask(Policy.bind("SessionDelta.EnableFeatures"), nbFeatures);
				// since 2.0.2 ISite.getConfiguredSite()
				// find the configuredSite that maintains this featureReference
				// configure the feature
				IFeatureReference ref = null;
				IConfiguredSite configSite = null;
				IFeature featureToConfigure = null;
				for (int i = 0; i < selected.length; i++) {
					ref = (IFeatureReference) selected[i];

					try {
						featureToConfigure = ref.getFeature(null);
					} catch (CoreException e) {
						UpdateCore.warn(null, e);
					}

					if (featureToConfigure != null) {
						pm.worked(1);
						configSite = ref.getSite().getCurrentConfiguredSite();
						try {
							// make sure only the latest version of the configured features
							// is configured across sites [16502]
							if (enable(featureToConfigure)) {
								configSite.configure(featureToConfigure);
								if (UpdateCore.isPatch(featureToConfigure))
									disablePatchedFeature(featureToConfigure, configSite);
							} else {
								configSite.unconfigure(featureToConfigure);
							}
						} catch (CoreException e) {
							// if I cannot configure one,
							//then continue with others
							UpdateCore.warn("Unable to configure feature:" + featureToConfigure, e);
						}
					} else {
						UpdateCore.warn("Unable to configure null feature:" + ref, null);
					}
				}
			}
		}

		delete();
		saveLocalSite();
	}

	/*
	 * Disable any patched features 
	 * 
	 * This occurs in the same configuredSite as patch is always installed in the same site as the feature
	 * it patches
	 */
	private void disablePatchedFeature(IFeature patchToEnable, IConfiguredSite configSite) {
		try {
			IIncludedFeatureReference[] children = patchToEnable.getIncludedFeatureReferences();
			IFeature child = null;
			for (int i = 0; i < children.length; i++) {
				try {
					child = children[i].getFeature(true, configSite, null);
				} catch (CoreException e) {
					//nothing
				}
				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_CONFIGURATION)
					UpdateCore.debug("Checking if children :" + child + " of efix " + patchToEnable + " should be enabled");
				if (child != null) {
					if (UpdateCore.isPatch(child))
						disablePatchedFeature(child, configSite);
					else
						try {
							enable(child);
						} catch (CoreException e) {
							if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_WARNINGS)
								UpdateCore.warn("Unable to enable child " + child, e);
						}
				}
			}
		} catch (CoreException e) {
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_WARNINGS)
				UpdateCore.warn("Unable to retrieve children of patch " + patchToEnable, e);
		}
	}
}
