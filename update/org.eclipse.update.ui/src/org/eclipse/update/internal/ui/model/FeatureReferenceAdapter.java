package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;

public class FeatureReferenceAdapter extends FeatureAdapter {
	private IFeatureReference featureRef;

	public FeatureReferenceAdapter(IFeatureReference featureRef) {
		this.featureRef = featureRef;
		setIncluded(featureRef instanceof IIncludedFeatureReference);
	}
	
	public IFeature getFeature(IProgressMonitor monitor) throws CoreException {
		return featureRef.getFeature(monitor);
	}
	
	public String getFastLabel() {
		return featureRef.getURL().toString();
	}
	
	public ISite getSite() {
		return featureRef.getSite();
	}
	
	public URL getURL() {
		return featureRef.getURL();
	}
	
	public boolean isOptional() {
		return featureRef instanceof IIncludedFeatureReference ? 
			((IIncludedFeatureReference)featureRef).isOptional():false;
	}

	public IFeatureAdapter[] getIncludedFeatures(IProgressMonitor monitor) {
		try {
			IFeatureReference[] included =
				getFeature(monitor).getIncludedFeatureReferences();
			FeatureReferenceAdapter[] result =
				new FeatureReferenceAdapter[included.length];
			for (int i = 0; i < included.length; i++) {
				result[i] = new FeatureReferenceAdapter(included[i]);
			}
			return result;
		} catch (CoreException e) {
			return new IFeatureAdapter[0];
		}
	}

	public IFeatureReference getFeatureReference() {
		return featureRef;
	}
}