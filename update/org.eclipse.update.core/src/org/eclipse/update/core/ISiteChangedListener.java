package org.eclipse.update.core;
public interface ISiteChangedListener {
	void featureUpdated(IFeature feature);
	void featureInstalled(IFeature feature);
	void featureUninstalled(IFeature feature);
}

