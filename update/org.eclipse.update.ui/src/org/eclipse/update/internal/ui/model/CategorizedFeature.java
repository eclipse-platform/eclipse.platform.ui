package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class CategorizedFeature extends ModelObject {
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