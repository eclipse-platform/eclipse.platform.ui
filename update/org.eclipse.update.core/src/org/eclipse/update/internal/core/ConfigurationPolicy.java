package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.update.core.IConfigurationPolicy;
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
		
		switch (policy) {
			case IPlatformConfiguration.ISitePolicy.USER_INCLUDE :
				if (featureReferences.isEmpty()){
					result = new IFeatureReference[featureReferences.size()];
					featureReferences.toArray(result);
				}
				break;

			case IPlatformConfiguration.ISitePolicy.USER_EXCLUDE :
						
				break;

			case IPlatformConfiguration.ISitePolicy.SITE_INCLUDE :
				// no features, let teh remote site decide						
				break;

		}

		return result;
	}

	/**
	 * Adds a feature reference
	 */
	/*package*/ void addFeatureReference(IFeatureReference feature){
		if (featureReferences==null) featureReferences = new ArrayList(0);
		featureReferences.add(feature);		
	}

}

