/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.mirror;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.model.*;
import org.xml.sax.*;

public class MirrorSiteFactory extends BaseSiteFactory {
	/*
	 * @see SiteModelFactory#createSiteMapModel()
	 */
	public SiteModel createSiteMapModel() {
		return new MirrorSite(this);
	}
	/*
	 * @see ISiteFactory#createSite(URL,boolean)
	 */
	public ISite createSite(URL url)
		throws CoreException, InvalidSiteTypeException {

		InputStream siteStream = null;

		String path = url.getFile();
		File siteLocation = new File(path);

		if (!siteLocation.exists()) {
			if (!siteLocation.mkdirs()) {
				throw Utilities.newCoreException(
					Policy.bind(
						"SiteMirrorFactory.CannotCreateDirectory",
						siteLocation.getAbsolutePath()),
					null);
			}
		}
		if (!siteLocation.isDirectory() || !siteLocation.canWrite())
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteMirrorFactory.SiteNotADirectoryOrNotWritable",
					siteLocation.getAbsolutePath()),
				null);

		path = siteLocation.getAbsolutePath().replace(File.separatorChar, '/');
		try {
			// ensure URL ends with /
			if (!path.endsWith("/"))
				path += "/";
			url = new URL("file:" + path); //$NON-NLS-1$
		} catch (MalformedURLException mue) {
		}
		MirrorSite site=null;
		// parse public features
		if (new File(siteLocation, Site.SITE_XML).exists()) {
			try {
				siteStream =
					new FileInputStream(new File(siteLocation, Site.SITE_XML));
			} catch (FileNotFoundException fnfe) {
			}
			site = (MirrorSite) parseSite(siteStream);
			try {
				if (siteStream != null)
					siteStream.close();
			} catch (IOException e) {
			}
		}
		if(site==null){
			site=(MirrorSite) createSiteMapModel();
		}
		// parse downloaded plugins and fragments
		parseDownloadedPluginsAndFragments(site, new File(path,Site.DEFAULT_PLUGIN_PATH));
		// parse downloaded features
		parseDownloadedFeatures(site, new File(path,Site.DEFAULT_FEATURE_PATH));

		SiteContentProvider contentProvider = null;
		contentProvider = new SiteFileContentProvider(url);

		site.setSiteContentProvider(contentProvider);
		contentProvider.setSite(site);
		try {
			site.resolve(url, url);
		} catch (MalformedURLException mue) {
			throw Utilities.newCoreException(Policy.bind("SiteMirrorFactory.UnableToCreateURL", url == null ? "" : url.toExternalForm()), mue); //$NON-NLS-1$
		}
		return site;
	}
	/**
	 * 
	 */
	private void parseDownloadedPluginsAndFragments(MirrorSite site, File pluginDir) throws CoreException {
		File file = null;
		String[] dir;

		ContentReference ref = null;
		String refString = null;

		try {
			if (pluginDir.exists()) {
				dir = pluginDir.list(FeaturePackagedContentProvider.filter);
				for (int i = 0; i < dir.length; i++) {
					file = new File(pluginDir, dir[i]);
					JarContentReference jarReference =
						new JarContentReference(null, file);
					ref = jarReference.peek("plugin.xml", null, null); //$NON-NLS-1$
					if (ref == null)
						ref=jarReference.peek("fragment.xml", null, null); //$NON-NLS-1$
					refString =
						(ref == null) ? null : ref.asURL().toExternalForm();

					if (ref != null) {
						PluginEntry entry =
							new DefaultPluginParser().parse(
								ref.getInputStream());
						site.addDownloadedPluginEntry(entry);

					}
				}
			}

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
			
		} catch (SAXException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	/**
	* Method parseFeature.
	* @throws CoreException
	*/
	private void parseDownloadedFeatures(MirrorSite site, File featureDir) throws CoreException {
		if (featureDir.exists()) {
			String[] dir;
			SiteFeatureReferenceModel featureRef;
			URL featureURL;
			File currentFeatureFile;
			String newFilePath = null;

			try {
				// only list JAR files
				dir = featureDir.list(FeaturePackagedContentProvider.filter);
				for (int index = 0; index < dir.length; index++) {

					// check if the JAR file contains a feature.xml
					currentFeatureFile = new File(featureDir, dir[index]);
					JarContentReference ref = new JarContentReference("", currentFeatureFile);
					ContentReference result = null;
					try {
						result = ref.peek(Feature.FEATURE_XML, null, null);
					} catch (IOException e) {
						UpdateCore.warn("Exception retrieving feature.xml in file:" + currentFeatureFile, e);
					}
					if (result == null) {
						UpdateCore.warn("Unable to find feature.xml in file:" + currentFeatureFile);
					} else {
						featureURL = currentFeatureFile.toURL();
						featureRef = createFeatureReferenceModel();
						featureRef.setSiteModel(site);
						featureRef.setURLString(featureURL.toExternalForm());
						featureRef.setType(ISite.DEFAULT_PACKAGED_FEATURE_TYPE);
						site.addDownloadedFeatureReferenceModel(featureRef);
					}
				}
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(Policy.bind("SiteFileFactory.UnableToCreateURLForFile", newFilePath), e);
				//$NON-NLS-1$
			}
		}
	}

}
