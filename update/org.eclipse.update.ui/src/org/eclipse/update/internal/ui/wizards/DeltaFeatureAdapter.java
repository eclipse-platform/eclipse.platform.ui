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
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 *
 */
public class DeltaFeatureAdapter {
	private IFeatureReference ref;
	private IFeature feature;
	private DeltaAdapter deltaAdapter;
	private boolean selected;
	private boolean duplicate;
	
	public DeltaFeatureAdapter(DeltaAdapter deltaAdapter, IFeatureReference ref, IFeature feature) {
		this.ref = ref;
		this.feature = feature;
		this.deltaAdapter = deltaAdapter;
	}
	
	public DeltaAdapter getDeltaAdapter() {
		return deltaAdapter;
	}
	
	public ISessionDelta getDelta() {
		return deltaAdapter.getDelta();
	}
	
	public IFeatureReference getFeatureReference() {
		return ref;
	}
	
	public IFeature getFeature() {
		return feature;
	}
		
	public void setSelected(boolean selected) {
		this.selected = selected;
		deltaAdapter.featureSelected(selected);
	}
		
	public boolean isSelected() {
		return selected;
	}
	
	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
	
	public boolean isDuplicate() {
		return duplicate;
	}
		
	public String toString() {
		if (duplicate) {
			return UpdateUI.getFormattedMessage("DeltaFeatureAdapter.longName",
				new String [] { feature.getLabel(), 
								feature.getVersionedIdentifier().getVersion().toString(),
								feature.getSite().getURL().toString()});
		}
		else {
		return UpdateUI.getFormattedMessage("DeltaFeatureAdapter.shortName",
			new String [] { feature.getLabel(), 
							feature.getVersionedIdentifier().getVersion().toString() });
		}
	}
}
