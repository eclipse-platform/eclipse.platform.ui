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

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIMessages;

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
			IFeature feature = getFeature(null);
			return feature.getLabel();
		}
		catch (CoreException e) {
			return UpdateUIMessages.FeatureAdapter_failure; 
		}
	}
	public boolean hasIncludedFeatures(IProgressMonitor monitor) {
		try {
			IFeatureReference [] included = getFeature(monitor).getIncludedFeatureReferences();
			return included.length>0;
		}
		catch (CoreException e) {
			return false;
		}
	}
}
