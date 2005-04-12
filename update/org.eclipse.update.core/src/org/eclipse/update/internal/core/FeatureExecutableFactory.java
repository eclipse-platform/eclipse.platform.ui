/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;

/**
 * FeatureFactory for Executable Features
 */
public class FeatureExecutableFactory extends BaseFeatureFactory {

	/*
	 * @see IFeatureFactory#createFeature(URL,ISite,IProgressMonitor)
	 */
	public IFeature createFeature(URL url, ISite site, IProgressMonitor monitor) throws CoreException {

		TargetFeature feature = null;
		InputStream featureStream = null;
		if (monitor == null)
			monitor = new NullProgressMonitor();

		if (url == null)
			return createFeature(site);

		// the URL should point to a directory
		url = validate(url);

		try {
			IFeatureContentProvider contentProvider = new FeatureExecutableContentProvider(url);
//			// PERF: Do not create FeatureContentConsumer
// bug 79893
			IFeatureContentConsumer contentConsumer =new FeatureExecutableContentConsumer();

			URL nonResolvedURL = contentProvider.getFeatureManifestReference(null).asURL();
			URL resolvedURL = URLEncoder.encode(nonResolvedURL);
			featureStream = UpdateCore.getPlugin().get(resolvedURL).getInputStream();

			feature = (TargetFeature) this.parseFeature(featureStream, resolvedURL.toExternalForm());
			monitor.worked(1);
			feature.setSite(site);

			feature.setFeatureContentProvider(contentProvider);
//			// PERF: FeatureContentConsumer
// bug 79893
			feature.setContentConsumer(contentConsumer);

			feature.resolve(url, url);
			feature.markReadOnly();
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			throw Utilities.newCoreException(NLS.bind(Messages.FeatureFactory_CreatingError, (new String[] { url.toExternalForm() })), e);
		} finally {
			try {
				if (featureStream != null)
					featureStream.close();
			} catch (IOException e) {
			}
		}
		return feature;
	}

	/*
	 * @see FeatureModelFactory#createFeatureModel()
	 */
	public FeatureModel createFeatureModel() {
		return new TargetFeature();
	}

	/*
	 * Creates an empty feature on the site 
	 */
	private IFeature createFeature(ISite site) throws CoreException {
		TargetFeature feature = null;

		IFeatureContentProvider contentProvider = new FeatureExecutableContentProvider(null);
		IFeatureContentConsumer contentConsumer = new FeatureExecutableContentConsumer();
		feature = (TargetFeature) createFeatureModel();
		feature.setSite(site);
		feature.setFeatureContentProvider(contentProvider);
		feature.setContentConsumer(contentConsumer);

		// do not mark read only yet...	
		return feature;
	}

	/*
	 * validates a URL as a directory URL
	 */
	private URL validate(URL url) throws CoreException {

		if (url == null)
			throw Utilities.newCoreException(Messages.FeatureExecutableFactory_NullURL, null); 

		if (!(url.getFile().endsWith("/") || url.getFile().endsWith(File.separator) || url.getFile().endsWith(Feature.FEATURE_XML))) { //$NON-NLS-1$
			try {
				String path = url.getFile() + "/"; //$NON-NLS-1$
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(NLS.bind(Messages.FeatureExecutableFactory_CannotCreateURL, (new String[] { url.toExternalForm() })), e);
			}
		}
		return url;
	}

}
