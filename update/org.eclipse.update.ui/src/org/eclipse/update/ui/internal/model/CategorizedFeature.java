package org.eclipse.update.ui.internal.model;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

public class CategorizedFeature extends PlatformObject {
	private IFeature feature;
	public CategorizedFeature(IFeature feature) {
		this.feature = feature;
	}
	
	public IFeature getFeature() {
		return feature;
	}
	
	public String toString() {
		try {
			return feature.getLabel();
		}
		catch (CoreException e) {
			return "??";
		}
	}
}