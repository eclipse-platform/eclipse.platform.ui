package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.SiteFile;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteManagerAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestSiteManagerAPI(String arg0) {
		super(arg0);
	}
	
	public void testFile() throws Exception {
		ISite fileSite = SiteManager.getSite(TARGET_FILE_SITE);
		assertEquals(TARGET_FILE_SITE, fileSite.getURL());
	}

	/*public void testHttp() throws Exception {
		ISite httpSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		assertEquals(SOURCE_HTTP_SITE, httpSite.getURL());
	}*/
	
	public void testUnknown() throws Exception {
		URL url = new URL("ftp://255.255.255.255/");
		boolean ok = false;
		try {
			ISite httpSite = SiteManager.getSite(url);
		} catch (CoreException e){
			ok = true;
		}
		if (!ok) fail();
	}
	
	public void testLocalSite() throws Exception {
		
		
		ILocalSite site = SiteManager.getLocalSite();
		ISite[] instSites = site.getCurrentConfiguration().getInstallSites();
		assertTrue(instSites.length>0);
		System.out.println("Local Site:"+instSites[0].getURL().toExternalForm());
		
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature remoteFeature = remoteSite.getFeatureReferences()[0].getFeature();
		instSites[0].install(remoteFeature,null);
		
		IFeatureReference[] features = site.getCurrentConfiguration().getFeatures();
		assertTrue(features.length>0);

		//cleanup
		File file = new File(instSites[0].getURL().getFile()+File.separator+SiteFile.INSTALL_FEATURE_PATH+remoteFeature.getIdentifier());
		UpdateManagerUtils.removeFromFileSystem(file);
		file = new File(instSites[0].getURL().getFile()+File.separator+SiteFile.DEFAULT_PLUGIN_PATH+"org.eclipse.update.plugin1_1.1.1");
		UpdateManagerUtils.removeFromFileSystem(file);		

	}
	

}

