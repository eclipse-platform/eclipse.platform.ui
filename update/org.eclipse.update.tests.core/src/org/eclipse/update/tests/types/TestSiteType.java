package org.eclipse.update.tests.types;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.FeatureExecutable;
import org.eclipse.update.internal.core.FeatureTypeFactory;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteType extends UpdateManagerTestCase {

	/**
	 * Test the getFeatures()
	 */
	public TestSiteType(String arg0) {
		super(arg0);
	}
	
	
	/**
	 * @throws Exception
	 */
	public void testSiteType() throws Exception{ 
		
		String featurePath = dataPath+"SiteTypeExamples/site1/";
		ISite site = SiteManager.getSite(new URL("file",null,featurePath));
		IFeatureReference ref = site.getFeatureReferences()[0];
		IFeature feature = ref.getFeature();
	
		assertTrue(site instanceof SiteURL);		
		assertTrue(!(site instanceof SiteFile));
		assertTrue(((Site)site).getType().equals("org.eclipse.update.core.http"));		
		assertTrue(feature instanceof FeaturePackaged);
		assertTrue(((FeatureReference)ref).getFeatureType().equals("org.eclipse.update.core.jar"));		

	}
	
	
		}

