/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.regularInstall;

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
		
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE,null);
		IFeatureReference[] remoteFeatures = remoteSite.getFeatureReferences();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getURL().toExternalForm());
		}
	}


	public void testFeatureHTTPSite() throws Exception{ 
		
		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE,null);
		IFeatureReference[] remoteFeatures = remoteSite.getFeatureReferences();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getURL().toExternalForm());
		}
	}
}


