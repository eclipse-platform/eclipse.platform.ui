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

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.operations.*;

/**
 *
 */
public class DeltaAdapter {
	private ISessionDelta delta;
	private DeltaFeatureAdapter[] dfeatures;
	private boolean removed;
	private IStatus status;
	private boolean statusComputed;
	private String name;
	private boolean selectionBlocked;

	public DeltaAdapter(ISessionDelta delta) {
		this.delta = delta;
		name = Utilities.format(delta.getDate());
	}

	public ISessionDelta getDelta() {
		return delta;
	}

	public String toString() {
		return name;
	}

	public DeltaFeatureAdapter[] getFeatures() {
		if (dfeatures == null) {
			initializeFeatures();
		}
		return dfeatures;
	}
	
	public void addFeaturesTo(ArrayList list) {
		DeltaFeatureAdapter [] array = getFeatures();
		for (int i=0; i<array.length; i++) {
			list.add(array[i]);
		}
	}

	public void setSelected(boolean value) {
		DeltaFeatureAdapter[] adapters = getFeatures();

		boolean stateChange = false;
		selectionBlocked = true;
		for (int i = 0; i < adapters.length; i++) {
			DeltaFeatureAdapter adapter = adapters[i];
			if (adapter.isSelected() != value) {
				adapter.setSelected(value);
				stateChange = true;
			}
		}
		selectionBlocked = false;
		if (value && stateChange) {
			// selecting the delta, we caused the
			// list of features to change and that
			// may affect the status. Reset status.
			resetStatus();
		}
	}

	void featureSelected(boolean selected) {
		if (selectionBlocked)
			return;
		int count = getSelectionCount();
		selected = (count > 0);
	}

	public int getSelectionCount() {
		DeltaFeatureAdapter[] adapters = getFeatures();
		int count = 0;
		for (int i = 0; i < adapters.length; i++) {
			DeltaFeatureAdapter adapter = adapters[i];
			if (adapter.isSelected())
				count++;
		}
		return count;
	}

	public boolean isMixedSelection() {
		int count = getSelectionCount();
		return count > 0 && count < getFeatures().length;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
		setSelected(false);
	}

	public boolean isSelected() {
		return getSelectionCount()>0;
	}

	public boolean isRemoved() {
		return removed;
	}

	public IStatus getStatus() {
		if (!statusComputed)
			computeStatus();
		return status;
	}

	public IFeatureReference[] getSelectedReferences() {
		ArrayList refs = new ArrayList();
		DeltaFeatureAdapter[] adapters = getFeatures();
		for (int i = 0; i < adapters.length; i++) {
			DeltaFeatureAdapter adapter = adapters[i];
			if (adapter.isSelected())
				refs.add(adapter.getFeatureReference());
		}
		return (IFeatureReference[]) refs.toArray(
			new IFeatureReference[refs.size()]);
	}

	public void resetStatus() {
		statusComputed = false;
	}

	private void computeStatus() {
		IFeatureReference[] refs = getSelectedReferences();
		status = OperationsManager.getValidator().validateSessionDelta(delta, refs);
		statusComputed = true;
	}

	public boolean isValid() {
		return getStatus() == null;
	}

	private void initializeFeatures() {
		IFeatureReference[] references = delta.getFeatureReferences();
		dfeatures = new DeltaFeatureAdapter[references.length];
		selectionBlocked = true;

		for (int i = 0; i < references.length; i++) {
			IFeatureReference reference = references[i];
			DeltaFeatureAdapter dfeature = null;
			DeltaFeatureAdapter duplicate = findDuplicate(reference, dfeatures);
			try {
				IFeature feature = reference.getFeature(null);
				dfeature = new DeltaFeatureAdapter(this, reference, feature);
				dfeature.setSelected(true);
			} catch (CoreException e) {
				IFeature feature =
					new MissingFeature(reference.getSite(), reference.getURL());
				dfeature = new DeltaFeatureAdapter(this, reference, feature);
			}
			if (duplicate != null) {
				dfeature.setDuplicate(true);
				duplicate.setDuplicate(true);
			}
			dfeatures[i] = dfeature;
		}
		selectionBlocked = false;
	}

	private DeltaFeatureAdapter findDuplicate(
		IFeatureReference reference,
		DeltaFeatureAdapter[] dfeatures) {
		for (int i = 0; i < dfeatures.length; i++) {
			DeltaFeatureAdapter prev = dfeatures[i];
			if (prev == null)
				return null;
			try {
				if (reference
					.getVersionedIdentifier()
					.equals(
						prev.getFeatureReference().getVersionedIdentifier())) {
					return prev;
				}
			} catch (CoreException e) {
				return null;
			}
		}
		return null;
	}
}
