package org.eclipse.update.ui.internal.model;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

public class CategorizedFeature extends PlatformObject {
	private IFeatureReference featureRef;
	public CategorizedFeature(IFeatureReference featureRef) {
		this.featureRef = featureRef;
	}
	
	public IFeature getFeature() throws CoreException {
		return featureRef.getFeature();
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