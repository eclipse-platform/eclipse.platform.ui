package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestExecutablePackagedInstall extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestExecutablePackagedInstall(String arg0) {
		super(arg0);
	}

	/**
	 * With site.xml
	 */
	public void testFilePackageExecutableFeatureSite() throws Exception {

		//cleanup target  
		File target = new File(TARGET_FILE_SITE.getFile());
		UpdateManagerUtils.removeFromFileSystem(target);
		
		URL newURL = new URL("file", null, dataPath + "ExecutableFeaturePackagedSite/data2/");
		ISite remoteSite = SiteManager.getSite(newURL);
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE);
		IFeature remoteFeature = null;
		
		// at least one executable feature and on packaged
		boolean execFeature = false;
		boolean packFeature = false;

		if (featuresRef.length==0) fail ("no feature found");
	
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature();
			localSite.install(remoteFeature, null);
			
			if (remoteFeature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider) packFeature = true;
			if (remoteFeature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider) execFeature = true;

			// verify
			String site = localSite.getURL().getFile();
			IPluginEntry[] entries = remoteFeature.getPluginEntries();
			assertTrue("no plugins entry", (entries != null && entries.length != 0));
			String pluginName = entries[0].getIdentifier().toString();
			File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			assertTrue("plugin files not installed locally", pluginFile.exists());

			File featureFile = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString());
			assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());

			File featureFileXML = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString() + File.separator + "feature.xml");
			assertTrue("feature info not installed locally: no feature.xml", featureFileXML.exists());
		}

		if (!execFeature && !packFeature){
			fail("cannot find one executable and one package feature on teh site");
		}

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);


	}
	
	
	/**
	 * Without site.xml
	 */
	public void testFileNoSiteXMLSite() throws Exception {

		//cleanup target 
		File target = new File(TARGET_FILE_SITE.getFile());
		UpdateManagerUtils.removeFromFileSystem(target);
		
		URL newURL = new URL("file", null, dataPath + "ExecutableFeaturePackagedSite/data/");
		ISite remoteSite = SiteManager.getSite(newURL);
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE);
		IFeature remoteFeature = null;
		
		// at least one executable feature and on packaged
		boolean execFeature = false;
		boolean packFeature = false;

		if (featuresRef.length==0) fail ("no feature found");
	
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature();
			localSite.install(remoteFeature, null);
			
			if (remoteFeature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider) packFeature = true;
			if (remoteFeature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider) execFeature = true;

			// verify
			String site = localSite.getURL().getFile();
			IPluginEntry[] entries = remoteFeature.getPluginEntries();
			assertTrue("no plugins entry", (entries != null && entries.length != 0));
			String pluginName = entries[0].getIdentifier().toString();
			File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			assertTrue("plugin files not installed locally", pluginFile.exists());

			File featureFile = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString());
			assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());

			File featureFileXML = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString() + File.separator + "feature.xml");
			assertTrue("feature info not installed locally: no feature.xml", featureFileXML.exists());
		}

		if (!execFeature && !packFeature){
			fail("cannot find one executable and one package feature on teh site");
		}

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);


	}
	
}