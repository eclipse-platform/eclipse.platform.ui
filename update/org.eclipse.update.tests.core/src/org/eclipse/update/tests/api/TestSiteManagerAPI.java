package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
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

	public void testHttp() throws Exception {
		ISite httpSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		assertEquals(SOURCE_HTTP_SITE, httpSite.getURL());
	}
	
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
		ISite site = SiteManager.getLocalSite();
		System.out.println("TEST LOCAL SITE TODO:"+site.getURL().toExternalForm());
		//FIXME: what kind of test for localSite API
		// I cannot test the URL as it doesn't mean anything
		// for a Real Local Site
	}
	

}

