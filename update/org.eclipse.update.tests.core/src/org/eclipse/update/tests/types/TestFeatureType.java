package org.eclipse.update.tests.types;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.FeatureTypeFactory;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestFeatureType extends UpdateManagerTestCase {

	/**
	 * Test the getFeatures()
	 */
	public TestFeatureType(String arg0) {
		super(arg0);
	}
	
	
	/**
	 * @throws Exception
	 */
	public void testFeatureType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory("org.eclipse.update.core.jar");
		
		ISite site = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeature feature = site.getFeatureReferences()[0].getFeature();
		
		IFeature anotherFeature = factory.createFeature(feature.getURL(),site);
		
		assertTrue("Factory doesn't create same feature",feature.getIdentifier().equals(anotherFeature.getIdentifier()));
	}
		
}

