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
package org.eclipse.update.internal.configurator;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configurator.*;
import org.xml.sax.*;

public class InstalledSiteParser {
	
	// private when parsing file system
	private SiteEntry site;
	
	private static FeatureParser featureParser = new FeatureParser();

	public InstalledSiteParser() {
	}
	
	/*
	 * @see ISiteFactory#createSite(URL,boolean)
	 */
	public IPlatformConfiguration.ISiteEntry parse(URL url) throws CoreException{
		InputStream siteStream = null;
	
		try {
			// if url points to a directory
			// attempt to parse site.xml
			String path = url.getFile();
			File siteLocation = new File(path);
			if (!siteLocation.isDirectory() || !siteLocation.exists())
				throw Utils.newCoreException(Policy.bind("InstalledSiteParser.DirectoryDoesNotExist", siteLocation.getAbsolutePath()), null);
			
			site = parseSite(siteLocation);

// TODO			site.resolve(url, url);
	
			// Do not set read only as may install in it
			//site.markReadOnly();
//		} catch (MalformedURLException e) {
//			throw Utils.newCoreException(Policy.bind("InstalledSiteParser.UnableToCreateURL", url == null ? "" : url.toExternalForm()), e);
//			//$NON-NLS-1$
		} finally {
			try {
				if (siteStream != null)
					siteStream.close();
			} catch (IOException e) {
			}
		}
		return site;
	}
	/**
	 * Method parseSite.
	 */
	private SiteEntry parseSite(File directory) throws CoreException {

		this.site = new SiteEntry();
	
		parseInstalledFeatures(directory);
		parseInstalledPlugins(directory);

		return site;

	}

	/**
	 * Method parseFeature.
	 * @throws CoreException
	 */
	private void parseInstalledFeatures(File directory) throws CoreException {

		File featuresDir = new File(directory, "features");
		if (featuresDir.exists()) {

			URL featureURL;

			// handle the installed features under the features directory
			File[] dirs = featuresDir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					boolean valid = f.isDirectory() && (new File(f,"feature.xml").exists());
					if (!valid)
						System.out.println("Unable to find feature.xml in directory:" + f.getAbsolutePath());
					return valid;
				}
			});
			try {
				for (int index = 0; index < dirs.length; index++) {
					File featureXML = new File(dirs[index], "feature.xml");
					featureURL = featureXML.toURL();
					IPlatformConfiguration.IFeatureEntry featureEntry = featureParser.parse(featureURL);
					site.addFeatureEntry(featureEntry);
				}
			} catch (MalformedURLException e) {
				throw Utils.newCoreException(Policy.bind("InstalledSiteParser.UnableToCreateURLForFile", featuresDir.getAbsolutePath()), e);
				//$NON-NLS-1$
			}
		}
	}



	/**
	 * Method parsePlugins.
	 * 
	 * look into each plugin/fragment directory, crack the plugin.xml open (or fragment.xml ???)
	 * get id and version, calculate URL...	
	 * 
	 * @return VersionedIdentifier
	 * @throws CoreException
	 */
	private void parseInstalledPlugins(File directory) throws CoreException {

		File pluginPath = new File(directory, "plugins");
		File pluginFile = null;

		try {
			if (pluginPath.exists()) {
				File[] files = pluginPath.listFiles();
				PluginParser parser = new PluginParser();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {

						if (!(pluginFile = new File(files[i], "plugin.xml")).exists()) { //$NON-NLS-1$
							pluginFile = new File(files[i], "fragment.xml"); //$NON-NLS-1$
						}

						if (pluginFile != null && pluginFile.exists() && !pluginFile.isDirectory()) {
							PluginEntry entry = parser.parse(new FileInputStream(pluginFile));
							site.addPluginEntry(entry);
						}
					} // files[i] is a directory
				}
			} // path is a directory
		} catch (IOException e) {
			String pluginFileString = (pluginFile == null) ? null : pluginFile.getAbsolutePath();
			throw Utils.newCoreException(Policy.bind("InstalledSiteParser.ErrorAccessing", pluginFileString), e);
			//$NON-NLS-1$
		} catch (SAXException e) {
			String pluginFileString = (pluginFile == null) ? null : pluginFile.getAbsolutePath();
			throw Utils.newCoreException(Policy.bind("InstalledSiteParser.ErrorParsingFile", pluginFileString), e);
			//$NON-NLS-1$
		}

	}


//	/**
//	* Method parseFeature.
//	* @throws CoreException
//	*/
//	private void parsePackagedFeatures(File directory) throws CoreException {
//
//		// FEATURES
//		File featureDir = new File(directory, Site.DEFAULT_FEATURE_PATH);
//		if (featureDir.exists()) {
//			String[] dir;
//			FeatureReference featureRef;
//			URL featureURL;
//			File currentFeatureFile;
//			String newFilePath = null;
//
//			try {
//				// only list JAR files
//				dir = featureDir.list(FeaturePackagedContentProvider.filter);
//				for (int index = 0; index < dir.length; index++) {
//
//					// check if the JAR file contains a feature.xml
//					currentFeatureFile = new File(featureDir, dir[index]);
//					JarContentReference ref = new JarContentReference("", currentFeatureFile);
//					ContentReference result = null;
//					try {
//						result = ref.peek(Feature.FEATURE_XML, null, null);
//					} catch (IOException e) {
//						UpdateCore.warn("Exception retrieving feature.xml in file:" + currentFeatureFile, e);
//					}
//					if (result == null) {
//						UpdateCore.warn("Unable to find feature.xml in file:" + currentFeatureFile);
//					} else {
//						featureURL = currentFeatureFile.toURL();
//						// PERF: remove code
//						//SiteFileFactory archiveFactory = new SiteFileFactory();
//						featureRef = new FeatureReference(site, featureURL.toExternalForm());
//						site.addFeatureReference(featureRef);
//					}
//				}
//			} catch (MalformedURLException e) {
//				throw Utilities.newCoreException(Policy.bind("SiteFileFactory.UnableToCreateURLForFile", newFilePath), e);
//				//$NON-NLS-1$
//			}
//		}
//	}
	
//	/**
//	 * 
//	 */
//	private void parsePackagedPlugins(File pluginDir) throws CoreException {
//
//		File file = null;
//		String[] dir;
//
//		ContentReference ref = null;
//		String refString = null;
//
//		try {
//			if (pluginDir.exists()) {
//				dir = pluginDir.list(FeaturePackagedContentProvider.filter);
//				for (int i = 0; i < dir.length; i++) {
//					file = new File(pluginDir, dir[i]);
//					JarContentReference jarReference = new JarContentReference(null, file);
//					ref = jarReference.peek("plugin.xml", null, null); //$NON-NLS-1$
//					if (ref == null)
//						jarReference.peek("fragment.xml", null, null); //$NON-NLS-1$
//
//					refString = (ref == null) ? null : ref.asURL().toExternalForm();
//
//					if (ref != null) {
//						PluginEntry entry = new PluginParser().parse(ref.getInputStream());
//						addParsedPlugin(entry,file);
//					} //ref!=null
//				} //for
//			}
//
//		} catch (IOException e) {
//			throw Utilities.newCoreException(Policy.bind("SiteFileFactory.ErrorAccessing", refString), e);
//			//$NON-NLS-1$
//		} catch (SAXException e) {
//			throw Utilities.newCoreException(Policy.bind("SiteFileFactory.ErrorParsingFile", refString), e);
//			//$NON-NLS-1$
//		}
//	}
}
