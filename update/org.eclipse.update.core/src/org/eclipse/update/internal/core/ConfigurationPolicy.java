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
	private List configuredFeatureReferences;
	private List unconfiguredFeatureReferences;

	/**
	 * Constructor for ConfigurationPolicy.
	 */
	public ConfigurationPolicy(int policy) {
		super();
		this.policy = policy;
	}

	/**
	 * Copy Constructor for ConfigurationPolicy.
	 */
	public ConfigurationPolicy(IConfigurationPolicy configPolicy) {
		super();
		this.policy = configPolicy.getPolicy();
		configuredFeatureReferences = new ArrayList(0);
		configuredFeatureReferences.addAll(Arrays.asList(configPolicy.getConfiguredFeatures()));
		unconfiguredFeatureReferences = new ArrayList(0);
		unconfiguredFeatureReferences.addAll(Arrays.asList(configPolicy.getUnconfiguredFeatures()));
		
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
		if (configuredFeatureReferences != null && !configuredFeatureReferences.isEmpty()) {
			result = new IFeatureReference[configuredFeatureReferences.size()];
			configuredFeatureReferences.toArray(result);
		}
		return result;
	}

	/**
	 * Adds a feature reference
	 * is package because is called by the parser 
	 * The parse just adds, without knowing if it is configured or not
	 */
	/*
	 * @see IConfigurationSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		if (configuredFeatureReferences!=null && !configuredFeatureReferences.isEmpty()){
			result = new IFeatureReference[configuredFeatureReferences.size()];
			configuredFeatureReferences.toArray(result);
		}
		return result;
	}

	/*
	 * @see IConfigurationPolicy#getUnconfiguredFeatures()
	 */
	public IFeatureReference[] getUnconfiguredFeatures() {
		IFeatureReference[] result = new IFeatureReference[0];
		if (unconfiguredFeatureReferences!=null && !unconfiguredFeatureReferences.isEmpty()){
			result = new IFeatureReference[unconfiguredFeatureReferences.size()];
			unconfiguredFeatureReferences.toArray(result);
		}
		return result;

	}

	/*
	 * @see IConfigurationSite#isConfigured(IFeatureReference)
	 */
	public boolean isConfigured(IFeatureReference feature) {
		boolean result = false;

				// return true if the feature is part of the configured list
				Iterator iter = configuredFeatureReferences.iterator();
				boolean found = false;
				String featureURLString = feature.getURL().toExternalForm();
				while (iter.hasNext() && !result) {
					IFeatureReference element = (IFeatureReference) iter.next();
					if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureURLString)) {
						result = true;
					}
				}
		return result;
	}

	/**
	 * adds teh feature to teh list of features if the policy is USER_INCLUDE
	 */
	/*package*/
	void configure(IFeatureReference feature) throws CoreException {
		if (configuredFeatureReferences == null)
			configuredFeatureReferences = new ArrayList(0);

		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_CONFIGURE);
		activity.setLabel(feature.getFeature().getIdentifier().toString());
		activity.setDate(new Date());
			
		addConfiguredFeatureReference(feature);
		
		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);		
	}

	/**
	 * adds teh feature to teh list of features if the policy is USER_EXCLUDE
	 */
	/*package*/
	void unconfigure(IFeatureReference feature) throws CoreException {
		if (unconfiguredFeatureReferences == null)
			unconfiguredFeatureReferences = new ArrayList(0);
			
		//Start UOW ?
		ConfigurationActivity activity = new ConfigurationActivity(IActivity.ACTION_UNCONFIGURE);
		activity.setLabel(feature.getFeature().getIdentifier().toString());
		activity.setDate(new Date());
			
		addUnconfiguredFeatureReference(feature);
		
		// everything done ok
		activity.setStatus(IActivity.STATUS_OK);
		((InstallConfiguration) SiteManager.getLocalSite().getCurrentConfiguration()).addActivity(activity);
		
	}

	/**
	 * returns an array of string corresponding to plugin
	 */
	/*package*/
	String[] getPlugins() throws CoreException {
		String[] result = new String[0];
		if (configuredFeatureReferences != null && !configuredFeatureReferences.isEmpty()) {
			List pluginsString = new ArrayList(0);

			Iterator iter = configuredFeatureReferences.iterator();
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


	/**
	 * 
	 */	
	private void remove(IFeatureReference feature, List list){
		String featureURLString = feature.getURL().toExternalForm();
		boolean found = false;
		Iterator iter = list.iterator();
		while (iter.hasNext() && !found) {
			IFeatureReference element = (IFeatureReference) iter.next();
				if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureURLString)) {
					list.remove(element);
					found = true;
			}
		}
	}
	
	/**
	 * 
	 */	
	private void add(IFeatureReference feature, List list){
		String featureURLString = feature.getURL().toExternalForm();
		boolean found = false;
		Iterator iter = list.iterator();
		while (iter.hasNext() && !found) {
			IFeatureReference element = (IFeatureReference) iter.next();
				if (element.getURL().toExternalForm().trim().equalsIgnoreCase(featureURLString)) {
					found = true;
			}
		}
		
		if (!found){
			list.add(feature);
		}
	}
	

	/**
	 * adds a feature in the configuredReference list
	 * also used by the parser to avoid creating another activity
	 */
	void addConfiguredFeatureReference(IFeatureReference feature) {
		if (configuredFeatureReferences==null) configuredFeatureReferences = new ArrayList(0);
		add(feature,configuredFeatureReferences);
		
			// when user configure a feature,
		// we have to remove it from unconfigured feature if it exists
		// because the user doesn't know...
		if (unconfiguredFeatureReferences != null){
			remove(feature,unconfiguredFeatureReferences);
		}
		
	}

	/**
	 * adds a feature in teh list
	 * also used by the parser to avoid creating another activity
	 */
	void addUnconfiguredFeatureReference(IFeatureReference feature) {
		if (unconfiguredFeatureReferences==null) unconfiguredFeatureReferences = new ArrayList(0);
		add(feature,unconfiguredFeatureReferences);
		
		// an unconfigured feature is always from a configured one no ?
		// unless it was parsed right ?
		if (configuredFeatureReferences != null){
			remove(feature,configuredFeatureReferences);
		}
	}


}