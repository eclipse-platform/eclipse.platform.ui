package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
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

	private IFeature getFeature1(ISite site) throws MalformedURLException, CoreException {
		URL id = UpdateManagerUtils.getURL(site.getURL(), "features/org.eclipse.update.core.tests.feature1_1.0.4.jar", null);
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id, site);
		remoteFeature.initializeFeature();
		return remoteFeature;
	}

	public void testFileSite() throws Exception {

		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature remoteFeature = getFeature1(remoteSite);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE);
		localSite.install(remoteFeature, null);

		// verify
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));
		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(site, AbstractSite.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin files not installed locally", pluginFile.exists());

		File featureFile = new File(site, FileSite.INSTALL_FEATURE_PATH + remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());
		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);

	}
	private IFeature getFeature2(ISite site) throws MalformedURLException, CoreException {
		URL id = UpdateManagerUtils.getURL(site.getURL(), "features/features2.jar", null);
		DefaultPackagedFeature remoteFeature = new DefaultPackagedFeature(id, site);
		remoteFeature.initializeFeature();
		return remoteFeature;
	}

	public void testHTTPSite() throws Exception {

		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		IFeature remoteFeature = null;

		if (features == null || features.length == 0)
			fail("No features on the site");

		for (int i = 0; i < features.length; i++) {
			if (features[i].getURL().toExternalForm().endsWith("features2.jar")) {
				remoteFeature = features[i].getFeature();
				break;
			}
		}

		assertNotNull("Cannot find feature2.jar on site", remoteFeature);
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE);
		localSite.install(remoteFeature, null);

		String site = localSite.getURL().getPath();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(site, AbstractSite.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin info not installed locally", pluginFile.exists());

		File featureFile = new File(site, FileSite.INSTALL_FEATURE_PATH + remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());

		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);
	}

	public void testInstall() throws Exception {

		URL INSTALL_SITE = null;
		try {
			INSTALL_SITE = new URL("http", bundle.getString("HTTP_HOST_1"), bundle.getString("HTTP_PATH_2"));
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}

		ISite remoteSite = SiteManager.getSite(INSTALL_SITE);
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		IFeature remoteFeature = null;

		if (features == null || features.length == 0)
			fail("No features on the site");

		for (int i = 0; i < features.length; i++) {
			if (features[i].getURL().toExternalForm().endsWith("helpFeature.jar")) {
				remoteFeature = features[i].getFeature();
				break;
			}
		}

		assertNotNull("Cannot find help.jar on site", remoteFeature);
		ISite localSite = SiteManager.getLocalSite();
		localSite.install(remoteFeature, null);

		String site = localSite.getURL().getPath();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(site, AbstractSite.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin info not installed locally", pluginFile.exists());

		File featureFile = new File(site, FileSite.INSTALL_FEATURE_PATH + remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());

		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);
	}
	
	
	public void testFileSiteWithoutSiteXML() throws Exception {
		ISite remoteSite = SiteManager.getTempSite();
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		if (features.length!=0) fail("The site contains feature");
	}
	
	
}