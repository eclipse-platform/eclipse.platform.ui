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

public class TestExecutableInstall extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestExecutableInstall(String arg0) {
		super(arg0);
	}
	


	public void testFileSite() throws Exception{
		
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature remoteFeature = remoteSite.getFeatureReferences()[0].getFeature();
		ISite localSite = SiteManager.getSite(TARGET_FILE_SITE);
		localSite.install(remoteFeature,null);
		
		// verify
		String site = localSite.getURL().getFile();
		IPluginEntry[] entries = remoteFeature.getPluginEntries();
		assertTrue("no plugins entry",(entries!=null && entries.length!=0));
		String pluginName= entries[0].getIdentifier().toString();
		File pluginFile = new File(site,Site.DEFAULT_PLUGIN_PATH+pluginName);
		assertTrue("plugin files not installed locally",pluginFile.exists());

		File featureFile = new File(site,SiteFile.INSTALL_FEATURE_PATH+remoteFeature.getIdentifier().toString());
		assertTrue("feature info not installed locally",featureFile.exists());
		
		File featureFileXML = new File(site,SiteFile.INSTALL_FEATURE_PATH+remoteFeature.getIdentifier().toString()+File.separator+"feature.xml");
		assertTrue("feature info not installed locally: no feature.xml",featureFileXML.exists());
	}
}

