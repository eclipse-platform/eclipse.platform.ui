package org.eclipse.update.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.ModelObject;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * 
 */
public class ConfigurationPolicyModel extends ModelObject {

	
	

	private int policy;
	private List /* of FeatureReferenceModel */configuredFeatureReferences;
	private List /* of FeatureReferenceModel */unconfiguredFeatureReferences;

	/**
	 * Constructor for ConfigurationPolicyModel.
	 */
	public ConfigurationPolicyModel() {
		super();
		configuredFeatureReferences = new ArrayList();
		unconfiguredFeatureReferences = new ArrayList();		
	}

	/**
	 * Copy Constructor for ConfigurationPolicyModel.
	 */
	public ConfigurationPolicyModel(ConfigurationPolicyModel configPolicy) {
		super();
		this.policy = configPolicy.getPolicy();
		configuredFeatureReferences = new ArrayList();
		configuredFeatureReferences.addAll(Arrays.asList(configPolicy.getConfiguredFeaturesModel()));
		unconfiguredFeatureReferences = new ArrayList();
		unconfiguredFeatureReferences.addAll(Arrays.asList(configPolicy.getUnconfiguredFeaturesModel()));
	}


	
	/**
	 * @since 2.0
	 */
	public int getPolicy() {
		return policy;
	}

	/**
	 * Sets the policy.
	 * @param policy The policy to set
	 */
	public void setPolicy(int policy) {
		assertIsWriteable();
		this.policy = policy;
	}

	/**
	 * @since 2.0
	 */
	public FeatureReferenceModel[] getConfiguredFeaturesModel() {
		if (configuredFeatureReferences==null || configuredFeatureReferences.isEmpty())
			return new FeatureReferenceModel[0];
		return (FeatureReferenceModel[]) configuredFeatureReferences.toArray(arrayTypeFor(configuredFeatureReferences));
	}

	/**
	 * @since 2.0
	 */
	public FeatureReferenceModel[] getUnconfiguredFeaturesModel() {
	if (unconfiguredFeatureReferences==null || unconfiguredFeatureReferences.isEmpty())
			return new FeatureReferenceModel[0];			
		return (FeatureReferenceModel[]) unconfiguredFeatureReferences.toArray(arrayTypeFor(unconfiguredFeatureReferences));		
	}

	/**
	 * 
	 */
	private boolean remove(FeatureReferenceModel feature, List list) {
		URL featureURL = feature.getURL();
		boolean found = false;
		Iterator iter = list.iterator();
		while (iter.hasNext() && !found) {
			FeatureReferenceModel element = (FeatureReferenceModel) iter.next();
			if (sameURL(element.getURL(),featureURL)) {
				list.remove(element);
				found = true;
			}
		}
		return found;
	}

	/**
	 * returns an array of string corresponding to plugins file
	 */
	/*package*/

	
	/**
	 * 
	 */
	private void add(FeatureReferenceModel feature, List list) {
		URL featureURL = feature.getURL();
		boolean found = false;
		Iterator iter = list.iterator();
		while (iter.hasNext() && !found) {
			FeatureReferenceModel element = (FeatureReferenceModel) iter.next();
			if (sameURL(element.getURL(),featureURL)) {
				found = true;
			}
		}

		if (!found) {
			list.add(feature);
		} else {
			UpdateManagerPlugin.warn("Feature Reference :"+feature+" already part of the list.",new Exception());
		}
	}

	/**
	 * adds a feature in the configuredReference list
	 * also used by the parser to avoid creating another activity
	 */
	public void addConfiguredFeatureReference(FeatureReferenceModel feature) {
		assertIsWriteable();
		
		if (configuredFeatureReferences == null)
			this.configuredFeatureReferences = new ArrayList();
		if (!configuredFeatureReferences.contains(feature)){
			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION){
				UpdateManagerPlugin.debug("Configuring "+feature.getURLString());
			}
			this.add(feature, configuredFeatureReferences);
		}	

		// when user configure a feature,
		// we have to remove it from unconfigured feature if it exists
		// because the user doesn't know...
		if (unconfiguredFeatureReferences != null) {
			boolean success = remove(feature, unconfiguredFeatureReferences);
			if (!success)
				UpdateManagerPlugin.warn("Unable to remove from unconfigured: "+feature.getURLString());			
		}

	}

	/**
	 * adds a feature in the list
	 * also used by the parser to avoid creating another activity
	 */
	public void addUnconfiguredFeatureReference(FeatureReferenceModel feature) {
		assertIsWriteable();
		if (unconfiguredFeatureReferences == null)
			this.unconfiguredFeatureReferences = new ArrayList();
		if (!unconfiguredFeatureReferences.contains(feature)){
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_CONFIGURATION){
				UpdateManagerPlugin.debug("Unconfiguring "+feature.getURLString());
			}
			this.add(feature, unconfiguredFeatureReferences);
		}	

		// an unconfigured feature is always from a configured one no ?
		// unless it was parsed right ?
		if (configuredFeatureReferences != null) {
			boolean success = remove(feature, configuredFeatureReferences);
			if (!success)
				UpdateManagerPlugin.warn("Unable to remove from configured: "+feature.getURLString());				
		}
	}

	/**
	 * removes a feature from any list
	 */
	public void removeFeatureReference(FeatureReferenceModel feature) {
		assertIsWriteable();
		if (unconfiguredFeatureReferences!=null){
			boolean success = remove(feature, unconfiguredFeatureReferences);
			if (!success)
				UpdateManagerPlugin.warn("Unable to remove from unconfigured: "+feature.getURLString());							
		}

		if (configuredFeatureReferences != null) {
			boolean success = remove(feature, configuredFeatureReferences);
			if (!success)
				UpdateManagerPlugin.warn("Unable to remove from unconfigured: "+feature.getURLString());							
		}
	}
	
	
	/**
	 * Sets the configuredFeatureReferences.
	 * @param configuredFeatureReferences The configuredFeatureReferences to set
	 */
	protected void setConfiguredFeatureReferences(IFeatureReference[] featureReferences) {
		configuredFeatureReferences = new ArrayList();
		configuredFeatureReferences.addAll(Arrays.asList(featureReferences));

	}

	
	/**
	 * Sets the unconfiguredFeatureReferences.
	 * @param unconfiguredFeatureReferences The unconfiguredFeatureReferences to set
	 */
	protected void setUnconfiguredFeatureReferences(IFeatureReference[] featureReferences) {
		unconfiguredFeatureReferences = new ArrayList();
		unconfiguredFeatureReferences.addAll(Arrays.asList(featureReferences));
	}

	/*
	 * Compares two URL for equality
	 * Return false if one of them is null
	 */
	private boolean sameURL(URL url1, URL url2) {
		
		if (url1 == null)
			return false;
		if (url1.equals(url2))
			return true;

		// check if URL are file: URL as we may
		// have 2 URL pointing to the same featureReference
		// but with different representation
		// (i.e. file:/C;/ and file:C:/)
		if (!"file".equalsIgnoreCase(url1.getProtocol()))
			return false;
		if (!"file".equalsIgnoreCase(url2.getProtocol()))
			return false;

		File file1 = new File(url1.getFile());
		File file2 = new File(url2.getFile());

		if (file1 == null)
			return false;

		return (file1.equals(file2));
	}
}