package org.eclipse.update.internal.ui.wizards;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.forms.ActivityConstraints;
import org.eclipse.update.internal.ui.model.MissingFeature;

/**
 *
 */
public class DeltaAdapter {
	private ISessionDelta delta;
	private DeltaFeatureAdapter[] dfeatures;
	private boolean selected;
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

	public void setSelected(boolean value) {
		this.selected = value;
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
		return !(count == 0 || count == getFeatures().length);
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
		this.selected = false;
	}

	public boolean isSelected() {
		return selected;
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
		status = ActivityConstraints.validateSessionDelta(delta, refs);
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
			try {
				IFeature feature = reference.getFeature(null);
				dfeature = new DeltaFeatureAdapter(this, reference, feature);
				dfeature.setSelected(true);
			} catch (CoreException e) {
				IFeature feature =
					new MissingFeature(reference.getSite(), reference.getURL());
				dfeature = new DeltaFeatureAdapter(this, reference, feature);
			}
			dfeatures[i] = dfeature;
		}
		selectionBlocked = false;
	}
}
