package org.eclipse.update.internal.ui.wizards;

import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;

/**
 *
 */
public class DeltaFeatureAdapter {
	private IFeatureReference ref;
	private IFeature feature;
	private DeltaAdapter deltaAdapter;
	private boolean selected;
	
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
		
	public String toString() {
		return feature.getLabel()
			+ " ("
			+ feature.getVersionedIdentifier().getVersion().toString()
			+ ")";
	}
}
