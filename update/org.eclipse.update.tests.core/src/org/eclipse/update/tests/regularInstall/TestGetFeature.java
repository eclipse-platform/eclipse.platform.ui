package org.eclipse.update.tests.regularInstall;

import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.ISite;
import org.eclipse.update.internal.core.URLSite;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestGetFeature extends UpdateManagerTestCase {
	/**
	 * Test the getFeatures()
	 */
	public TestGetFeature(String arg0) {
		super(arg0);
	}
	
	
	public void testFileSite() throws Exception{
		
		ISite remoteSite = new URLSite(SOURCE_FILE_SITE);
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier());
		}
	}

	public void testHTTPSite() throws Exception{ 
		
		ISite remoteSite = new URLSite(SOURCE_HTTP_SITE);
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier());
		}
	}
}

