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

	/*
	 * @see IFeatureAdapter#getInstallConfiguration()
	 */
	public IInstallConfiguration getInstallConfiguration() {
		return null;
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
}
