package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeatureReference;

/**
 * 
 */
public class ConfigurationPolicy implements IConfigurationPolicy {

	private int policy;
	private List featureReferences;

	/**
	 * Constructor for ConfigurationPolicy.
	 */
	public ConfigurationPolicy(int policy) {
		super();
		this.policy = policy;
	}

	/*
	 * @see IConfigurationPolicy#getPolicy()
	 */
	public int getPolicy() {
		return policy;
	}

	/*
	 * @see IConfigurationPolicy#getFilteredFeatures(IFeatureReference[])
	 */
	public IFeatureReference[] getFilteredFeatures(IFeatureReference[] featuresToFilter) {

		IFeatureReference[] result = new IFeatureReference[0];
		if (featureReferences != null && !featureReferences.isEmpty()) {
			result = new IFeatureReference[featureReferences.size()];
			featureReferences.toArray(result);
		}
		return result;
	}

	/**
	 * Adds a feature reference
	 * is package because is called by the parser 
	 * The parse just adds, without knowing if it is configured or not
	 */
	/*package*/
	void addFeatureReference(IFeatureReference feature) {
		if (featureReferences == null)
			featureReferences = new ArrayList(0);
		featureReferences.add(feature);
	}

	/*
	 * @see IConfigurationSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		// FIXME:
		if (policy == IPlatformConfiguration.ISitePolicy.USER_INCLUDE) {
			result = getFilteredFeatures(null);
		}
		return result;
	}

	/*
	 * @see IConfigurationPolicy#getUnconfiguredFeatures()
	 */
	public IFeatureReference[] getUnconfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		// FIXME:
		if (policy == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			result = getFilteredFeatures(null);
		}
		return result;

	}

	/*
	 * @see IConfigurationSite#isConfigured(IFeatureReference)
	 */
	public boolean isConfigured(IFeatureReference feature) {
		boolean result = false;
		switch (policy) {
			case IPlatformConfiguration.ISitePolicy.USER_INCLUDE :
				// return true if the feature is part of the configured list
				Iterator iter = featureReferences.iterator();
				boolean found = false;
				String featureURLString = feature.getURL().toExternalForm();
				while (iter.hasNext() && !result) {
					IFeatureReference element = (IFeatureReference) iter.next();
					if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureURLString)) {
						result = true;
					}
				}
				break;
			case IPlatformConfiguration.ISitePolicy.USER_EXCLUDE :
				// return true if the feature is NOT part of the list
				iter = featureReferences.iterator();
				result = true;
				featureURLString = feature.getURL().toExternalForm();
				while (iter.hasNext() && result) {
					IFeatureReference element = (IFeatureReference) iter.next();
					if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureURLString)) {
						// we found it in teh list of not configured
						result = false;
					}
				}
				break;
		}
		return false;
	}

	/**
	 * adds teh feature to teh list of features if the policy is USER_INCLUDE
	 */
	/*packahge*/ void configure(IFeatureReference feature) throws CoreException {
		if (policy == IPlatformConfiguration.ISitePolicy.USER_INCLUDE) {
			//Start UOW ?

			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
			activity.setLabel(feature.getURL().toExternalForm());
			activity.setDate(new Date());
			addFeatureReference(feature);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);

			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);

		}
	}

	/**
	 * adds teh feature to teh list of features if the policy is USER_EXCLUDE
	 */
	/*package*/ void unconfigure(IFeatureReference feature) throws CoreException {
		if (policy == IPlatformConfiguration.ISitePolicy.USER_EXCLUDE) {
			//Start UOW ?
			ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
			activity.setLabel(feature.getURL().toExternalForm());
			activity.setDate(new Date());
			addFeatureReference(feature);

			// everything done ok
			activity.setStatus(IActivity.STATUS_OK);
			((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);
		}
	}

	/**
	 * returns an array of string corresponding to plugin
	 */
	/*package*/
	String[] getPlugins() throws CoreException {
		String[] result = new String[0];
		if (featureReferences != null && !featureReferences.isEmpty()) {
			List pluginsString = new ArrayList(0);

			Iterator iter = featureReferences.iterator();
			while (iter.hasNext()) {
				IFeatureReference element = (IFeatureReference) iter.next();
				IFeature feature = element.getFeature();
				IPluginEntry[] entries = feature.getPluginEntries();
				for (int index = 0; index < entries.length; index++) {
					IPluginEntry entry = entries[index];
					pluginsString.add(entry.getIdentifier().toString());
				}
			}

			if (!pluginsString.isEmpty()) {
				result = new String[pluginsString.size()];
				pluginsString.toArray(result);
			}

		}
		return result;
	}

}