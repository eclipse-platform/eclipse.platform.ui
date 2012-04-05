/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSpaceInInstall extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestSpaceInInstall(String arg0) {
		super(arg0);
	}

	/**
	 *
	 */
	public void testSpaceInURL() throws Exception {

		//cleanup target 
		URL testURL = new URL(TARGET_FILE_SITE,"test site space/"); 
		String testPath = testURL.getFile();
		File target = new File(testPath);
		UpdateManagerUtils.removeFromFileSystem(target);
		
		URL newURL = new File(dataPath + "Site with space/site.xml").toURL();
		ISite remoteSite = SiteManager.getSite(newURL,true,null);
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		if (!target.exists()) target.mkdirs();
		ISite localSite = SiteManager.getSite(testURL,true,null);
		IFeature remoteFeature = null;
		
		// at least one executable feature and on packaged
		boolean execFeature = false;
		boolean packFeature = false;

		if (featuresRef.length==0) fail ("no feature found");
	
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature(null);
			remove(remoteFeature,localSite); 
			localSite.install(remoteFeature, null,null);
			
			if (remoteFeature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider) packFeature = true;
			if (remoteFeature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider) execFeature = true;

			// verify
			String site = testPath;
			IPluginEntry[] entries = remoteFeature.getRawPluginEntries();
			assertTrue("no plugins entry", (entries != null && entries.length != 0));
			String pluginName = entries[0].getVersionedIdentifier().toString();
			File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			assertTrue("plugin files not installed locally (pluginFile=" + pluginFile + ",target=" + target + ')', pluginFile.exists());

			File featureFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString());
			assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());

			File featureFileXML = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString() + File.separator + "feature.xml");
			assertTrue("feature info not installed locally: no feature.xml", featureFileXML.exists());
		}

		if (!execFeature && !packFeature){
			fail("cannot find one executable and one package feature on teh site");
		}

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);


	}
	
	
	/**
	 *
	 */
	public void testSpaceInHTTPURL() throws Exception {

		//cleanup target  
		File target = new File(TARGET_FILE_SITE.getFile());
		UpdateManagerUtils.removeFromFileSystem(target);
		InstallRegistry.cleanup();
		
		String path = bundle.getString("HTTP_PATH_3");
		ISite remoteSite = SiteManager.getSite(new URL("http",getHttpHost(),getHttpPort(),path),true,null);		
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,true,null);
		IFeature remoteFeature = null;
		
		// at least one executable feature and on packaged
		boolean execFeature = false;
		boolean packFeature = false;

		if (featuresRef.length==0) fail ("no feature found");
	
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature(null);
			localSite.install(remoteFeature, null,null);
			
			if (remoteFeature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider) packFeature = true;
			if (remoteFeature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider) execFeature = true;

			// verify
			String site = TARGET_FILE_SITE.getFile();			
			IPluginEntry[] entries = remoteFeature.getRawPluginEntries();
			assertTrue("no plugins entry", (entries != null && entries.length != 0));
			String pluginName = entries[0].getVersionedIdentifier().toString();
			File pluginFile = new File(site, Site.DEFAULT_PLUGIN_PATH + pluginName);
			assertTrue("plugin files not installed locally", pluginFile.exists());

			File featureFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString());
			assertTrue("feature info not installed locally:"+featureFile, featureFile.exists());

			File featureFileXML = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString() + File.separator + "feature.xml");
			assertTrue("feature info not installed locally: no feature.xml", featureFileXML.exists());
		}

		if (!execFeature && !packFeature){
			fail("cannot find one executable and one package feature on teh site");
		}

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);
		InstallRegistry.cleanup();

	}
		
		
}
