package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.*;

/**
 * 
 */
public class ConfigurationSite implements IConfigurationSite {
	
	private ISite site;
	private IConfigurationPolicy policy;
	
	/**
	 * Constructor
	 */
	public ConfigurationSite(ISite site, IConfigurationPolicy policy){
		this.site = site;
		this.policy = policy;	
	}

	/*
	 * @see IConfigurationSite#getSite()
	 */
	public ISite getSite() {
		return null;
	}

	/*
	 * @see IConfigurationSite#getPolicy()
	 */
	public IConfigurationPolicy getPolicy() {
		return null;
	}

	/*
	 * @see IConfigurationSite#setPolicy(IConfigurationPolicy)
	 */
	public void setPolicy(IConfigurationPolicy policy) {
	}

	/*
	 * @see IConfigurationSite#getConfiguredFeatures()
	 */
	public IFeatureReference[] getConfiguredFeatures() {
		return null;
	}

	/*
	 * @see IConfigurationSite#isConfigured(IFeatureReference)
	 */
	public boolean isConfigured(IFeatureReference feature) {
		return false;
	}

	/*
	 * @see IConfigurationSite#isInstallSite()
	 */
	public boolean isInstallSite() {
		return false;
	}

	/*
	 * @see IConfigurationSite#configure(IFeatureReference)
	 */
	public void configure(IFeatureReference feature) {
	}

	/*
	 * @see IConfigurationSite#unconfigure(IFeatureReference)
	 */
	public void unconfigure(IFeatureReference feature) {
	}

}

