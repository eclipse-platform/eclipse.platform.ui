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
package org.eclipse.update.tests.types;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;


public class TestFeatureType extends UpdateManagerTestCase {


	private static final String PACKAGED_FEATURE_TYPE = "packaged"; //$NON-NLS-1$
	private static final String INSTALLED_FEATURE_TYPE = "installed"; //$NON-NLS-1$	
	
	/**
	 * Test the getFeatures()
	 */
	public TestFeatureType(String arg0) {
		super(arg0);
	}
	
	
	public String getDefaultInstallableFeatureType() {
		String pluginID = UpdateCore.getPlugin().getDescriptor().getUniqueIdentifier() + "."; //$NON-NLS-1$
		return pluginID + PACKAGED_FEATURE_TYPE;
	}
	
	public String getDefaultExecutableFeatureType() {
		String pluginID = UpdateCore.getPlugin().getDescriptor().getUniqueIdentifier() + "."; //$NON-NLS-1$
		return pluginID + INSTALLED_FEATURE_TYPE;
	}		
	
	/**
	 * @throws Exception
	 */
	public void testSimplePackagedFeatureType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory(getDefaultInstallableFeatureType());
		
		ISite site = SiteManager.getSite(SOURCE_FILE_SITE,null);
		URL featureURL = new URL(SOURCE_FILE_SITE,"features/features2.jar ");
		
		IFeature anotherFeature = factory.createFeature(featureURL,site,null);
		
		assertTrue("Factory doesn't create same feature",anotherFeature.getVersionedIdentifier().equals(anotherFeature.getVersionedIdentifier()));
	}	
	
		/**
	 * @throws Exception
	 */
	public void testSimpleExecutableFeatureType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory(getDefaultExecutableFeatureType());
		
		ISite site = SiteManager.getSite(SOURCE_FILE_SITE,null);
		URL featureURL = new URL(SOURCE_FILE_SITE,"testAPI/"+Site.DEFAULT_INSTALLED_FEATURE_PATH+"feature3/");
		
		IFeature anotherFeature = factory.createFeature(featureURL,site,null);
		
		assertTrue("Factory doesn't create same feature",anotherFeature.getVersionedIdentifier().equals(anotherFeature.getVersionedIdentifier()));
	}	
	
	/**
	 * @throws Exception
	 */
	public void testFeatureType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory(getDefaultInstallableFeatureType());
		
		ISite site = SiteManager.getSite(SOURCE_HTTP_SITE,null);
		IFeature feature = site.getFeatureReferences()[0].getFeature(null);
		
		IFeature anotherFeature = factory.createFeature(feature.getURL(),site,null);
		
		assertTrue("Factory doesn't create same feature",feature.getVersionedIdentifier().equals(anotherFeature.getVersionedIdentifier()));
	}
	
	
	/**
	 * @throws Exception
	 */
	public void testFeatureNewType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory(getDefaultExecutableFeatureType());
		
		String featurePath = dataPath+"FeatureTypeExamples/site1/site.xml";
		ISite site = SiteManager.getSite(new File(featurePath).toURL(),null);
		IFeatureReference ref = site.getFeatureReferences()[0];
		IFeature feature = ref.getFeature(null);
		
		IFeature anotherFeature = factory.createFeature(feature.getURL(),site,null);

		assertTrue(feature.getFeatureContentProvider() instanceof FeatureExecutableContentProvider);		
		assertTrue(((FeatureReference)ref).getType().equals("org.eclipse.update.tests.core.feature1"));		
		assertTrue("Factory doesn't create same feature",feature.getVersionedIdentifier().equals(anotherFeature.getVersionedIdentifier()));


	}
	
	
		/**
	 * @throws Exception
	 */
	public void testFeatureAnotherType() throws Exception{ 
		FeatureTypeFactory factories = FeatureTypeFactory.getInstance();
		IFeatureFactory factory = factories.getFactory(getDefaultInstallableFeatureType());
		
		String featurePath = dataPath+"FeatureTypeExamples/site2/site.xml";
		ISite site = SiteManager.getSite(new File(featurePath).toURL(),null);
		IFeatureReference ref = site.getFeatureReferences()[0];		
		IFeature feature = ref.getFeature(null);
		
		IFeature anotherFeature = factory.createFeature(feature.getURL(),site,null);
		
		assertTrue("Factory doesn't create same feature",feature.getVersionedIdentifier().equals(anotherFeature.getVersionedIdentifier()));
		assertTrue(feature.getFeatureContentProvider() instanceof FeaturePackagedContentProvider);
		assertTrue(((FeatureReference)ref).getType().equals("org.eclipse.update.core.packaged"));
		
	}
		
		
	/**
	 * @throws Exception
	 */
	public void testFeatureUnknownType() throws Exception{ 
		String featurePath = dataPath+"FeatureTypeExamples/site3/site.xml";
		ISite site = SiteManager.getSite(new File(featurePath).toURL(),null);
		IFeatureReference ref = site.getFeatureReferences()[0];		
		try {
			ref.getFeature(null);
			assertTrue("id found, should not be found",false);
		} catch (CoreException e){
			if (e.getMessage().indexOf("org.eclipse.update.core.unknowntype.jar")==-1){
				throw e;
			}
		}
		
	}		
		
}


