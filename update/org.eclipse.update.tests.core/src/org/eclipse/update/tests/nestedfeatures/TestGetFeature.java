package org.eclipse.update.tests.nestedfeatures;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestGetFeature extends UpdateManagerTestCase {
	/**
	 * Test the getFeatures()
	 */
	public TestGetFeature(String arg0) {
		super(arg0);
	}
	
	
	public void testFeatureFileSite() throws Exception{
		
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeatureReference[] remoteFeatures = remoteSite.getFeatureReferences();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getURL().toExternalForm());
		}
	}

	public void testFeatureHTTPSite() throws Exception{ 
		
		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeatureReference[] remoteFeatures = remoteSite.getFeatureReferences();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getURL().toExternalForm());
		}
	}
}

