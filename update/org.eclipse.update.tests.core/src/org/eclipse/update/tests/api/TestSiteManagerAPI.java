package org.eclipse.update.tests.api;

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
		URL url = new URL(TARGET_FILE_SITE);
		ISite fileSite = SiteManager.getSite(url);
		assertEquals(url, fileSite.getURL());
	}

	public void testHttp() throws Exception {
		URL url = new URL(SOURCE_HTTP_SITE);
		ISite httpSite = SiteManager.getSite(url);
		assertEquals(url, httpSite.getURL());
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
		System.out.println(site.getURL().toExternalForm());
		//FIXME: what kind of test for localSite API
		// I cannot test the URL as it doesn't mean anything
		// for a Real Local Site
	}
	

}

