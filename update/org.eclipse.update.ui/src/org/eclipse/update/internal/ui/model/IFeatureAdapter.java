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
public interface IFeatureAdapter {
	public IFeature getFeature() throws CoreException;
	public IFeatureAdapter [] getIncludedFeatures();
	public boolean hasIncludedFeatures();
	public boolean isIncluded();
}
