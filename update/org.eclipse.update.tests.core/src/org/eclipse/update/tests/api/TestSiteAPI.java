package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestSiteAPI(String arg0) {
		super(arg0);
	}
	
	public void testURL() throws Exception {

		ISite site = SiteManager.getSite(SOURCE_FILE_SITE);
		assertEquals("/"+site.getURL().getFile(),SOURCE_FILE_SITE.getFile());
		
		ISite site2 = SiteManager.getSite(SOURCE_HTTP_SITE);
		assertEquals(site2.getURL(),new URL("http", getHttpHost(),getHttpPort(), bundle.getString("HTTP_PATH_1")+"site.xml"));

	}

}

