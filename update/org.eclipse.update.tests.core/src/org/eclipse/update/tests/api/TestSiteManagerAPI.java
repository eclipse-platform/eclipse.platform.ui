package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
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
		fail("Connected to ftp://255.255.255.255/, should not happen");
		} catch (CoreException e){
			// expected
		}
	}
	
	public void testLocalSite() throws Exception {
		
		ILocalSite site = SiteManager.getLocalSite();
		IConfigurationSite[] instSites = site.getCurrentConfiguration().getConfigurationSites();
		assertTrue(instSites.length>0);
		System.out.println("Local Site:"+instSites[0].getSite().getURL().toExternalForm());
		
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature remoteFeature = remoteSite.getFeatureReferences()[0].getFeature();
		instSites[0].getSite().install(remoteFeature,null);
		
		IFeatureReference[] features = site.getCurrentConfiguration().getConfigurationSites()[0].getSite().getFeatureReferences();
		assertTrue(features.length>0);

		//cleanup
		File file = new File(instSites[0].getSite().getURL().getFile()+File.separator+Site.INSTALL_FEATURE_PATH+remoteFeature.getVersionIdentifier());
		UpdateManagerUtils.removeFromFileSystem(file);
		file = new File(instSites[0].getSite().getURL().getFile()+File.separator+Site.DEFAULT_PLUGIN_PATH+"org.eclipse.update.plugin1_1.1.1");
		UpdateManagerUtils.removeFromFileSystem(file);		
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		

	}
	

}

