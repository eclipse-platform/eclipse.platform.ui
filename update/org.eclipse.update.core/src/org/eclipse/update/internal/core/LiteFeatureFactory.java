package org.eclipse.update.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.BaseFeatureFactory;
import org.eclipse.update.core.Feature;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.model.FeatureModel;

public class LiteFeatureFactory extends BaseFeatureFactory {

	public LiteFeatureFactory() {
		super();
	}

	@Override
	public IFeature createFeature(URL url, ISite site, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureModel createFeatureModel() {
		return new LiteFeature();
	}
}
