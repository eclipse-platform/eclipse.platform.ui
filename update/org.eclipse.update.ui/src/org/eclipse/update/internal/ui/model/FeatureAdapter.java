/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.CoreException;

/**
 * @version 	1.0
 * @author
 */
public abstract class FeatureAdapter extends UIModelObject implements IFeatureAdapter {
	private boolean included=false;

	/*
	 * @see IFeatureAdapter#getInstallConfiguration()
	 */
	public IInstallConfiguration getInstallConfiguration() {
		return null;
	}
	
	public boolean isIncluded() {
		return included;
	}
	
	protected void setIncluded(boolean included) {
		this.included = included;
	}
	
	public String toString() {
		try {
			IFeature feature = getFeature();
			return feature.getLabel();
		}
		catch (CoreException e) {
			return "<failure>";
		}
	}
	public boolean hasIncludedFeatures() {
		try {
			IFeatureReference [] included = getFeature().getIncludedFeatureReferences();
			return included.length>0;
		}
		catch (CoreException e) {
			return false;
		}
	}
}
