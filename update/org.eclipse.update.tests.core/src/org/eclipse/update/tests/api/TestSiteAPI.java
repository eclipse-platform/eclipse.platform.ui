package org.eclipse.update.tests.api;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.update.core.AbstractSite;
import org.eclipse.update.internal.core.FileSite;
import org.eclipse.update.internal.core.URLSite;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestSiteAPI(String arg0) {
		super(arg0);
	}
	
	public void testURL(){
		try {
		URL url = new URL(SOURCE_FILE_SITE);
		AbstractSite site = (AbstractSite)new FileSite(url);
		assertEquals(site.getURL(),url);
		
		AbstractSite site2 = (AbstractSite)new URLSite(url);
		assertEquals(site2.getURL(),url);
		} catch (MalformedURLException e){
			fail("URL error: cannot create URL");
		}
	}

}

