/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUI;

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
			return UpdateUI.getString("FeatureAdapter.failure"); //$NON-NLS-1$
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
