package org.eclipse.update.tests.api;

import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestSiteAPI(String arg0) {
		super(arg0);
	}
	
	public void testURL(){

		AbstractSite site = (AbstractSite)new FileSite(SOURCE_FILE_SITE);
		assertEquals(site.getURL(),SOURCE_FILE_SITE);
		
		AbstractSite site2 = (AbstractSite)new URLSite(SOURCE_FILE_SITE);
		assertEquals(site2.getURL(),SOURCE_FILE_SITE);

	}

}

