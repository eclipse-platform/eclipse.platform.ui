package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.FeatureReference;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestFeatureParse extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestFeatureParse(String arg0) {
		super(arg0);
	}
	
	
	public void testParse(){
	
		String xmlFile = "xmls/feature_1.0.0/";
		try {		
			ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
			URL url = UpdateManagerUtils.getURL(remoteSite.getURL(),xmlFile,null);
			
			FeatureReference ref = new FeatureReference();
			ref.setSite(remoteSite);
			ref.setURL(url);
			IFeature feature = ref.getFeature();
			
			String prov = feature.getProvider();
			assertEquals("Object Technology International",prov);
			
		} catch (Exception e){
			fail(e.toString());
			e.printStackTrace();
		}
	}
}

