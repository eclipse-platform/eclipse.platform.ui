package org.eclipse.update.tests.types;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.FeaturePackagedContentProvider;
import org.eclipse.update.internal.core.SiteURLContentProvider;
import org.eclipse.update.tests.UpdateManagerTestCase;
import org.eclipse.update.tests.implementation.SiteFTPFactory;

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
	
		assertTrue(site.getSiteContentProvider() instanceof SiteURLContentProvider);		
		assertTrue(((Site)site).getType().equals("org.eclipse.update.core.http"));		
		assertTrue(feature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider);
		
		// FIXME: cannot set type after feature a=has been created ? markAsReadOnly ?
		//assertTrue(((FeatureReference)ref).getType().equals("org.eclipse.update.core.jar"));		

	}
	
	
/**
	 * @throws Exception
	 */
	public void testFTPSiteType() throws Exception{ 
		
		String featurePath = dataPath+"SiteTypeExamples/site1/";
		ISite site = SiteManager.getSite(new URL("ftp://theguest:theguest@eclipse3.torolab.ibm.com/"));
		
		// should not find the mapping
		// but then should attempt to read the XML file
		// found a new type
		// call the new type
		assertTrue("Wrong site type",site.getType().equals("org.eclipse.update.tests.ftp"));		
		assertTrue("Wrong file",site.getURL().getFile().equals("/"+SiteFTPFactory.FILE));
	
	}	
	
		}

