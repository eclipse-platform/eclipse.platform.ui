/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public class SimpleFeatureAdapter extends FeatureAdapter {
	protected IFeature feature;
	private boolean optional;
	public SimpleFeatureAdapter(IFeature feature) {
		this(feature, false);
	}
	public SimpleFeatureAdapter(IFeature feature, boolean optional) {
		this.feature = feature;
		this.optional = optional;
	}
	
	public IFeature getFeature(IProgressMonitor monitor) throws CoreException {
		return feature;
	}
	
	public String getFastLabel() {
		return feature.getLabel();
	}
	
	public URL getURL() {
		return feature.getURL();
	}
	
	public ISite getSite() {
		return feature.getSite();
	}
	
	public IFeatureAdapter[] getIncludedFeatures(IProgressMonitor monitor) {
		try {
			IIncludedFeatureReference[] included = getFeature(monitor).getIncludedFeatureReferences();
			SimpleFeatureAdapter[] result =
				new SimpleFeatureAdapter[included.length];
			for (int i = 0; i < included.length; i++) {
				result[i] =
					new SimpleFeatureAdapter(included[i].getFeature(null), included[i].isOptional());
				result[i].setIncluded(true);
			}
			return result;
		} catch (CoreException e) {
			return new IFeatureAdapter[0];
		}
	}
	public boolean isOptional() {
		return optional;
	}
}
