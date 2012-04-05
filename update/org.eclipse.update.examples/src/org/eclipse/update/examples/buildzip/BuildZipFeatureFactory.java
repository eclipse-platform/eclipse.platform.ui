/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.examples.buildzip;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
	 * @see IFeatureFactory#createFeature(URL, ISite, IProgressMonitor)
	 */
	public IFeature createFeature(URL url, ISite site, IProgressMonitor monitor) throws CoreException {
		try {
			// create content provider for feature
			BuildZipFeatureContentProvider cp = new BuildZipFeatureContentProvider(url);
			Feature feature = null;
			
			// parse the feature
			feature = (Feature) parseFeature(cp);
			
			// initialize feature
			feature.setFeatureContentProvider(cp);
			feature.setSite(site);
			feature.resolve(cp.getFeatureBaseURL(), null); 
			feature.markReadOnly();
			return feature;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.examples.buildzip",0,"Unable to create feature",e));
		}
	}

	/*
	 * parse the build zip to reconstruct a feature model
	 */
	public FeatureModel parseFeature(BuildZipFeatureContentProvider cp) throws Exception {
		return (new BuildZipFeatureParser(this)).parse(cp);
	}

}
