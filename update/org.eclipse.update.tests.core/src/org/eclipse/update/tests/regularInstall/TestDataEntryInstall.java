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
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestDataEntryInstall extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestDataEntryInstall(String arg0) {
		super(arg0);
	}

	/**
	 * With site.xml
	 */
	public void testDataEntrySite() throws Exception {

		//cleanup target 
		File target = new File(TARGET_FILE_SITE.getFile());
		UpdateManagerUtils.removeFromFileSystem(target);
		
		URL newURL =new File(dataPath + "dataEntrySiteTest/site1/site.xml").toURL();
		ISite remoteSite = SiteManager.getSite(newURL,null);
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
		IFeature remoteFeature = null;
		
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature(null);
			localSite.install(remoteFeature, null,null);

			// verify
			String site = localSite.getURL().getFile();
			INonPluginEntry[] entries = remoteFeature.getNonPluginEntries();
			assertTrue("no data entry", (entries != null && entries.length != 0));
			String pluginName = entries[0].getIdentifier().toString();
			File pluginFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString()+File.separator+ pluginName);
			assertTrue("data files not installed locally:"+pluginFile, pluginFile.exists());
		} 

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);


	}
	
	
	/**
	 * With site.xml
	 */
	public void testDataEntrySitePackaged() throws Exception {

		//cleanup target 
		File target = new File(TARGET_FILE_SITE.getFile());
		UpdateManagerUtils.removeFromFileSystem(target);
		
		URL newURL =new File(dataPath + "dataEntrySiteTest/site2/site.xml").toURL();
		ISite remoteSite = SiteManager.getSite(newURL,null);
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE,null);
		IFeature remoteFeature = null;
		
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature(null);
			localSite.install(remoteFeature, null,null);

			// verify
			String site = localSite.getURL().getFile();
			INonPluginEntry[] entries = remoteFeature.getNonPluginEntries();
			assertTrue("no data entry", (entries != null && entries.length != 0));
			entries[0].getIdentifier().toString();
			File pluginFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString()+File.separator+ "nondata"+File.separator+"file1.zip");
			assertTrue("Data file inside the jar not installed locally:"+pluginFile, pluginFile.exists());
			pluginFile = new File(site, Site.DEFAULT_INSTALLED_FEATURE_PATH + remoteFeature.getVersionedIdentifier().toString()+File.separator+ "file.zip");
			assertTrue("Data file outside the jar not installed locally:"+pluginFile, pluginFile.exists());
			
		} 
		

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);


	}	
	
}
