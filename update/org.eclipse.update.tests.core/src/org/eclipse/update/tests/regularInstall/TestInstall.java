/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.regularInstall;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.IConfiguredSiteChangedListener;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.Site;
import org.eclipse.update.core.SiteFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.ConfiguredSite;
import org.eclipse.update.internal.core.InstallConfiguration;
import org.eclipse.update.internal.core.InstallRegistry;
import org.eclipse.update.internal.core.LocalSite;
import org.eclipse.update.internal.core.UpdateCore;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestInstall extends UpdateManagerTestCase {

	/**
	 * 
	 */
	public static final String PACKAGED_FEATURE_TYPE = "packaged"; //$NON-NLS-1$
	
	public Set filesToDelete = new HashSet();
	

	public class Listener implements IConfiguredSiteChangedListener {

		public boolean notified = false;

		public void featureInstalled(IFeature feature) {
			notified = true;
			System.out.println("Notified DefaultFeature Installed");
		}

		public void featureRemoved(IFeature feature) {
		}

		public void featureConfigured(IFeature feature) {
		}
		public void featureUnconfigured(IFeature feature) {
		}

		public boolean isNotified() {
			return notified;
		}
	}

	/**
	 * Constructor for Test1
	 */
	public TestInstall(String arg0) {
		super(arg0);
	}

	private IFeature getFeature1(ISite site)
		throws MalformedURLException, CoreException {
		SiteFeatureReference ref = new SiteFeatureReference();
		ref.setSite(site);
		ref.setURLString("features/org.eclipse.update.core.tests.feature1_1.0.4.jar");
		ref.setType(getDefaultInstallableFeatureType());
		ref.resolve(site.getURL(), null);
		return ref.getFeature(null);
	}
	


	public void testFileSite() throws Exception {

		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE,null);
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE, null);
		filesToDelete.add(new File(localSite.getURL().getFile()));
		localSite.install(remoteFeature, null, null);

		// verify
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));
		String pluginName = entries[0].getVersionedIdentifier().toString();
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		filesToDelete.add(pluginFile);
		assertTrue("plugin files not installed locally:"+pluginFile, pluginFile.exists());
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

	}

	/**
	 * 
	 */
	public void testHTTPSite() throws Exception {

		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE,null);
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		IFeature remoteFeature = null;

		if (features == null || features.length == 0)
			fail("No features on the site");

		for (int i = 0; i < features.length; i++) {
			if (features[i].getURL().toExternalForm().endsWith("features2.jar")) {
				remoteFeature = features[i].getFeature(null);
				break;
			}
		}

		assertNotNull("Cannot find feature2.jar on site", remoteFeature);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE, null);
		filesToDelete.add(new File(localSite.getURL().getFile()));
		localSite.install(remoteFeature, null, null);

		// feature2.jar should not be in the local site
		IFeatureReference[] localFeatures = localSite.getFeatureReferences();
		if (localFeatures == null || localFeatures.length == 0)
			fail("No features on the target site");

		boolean found = false;
		for (int i = 0; i < localFeatures.length; i++) {
			if (localFeatures[i].getURL().toExternalForm().endsWith("features2.jar")) {
				found = true;
				break;
			}
		}

		assertTrue(
			"Found feature2.jar on target site. Target site feature ref shouldnot contain JAR file",
			!found);

		// check
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String pluginName = entries[0].getVersionedIdentifier().toString();
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		filesToDelete.add(pluginFile);
		assertTrue("plugin info not installed locally:"+pluginFile, pluginFile.exists());

		File featureFile =
			new File(
				site,
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ remoteFeature.getVersionedIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());
		
	}

	public void testInstall() throws Exception {

		// cleanup local files...
		File localFile = new File(((LocalSite)SiteManager.getLocalSite()).getLocationURL().getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);

		URL INSTALL_SITE = null;
		try {
			INSTALL_SITE =
				new URL("http", getHttpHost(), getHttpPort(), bundle.getString("HTTP_PATH_2"));
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
		
		ISite remoteSite = SiteManager.getSite(INSTALL_SITE,null);
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		IFeature remoteFeature = null;

		if (features == null || features.length == 0)
			fail("No features on the site");

		for (int i = 0; i < features.length; i++) {
			if (features[i].getURL().toExternalForm().endsWith("helpFeature.jar")) {
				remoteFeature = features[i].getFeature(null);
				break;
			}
		}

		assertNotNull("Cannot find help.jar on site", remoteFeature);
		ILocalSite localSite = SiteManager.getLocalSite();
		IConfiguredSite site =
			localSite.getCurrentConfiguration().getConfiguredSites()[0];
		Listener listener = new Listener();
		site.addConfiguredSiteChangedListener(listener);

		((ConfiguredSite)site).setUpdatable(true);
		
		// list of files to be cleaned at the end
		File file = new File( site.getSite().getURL().getFile() +
							  File.separator + 
							  Site.DEFAULT_INSTALLED_FEATURE_PATH + 
							  remoteFeature.getVersionedIdentifier());
		filesToDelete.add(file);
		filesToDelete.add(localFile);
		filesToDelete.add( new File(((InstallConfiguration) localSite.getCurrentConfiguration()).getURL().getFile()));
		
		site.install(remoteFeature, null, null);

		IPluginEntry[] entries = remoteFeature.getRawPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String sitePath = site.getSite().getURL().getFile();
		String pluginName = entries[0].getVersionedIdentifier().toString();
		File pluginFile = new File(sitePath, Site.DEFAULT_PLUGIN_PATH + pluginName);
		filesToDelete.add(pluginFile);
		if (Locale.getDefault().toString().indexOf("fr") != -1) {
			assertTrue("plugin info not installed locally", pluginFile.exists());			
		}

		File featureFile =
			new File(
				sitePath,
				Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ remoteFeature.getVersionedIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());
		

		

		site.removeConfiguredSiteChangedListener(listener);
		
		assertTrue("Listener hasn't received notification", listener.isNotified());
	}

	public void testFileSiteWithoutSiteXML() throws Exception {

		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE,null);
		IFeature remoteFeature = getFeature1(remoteSite);
		
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
		localSite.install(remoteFeature, null, null);
		
		IFeatureReference[] features = localSite.getRawFeatureReferences();
		int numberOfInstalledFeatures = features.length;

		File file =
			new File(
				localSite.getURL().getFile()
					+ File.separator
					+ Site.DEFAULT_INSTALLED_FEATURE_PATH
					+ remoteFeature.getVersionedIdentifier());
		filesToDelete.add(file);
		file =
			new File(
				localSite.getURL().getFile()
					+ File.separator
					+ Site.DEFAULT_PLUGIN_PATH
					+ "org.eclipse.update.core.tests.feature1.plugin1_3.5.6");
		filesToDelete.add(file);
		file =
			new File(
				localSite.getURL().getFile()
					+ File.separator
					+ Site.DEFAULT_PLUGIN_PATH
					+ "org.eclipse.update.core.tests.feature1.plugin2_5.0.0");
		filesToDelete.add(file);
		file = new File(((LocalSite)SiteManager.getLocalSite()).getLocationURL().getFile());
		filesToDelete.add(file);

		
		if (numberOfInstalledFeatures == 0)
			fail("The local site does not contain feature, should not contain an XML file but features should be found anyway by parsing");
		if (localSite/*.getSite()*/.getArchives().length == 0)
			fail("The local site does not contain archives, should not contain an XML file but archives should be found anyway by parsing");
		
		try {
			SiteManager.getSite(new URL("http://www.eclipse.org/"),null);
			fail("The site contains site.xml... it should be an HTTP site without an XML file");			
		} catch (CoreException e) {
			// expected
		}

	}

	/*
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultInstallableFeatureType() {
		String pluginID =
			UpdateCore.getPlugin().getBundle().getSymbolicName() + ".";
		return pluginID + PACKAGED_FEATURE_TYPE;
	}
	
	protected void umTearDown() throws Exception {
		Iterator files = filesToDelete.iterator();		
		while(files.hasNext()) {
			File file = (File)files.next();
			UpdateManagerUtils.removeFromFileSystem(file);			
		}
		filesToDelete.clear();
		
		InstallRegistry.cleanup();
	}

}
