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
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestInstall extends UpdateManagerTestCase {
	
	
	public class Listener implements ISiteChangedListener{
		
		public boolean notified = false;
		/*
		 * @see ISiteChangedListener#featureUpdated(IFeature)
		 */
		public void featureUpdated(IFeature feature) {}

			/*
		 * @see ISiteChangedListener#featureInstalled(IFeature)
		 */
		public void featureInstalled(IFeature feature) {
			notified = true;
			System.out.println("Notified Feature Installed");
		}

		/*
		 * @see ISiteChangedListener#featureUninstalled(IFeature)
		 */
		public void featureUninstalled(IFeature feature) {}

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

	private IFeature getFeature1(ISite site) throws MalformedURLException, CoreException {
		URL id = UpdateManagerUtils.getURL(site.getURL(), "features/org.eclipse.update.core.tests.feature1_1.0.4.jar", null);
		FeaturePackaged remoteFeature = new FeaturePackaged(id, site);
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
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin files not installed locally", pluginFile.exists());

		File featureFile = new File(site, SiteFile.INSTALL_FEATURE_PATH + remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());
		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);

	}
	private IFeature getFeature2(ISite site) throws MalformedURLException, CoreException {
		URL id = UpdateManagerUtils.getURL(site.getURL(), "features/features2.jar", null);
		FeaturePackaged remoteFeature = new FeaturePackaged(id, site);
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

		String site = UpdateManagerUtils.getPath(localSite.getURL());			
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin info not installed locally", pluginFile.exists());

		File featureFile = new File(site, SiteFile.INSTALL_FEATURE_PATH + remoteFeature.getIdentifier().toString());
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
		ILocalSite localSite = SiteManager.getLocalSite();
		ISite site = localSite.getCurrentConfiguration().getInstallSites()[0];
		Listener listener = new Listener();
		site.addSiteChangedListener(listener);
		
		site.install(remoteFeature, null);


		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String sitePath = UpdateManagerUtils.getPath(site.getURL());			
		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(sitePath, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin info not installed locally", pluginFile.exists());

		File featureFile = new File(sitePath, SiteFile.INSTALL_FEATURE_PATH + remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());

		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);
		
		assertTrue("Listener hasn't received notification",listener.isNotified());
	}
	
	
	public void testFileSiteWithoutSiteXML() throws Exception {
		
		
		ISite remoteSite = SiteManager.getLocalSite().getCurrentConfiguration().getInstallSites()[0];
		IFeatureReference[] features = remoteSite.getFeatureReferences();
		if (features.length==0) fail("The local site does not contain feature, should not contain an XML file but features should be found anyway by parsing");
		
		remoteSite = SiteManager.getSite(new URL("http://www.eclipse.org/"));
		features = remoteSite.getFeatureReferences();
		if (features.length!=0) fail("The site contains feature... it is an HTTP site without an XML file, so it should not contain any features");
		
	}
	
}