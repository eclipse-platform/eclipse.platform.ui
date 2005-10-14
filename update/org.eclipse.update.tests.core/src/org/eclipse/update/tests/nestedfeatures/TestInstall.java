/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.nestedfeatures;
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestInstall extends UpdateManagerTestCase {

	/**
	 * Constructor for Test1
	 */
	public TestInstall(String arg0) {
		super(arg0);
	}

	public void testFileSite() throws Exception {
		
		//cleanup target 
		File target = new File(TARGET_FILE_SITE.getFile());
		UpdateManagerUtils.removeFromFileSystem(target);		
		InstallRegistry.cleanup();
		

		ISite remoteSite =
			SiteManager.getSite(new URL(SOURCE_FILE_SITE, "nestedFeatureSiteTest/site.xml"),null);
		IFeature remoteFeature = remoteSite.getFeatureReferences()[0].getFeature(null);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
		localSite.install(remoteFeature, null, null);

		// verify root Feature
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));
		String pluginName = entries[0].getVersionedIdentifier().toString();
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue(
			"plugin files not installed locally:" + pluginFile,
			pluginFile.exists());
		File pluginXMLFile =
			new File(
				site,
				Site.DEFAULT_PLUGIN_PATH + pluginName + File.separator + "plugin.xml");
		assertTrue("plugin.xml file not installed locally", pluginXMLFile.exists());

		File featureFile =
			new File(
				site,
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ remoteFeature.getVersionedIdentifier().toString());
		assertTrue(
			"feature info not installed locally:" + featureFile,
			featureFile.exists());

		// clean plugins & feature
		for (int i = 0; i < entries.length; i++) {
			pluginName = entries[i].getVersionedIdentifier().toString();
			pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			UpdateManagerUtils.removeFromFileSystem(pluginFile);
			InstallRegistry.unregisterPlugin(entries[i]);
		}
		UpdateManagerUtils.removeFromFileSystem(featureFile);

		// verify child Feature
		IFeature childFeature =
			remoteFeature.getIncludedFeatureReferences()[0].getFeature(null);
		entries = childFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));
		pluginName = entries[0].getVersionedIdentifier().toString();
		pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue(
			"plugin files not installed locally:" + pluginFile,
			pluginFile.exists());
		pluginXMLFile =
			new File(
				site,
				Site.DEFAULT_PLUGIN_PATH + pluginName + File.separator + "plugin.xml");
		assertTrue("plugin.xml file not installed locally", pluginXMLFile.exists());

		featureFile =
			new File(
				site,
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ childFeature.getVersionedIdentifier().toString());
		assertTrue(
			"feature info not installed locally:" + featureFile,
			featureFile.exists());

		// clean plugins
		for (int i = 0; i < entries.length; i++) {
			pluginName = entries[0].getVersionedIdentifier().toString();
			pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			UpdateManagerUtils.removeFromFileSystem(pluginFile);
		}

		// clean features
		UpdateManagerUtils.removeFromFileSystem(featureFile);
		UpdateManagerUtils.removeFromFileSystem(new File(localSite.getURL().getFile()));
		InstallRegistry.cleanup();
	}

	/**
	 * 
	 */
	public void testHTTPSite() throws Exception {

		ISite remoteSite =
			SiteManager.getSite(new URL(SOURCE_HTTP_SITE, "nestedFeatureSiteTest/"),null);
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		IFeature remoteFeature = null;

		if (features == null || features.length == 0)
			fail("No features on the site");

		for (int i = 0; i < features.length; i++) {
			if (features[i].getURL().toExternalForm().endsWith("rootfeature.jar")) {
				remoteFeature = features[i].getFeature(null);
				break;
			}
		}

		assertNotNull("Cannot find rootfeature.jar on site", remoteFeature);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
		localSite.install(remoteFeature, null, null);

		// rootfeature.jar should not be in the local site
		IFeatureReference[] localFeatures = localSite.getFeatureReferences();
		if (localFeatures == null || localFeatures.length == 0)
			fail("No features on the target site");

		boolean found = false;
		for (int i = 0; i < localFeatures.length; i++) {
			if (localFeatures[i].getURL().toExternalForm().endsWith("rootfeature.jar")) {
				found = true;
				break;
			}
		}

		assertTrue(
			"Found rootfeature.jar on target site. Target site feature ref shouldnot contain JAR file",
			!found);

		// check root
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String pluginName = entries[0].getVersionedIdentifier().toString();
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue(
			"plugin files not installed locally:" + pluginFile,
			pluginFile.exists());		
		File pluginXMLFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName+ File.separator + "plugin.xml");
		assertTrue(
			"plugin info not installed locally:" + pluginXMLFile,
			pluginXMLFile.exists());

		File featureFile =
			new File(
				site,
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ remoteFeature.getVersionedIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());

		// clean plugins & feature
		for (int i = 0; i < entries.length; i++) {
			pluginName = entries[i].getVersionedIdentifier().toString();
			pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			UpdateManagerUtils.removeFromFileSystem(pluginFile);
			InstallRegistry.unregisterPlugin(entries[i]);
		}
		UpdateManagerUtils.removeFromFileSystem(featureFile);

		// verify child Feature
		IFeature childFeature =
			remoteFeature.getIncludedFeatureReferences()[0].getFeature(null);
		entries = childFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));
		pluginName = entries[0].getVersionedIdentifier().toString();
		pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue(
			"plugin files not installed locally:" + pluginFile,
			pluginFile.exists());
		pluginXMLFile =
			new File(
				site,
				Site.DEFAULT_PLUGIN_PATH + pluginName + File.separator + "plugin.xml");
		assertTrue("plugin.xml file not installed locally", pluginXMLFile.exists());

		featureFile =
			new File(
				site,
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ childFeature.getVersionedIdentifier().toString());
		assertTrue(
			"feature info not installed locally:" + featureFile,
			featureFile.exists());

		// clean plugins
		for (int i = 0; i < entries.length; i++) {
			pluginName = entries[0].getVersionedIdentifier().toString();
			pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			UpdateManagerUtils.removeFromFileSystem(pluginFile);
		}

		// clean features
		UpdateManagerUtils.removeFromFileSystem(featureFile);
		UpdateManagerUtils.removeFromFileSystem(new File(localSite.getURL().getFile()));
	}

//	public void testInstall() throws Exception {
//
//		// cleanup local files...
//		URL localURL = ((LocalSite) SiteManager.getLocalSite()).getLocationURL();
//		File localFile = new File(localURL.getFile());
//		UpdateManagerUtils.removeFromFileSystem(localFile);
//		InstallRegistry.cleanup();
//
//		URL INSTALL_SITE = null;
//		try {
//			INSTALL_SITE = new URL(SOURCE_FILE_SITE, "nestedFeatureSiteTest/site.xml");
//		} catch (Exception e) {
//			fail(e.toString());
//			e.printStackTrace();
//		}
//
//		ISite remoteSite = SiteManager.getSite(INSTALL_SITE,null);
//		IFeatureReference[] features = remoteSite.getFeatureReferences();
//		IFeature remoteFeature = null;
//
//		if (features == null || features.length == 0)
//			fail("No features on the site");
//
//		for (int i = 0; i < features.length; i++) {
//			if (features[i].getURL().toExternalForm().endsWith("rootfeature.jar")) {
//				remoteFeature = features[i].getFeature(null);
//				break;
//			}
//		}
//
//		assertNotNull("Cannot find rootfeature.jar on site", remoteFeature);
//		ILocalSite localSite = SiteManager.getLocalSite();
//		IConfiguredSite site =
//			localSite.getCurrentConfiguration().getConfiguredSites()[0];
//
//		((ConfiguredSite)site).setUpdatable(true);
//		site.install(remoteFeature, null, null);
//
//		IPluginEntry[] entries = remoteFeature.getPluginEntries();
//		assertTrue("no plugins entry", (entries != null && entries.length != 0));
//
//		String sitePath = site.getSite().getURL().getFile();
//		String pluginName = entries[0].getVersionedIdentifier().toString();
//		File pluginFile = new File(sitePath, Site.DEFAULT_PLUGIN_PATH + pluginName);
//		assertTrue("plugin info not installed locally"+pluginFile, pluginFile.exists());
//
//		File featureFile =
//			new File(
//				sitePath,
//				Site.DEFAULT_INSTALLED_FEATURE_PATH
//					+ remoteFeature.getVersionedIdentifier().toString());
//		assertTrue("feature info not installed locally", featureFile.exists());
//
//		//cleanup
//		File file =
//			new File(
//				site.getSite().getURL().getFile()
//					+ File.separator
//					+ Site.DEFAULT_INSTALLED_FEATURE_PATH
//					+ remoteFeature.getVersionedIdentifier());
//		// clean plugins
//		for (int i = 0; i < entries.length; i++) {
//			pluginName = entries[0].getVersionedIdentifier().toString();
//			pluginFile =
//				new File(
//					site.getSite().getURL().getFile(),
//					Site.DEFAULT_PLUGIN_PATH + pluginName);
//			UpdateManagerUtils.removeFromFileSystem(pluginFile);
//		}
//
//		UpdateManagerUtils.removeFromFileSystem(file);
//		UpdateManagerUtils.removeFromFileSystem(pluginFile);
//		UpdateManagerUtils.removeFromFileSystem(localFile);
//		UpdateManagerUtils.removeFromFileSystem(
//			new File(
//				((InstallConfiguration) localSite.getCurrentConfiguration())
//					.getURL()
//					.getFile()));
//	}
}
