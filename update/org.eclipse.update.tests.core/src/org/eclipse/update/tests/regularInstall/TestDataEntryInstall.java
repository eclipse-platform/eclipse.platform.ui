package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
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
		
		URL newURL = new URL("file", null, dataPath + "dataEntrySiteTest/");
		ISite remoteSite = SiteManager.getSite(newURL);
		IFeatureReference[] featuresRef = remoteSite.getFeatureReferences();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE);
		IFeature remoteFeature = null;
		
		for (int i = 0; i < featuresRef.length; i++) {
			remoteFeature = featuresRef[i].getFeature();
			localSite.install(remoteFeature, null);

			// verify
			String site = localSite.getURL().getFile();
			INonPluginEntry[] entries = remoteFeature.getNonPluginEntries();
			assertTrue("no data entry", (entries != null && entries.length != 0));
			String pluginName = entries[0].getIdentifier().toString();
			File pluginFile = new File(site, Site.INSTALL_FEATURE_PATH + remoteFeature.getVersionIdentifier().toString()+File.separator+ pluginName);
			assertTrue("data files not installed locally:"+pluginFile, pluginFile.exists());
		} 

		//cleanup target 
		UpdateManagerUtils.removeFromFileSystem(target);


	}
	
	
}