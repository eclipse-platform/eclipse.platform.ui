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
			System.out.println("Notified DefaultFeature Installed");
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
		URL url = UpdateManagerUtils.getURL(site.getURL(), "features/org.eclipse.update.core.tests.feature1_1.0.4.jar", null);
		FeatureReference ref = new FeatureReference();
		ref.setSite(site);
		ref.setURLString("features/org.eclipse.update.core.tests.feature1_1.0.4.jar");
		ref.resolve(site.getURL(),null);
		//Feature remoteFeature = createPackagedFeature(url,site);
		//remoteFeature.setFeatureIdentifier("org.eclipse.update.core.tests.feature1");
		//remoteFeature.setFeatureVersion("1.0.4");		
		return ref.getFeature();
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
		File pluginXMLFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName+File.separator + "plugin.xml");
		assertTrue("plugin.xml file not installed locally", pluginXMLFile.exists());

		File featureFile = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString());
		assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());
		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);
		UpdateManagerUtils.removeFromFileSystem(new File(localSite.getURL().getFile()));

	}
	
	/**
	 * 
	 */
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
		
		// feature2.jar should not be in the local site
		IFeatureReference[] localFeatures = localSite.getFeatureReferences();		
		if (localFeatures == null || localFeatures.length == 0)
			fail("No features on the target site");

		boolean found = false;
		for (int i = 0; i < localFeatures.length; i++) {
			if (features[i].getURL().toExternalForm().endsWith("features2.jar")) {
				found= true;
				break;
			}
		}

		assertTrue("Found feature2.jar on target site. Target site feature ref shouldnot contain JAR file", !found);


		// check
		String site = UpdateManagerUtils.getPath(localSite.getURL());			
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin info not installed locally", pluginFile.exists());

		File featureFile = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());

		localSite.save();

		//cleanup
		UpdateManagerUtils.removeFromFileSystem(pluginFile);
		UpdateManagerUtils.removeFromFileSystem(new File(localSite.getURL().getFile()));		
	}

	public void testInstall() throws Exception {
		
		// cleanup local files...
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		

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
		IConfigurationSite site = localSite.getCurrentConfiguration().getConfigurationSites()[0];
		Listener listener = new Listener();
		site.getSite().addSiteChangedListener(listener);
		
		site.getSite().install(remoteFeature, null);


		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry", (entries != null && entries.length != 0));

		String sitePath = UpdateManagerUtils.getPath(site.getSite().getURL());			
		String pluginName = entries[0].getIdentifier().toString();
		File pluginFile = new File(sitePath, Site.DEFAULT_PLUGIN_PATH + pluginName);
		assertTrue("plugin info not installed locally", pluginFile.exists());

		File featureFile = new File(sitePath, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString());
		assertTrue("feature info not installed locally", featureFile.exists());

		//cleanup
		File file = new File(site.getSite().getURL().getFile()+File.separator+Site.INSTALL_FEATURE_PATH+remoteFeature.getVersionIdentifier());
		UpdateManagerUtils.removeFromFileSystem(file);
		UpdateManagerUtils.removeFromFileSystem(pluginFile);
		UpdateManagerUtils.removeFromFileSystem(localFile);		

		site.getSite().removeSiteChangedListener(listener);
		assertTrue("Listener hasn't received notification",listener.isNotified());
	}
	
	
	public void testFileSiteWithoutSiteXML() throws Exception {
		
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature remoteFeature = getFeature1(remoteSite);
		IConfigurationSite localSite = SiteManager.getLocalSite().getCurrentConfiguration().getConfigurationSites()[0];
		localSite.getSite().install(remoteFeature, null);

		IFeatureReference[] features = localSite.getSite().getFeatureReferences();
		if (features.length==0) fail("The local site does not contain feature, should not contain an XML file but features should be found anyway by parsing");
		if (localSite.getSite().getArchives().length==0) fail("The local site does not contain archives, should not contain an XML file but archives should be found anyway by parsing");
		
		//cleanup
		File file = new File(localSite.getSite().getURL().getFile()+File.separator+Site.INSTALL_FEATURE_PATH+remoteFeature.getVersionIdentifier());
		UpdateManagerUtils.removeFromFileSystem(file);
		file = new File(localSite.getSite().getURL().getFile()+File.separator+Site.DEFAULT_PLUGIN_PATH+"org.eclipse.update.core.tests.feature1.plugin1_3.5.6");
		UpdateManagerUtils.removeFromFileSystem(file);
		file = new File(localSite.getSite().getURL().getFile()+File.separator+Site.DEFAULT_PLUGIN_PATH+"org.eclipse.update.core.tests.feature1.plugin2_5.0.0");
		UpdateManagerUtils.removeFromFileSystem(file);
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		
		try {
			ISite site = SiteManager.getSite(new URL("http://www.eclipse.org/"));
			fail("The site contains site.xml... it should be an HTTP site without an XML file");			
		} catch (CoreException e){
			// expected
		}

		
	}
	
	
		/**
	 * 
	 */
	private Feature createPackagedFeature(URL url,ISite site) throws CoreException {
		String packagedFeatureType = site.getDefaultInstallableFeatureType();
		Feature result = null;
		if (packagedFeatureType != null) {
			IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(packagedFeatureType);
			result = (Feature)factory.createFeature(url, site);
		}
		return result;
	}
	/*
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultInstallableFeatureType() {
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier()+".";
		return pluginID+IFeatureFactory.INSTALLABLE_FEATURE_TYPE;
	}	
	
}