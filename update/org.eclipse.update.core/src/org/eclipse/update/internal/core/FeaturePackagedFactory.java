package org.eclipse.update.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureFactory;
import org.eclipse.update.core.*;

public class FeaturePackagedFactory implements IFeatureFactory {

	/*
	 * @see IFeatureFactory#createFeature(URL,ISite)
	 */
	public IFeature createFeature(URL url,ISite site) throws CoreException {
		return new FeaturePackaged(url,site);
	}

}
