package org.eclipse.update.tests.types;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
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
	public void testSimplePackagedFeatureType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory("org.eclipse.update.core.jar");
		
		ISite site = SiteManager.getSite(SOURCE_FILE_SITE);
		URL featureURL = new URL(SOURCE_FILE_SITE,"features/features2.jar ");
		
		IFeature anotherFeature = factory.createFeature(featureURL,site);
		
		//assertTrue("Factory doesn't create same feature",anotherFeature.getIdentifier().equals(anotherFeature.getIdentifier()));
	}	
	
		/**
	 * @throws Exception
	 */
	public void testSimpleExecutableFeatureType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory("org.eclipse.update.core.exe");
		
		ISite site = SiteManager.getSite(SOURCE_FILE_SITE);
		URL featureURL = new URL(SOURCE_FILE_SITE,"install/features/feature3/");
		
		IFeature anotherFeature = factory.createFeature(featureURL,site);
		
		//assertTrue("Factory doesn't create same feature",anotherFeature.getIdentifier().equals(anotherFeature.getIdentifier()));
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
		
		assertTrue("Factory doesn't create same feature",feature.getVersionIdentifier().equals(anotherFeature.getVersionIdentifier()));
	}
	
	
	/**
	 * @throws Exception
	 */
	public void testFeatureNewType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory("org.eclipse.update.core.exe");
		
		String featurePath = dataPath+"FeatureTypeExamples/site1/";
		ISite site = SiteManager.getSite(new URL("file",null,featurePath));
		IFeatureReference ref = site.getFeatureReferences()[0];
		IFeature feature = ref.getFeature();
		
		IFeature anotherFeature = factory.createFeature(feature.getURL(),site);

		assertTrue(feature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider);		
		assertTrue(((FeatureReference)ref).getType().equals("org.eclipse.update.tests.core.feature1"));		
		assertTrue("Factory doesn't create same feature",feature.getVersionIdentifier().equals(anotherFeature.getVersionIdentifier()));


	}
	
	
		/**
	 * @throws Exception
	 */
	public void testFeatureAnotherType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory("org.eclipse.update.core.jar");
		
		String featurePath = dataPath+"FeatureTypeExamples/site2/";
		ISite site = SiteManager.getSite(new URL("file",null,featurePath));
		IFeatureReference ref = site.getFeatureReferences()[0];		
		IFeature feature = ref.getFeature();
		
		IFeature anotherFeature = factory.createFeature(feature.getURL(),site);
		
		assertTrue("Factory doesn't create same feature",feature.getVersionIdentifier().equals(anotherFeature.getVersionIdentifier()));
		assertTrue(feature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider);
		assertTrue(((FeatureReference)ref).getType().equals("org.eclipse.update.core.jar"));
		
	}
		
		
	/**
	 * @throws Exception
	 */
	public void testFeatureUnknownType() throws Exception{ 
		String featurePath = dataPath+"FeatureTypeExamples/site3/";
		ISite site = SiteManager.getSite(new URL("file",null,featurePath));
		IFeatureReference ref = site.getFeatureReferences()[0];		
		try {
			IFeature feature = ref.getFeature();
			assertTrue("id found, should be found",false);
		} catch (CoreException e){
			if (!e.getMessage().equals("Cannot find feature factory for id: org.eclipse.update.core.unknowntype.jar")){
				throw e;
			}
		}
		
	}		
		
}

