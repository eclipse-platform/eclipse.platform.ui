package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.IConfigurationPolicy;
import org.eclipse.update.core.IFeatureReference;

/**
 * 
 */
public class ConfigurationPolicy implements IConfigurationPolicy {

	private int policy;

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
	public IFeatureReference[] getFilteredFeatures(IFeatureReference[] features) {
		return null;
	}

}

