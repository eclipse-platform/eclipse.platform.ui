package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestSiteAPI(String arg0) {
		super(arg0);
	}
	
	public void testURL() throws Exception {

		AbstractSite site = (AbstractSite)SiteManager.getSite(SOURCE_FILE_SITE);
		assertEquals(site.getURL(),SOURCE_FILE_SITE);
		
		AbstractSite site2 = (AbstractSite)SiteManager.getSite(SOURCE_FILE_SITE);
		assertEquals(site2.getURL(),SOURCE_FILE_SITE);

	}

}

