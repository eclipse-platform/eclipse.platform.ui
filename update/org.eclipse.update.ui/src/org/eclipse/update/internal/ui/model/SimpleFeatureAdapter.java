/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

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
	
	public IFeatureAdapter [] getIncludedFeatures() {
		return new IFeatureAdapter[0];
	}
}