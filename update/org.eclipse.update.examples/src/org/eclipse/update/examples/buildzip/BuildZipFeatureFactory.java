package org.eclipse.update.examples.buildzip;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.BaseFeatureFactory;
import org.eclipse.update.core.Feature;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureFactory;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.model.FeatureModel;

/**
 * An example feature factory based on the packaging 
 * format used for integration and stable builds
 * posted on the downloads pages at www.eclipse.org
 * </p>
 * @since 2.0
 */

public class BuildZipFeatureFactory
	extends BaseFeatureFactory
	implements IFeatureFactory {

	/*
	 * @see IFeatureFactory#createFeature(URL, ISite)
	 */
	public IFeature createFeature(URL url, ISite site) throws CoreException {
		try {
			// create content provider for feature
			BuildZipContentProvider cp = new BuildZipContentProvider(url);
			Feature feature = null;
			
			// parse the feature
			feature = (Feature) parseFeature(cp);
			
			// initialize feature
			feature.setFeatureContentProvider(cp);
			feature.setSite(site);
			feature.resolve(url, null); 
			feature.markReadOnly();
			return feature;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.examples.buildzip",0,"Unable to create feature",e));
		}
	}

	/*
	 * parse the build zip to reconstruct a feature model
	 */
	public FeatureModel parseFeature(BuildZipContentProvider cp) throws Exception {
		return (new BuildZipFeatureParser(this)).parse(cp);
	}

}
