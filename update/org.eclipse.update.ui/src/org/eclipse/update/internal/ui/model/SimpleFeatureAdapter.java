/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.core.runtime.CoreException;

/**
 * @version 	1.0
 * @author
 */
public class SimpleFeatureAdapter extends FeatureAdapter {
	protected IFeature feature;
	public SimpleFeatureAdapter(IFeature feature) {
		this.feature = feature;
	}
	
	public IFeature getFeature() throws CoreException {
		return feature;
	}
	
	public URL getURL() {
		return feature.getURL();
	}
	
	public ISite getSite() {
		return feature.getSite();
	}
	
	public IFeatureAdapter[] getIncludedFeatures() {
		try {
			IFeatureReference[] included = getFeature().getIncludedFeatureReferences();
			SimpleFeatureAdapter[] result =
				new SimpleFeatureAdapter[included.length];
			for (int i = 0; i < included.length; i++) {
				result[i] =
					new SimpleFeatureAdapter(included[i].getFeature());
				result[i].setIncluded(true);
			}
			return result;
		} catch (CoreException e) {
			return new IFeatureAdapter[0];
		}
	}
}