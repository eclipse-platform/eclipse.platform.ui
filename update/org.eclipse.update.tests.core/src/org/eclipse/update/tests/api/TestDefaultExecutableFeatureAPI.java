package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;



public class TestDefaultExecutableFeatureAPI extends UpdateManagerTestCase {
	
	private Feature remoteFeature=null;
	
	/**
	 * Test the testFeatures()
	 */
	public TestDefaultExecutableFeatureAPI(String arg0) {
		super(arg0);
	}
	
	/**
	 * the feature to test
	 */
	private Feature getFeature() throws MalformedURLException, CoreException {
		if (remoteFeature == null){

		ISite site = SiteManager.getSite(SOURCE_FILE_SITE);
		URL id = UpdateManagerUtils.getURL(site.getURL(),"org.eclipse.update.core.feature1_1.0.0.jar",null);
		remoteFeature = new FeatureExecutable(id,site);

		}
		return remoteFeature;
	}
	
	/**
	 * @see IFeature#testIdentifier()
	 */
	public void testIdentifier() throws CoreException, MalformedURLException {
		String id1 = "xmls/apitests/org.eclipse.test.feature_1.0.0/";
		String id2 = "xmls/apitests/org.eclipse_test_feature";
		VersionedIdentifier ident1 = new VersionedIdentifier("org.test1.ident1","1.0.0");
		VersionedIdentifier ident2 = new VersionedIdentifier("org.test1.ident2","1.0.0");		
		

			ISite site = SiteManager.getSite(SOURCE_FILE_SITE);
			
			URL url1 = UpdateManagerUtils.getURL(site.getURL(),id1,null);			
			remoteFeature = new FeatureExecutable(url1,site);
			remoteFeature.setIdentifier(ident1);
			assertEquals(ident1.toString(),remoteFeature.getIdentifier().toString());
		
			 URL url2 = UpdateManagerUtils.getURL(site.getURL(),id2,null);		
			remoteFeature = new FeatureExecutable(url2,site);
			remoteFeature.setIdentifier(ident2);
			assertEquals(ident2.toString(),remoteFeature.getIdentifier().toString());
		

	}


	/**
	 * @see IFeature#testSite()
	 */
	public void testSite() throws MalformedURLException, CoreException{
		String ident1 = "org.eclipse.test.feature_1.0.0.jar";		

			ISite site = SiteManager.getSite(SOURCE_FILE_SITE);
			
			URL id = UpdateManagerUtils.getURL(site.getURL(),ident1,null);			
			remoteFeature = new FeatureExecutable(id,site);
			assertEquals(site,remoteFeature.getSite());

	}


	/**
	 * @see IFeature#testLabel()
	 */
	public void testLabel() throws CoreException,MalformedURLException {
		String label = "dummy label";
		Feature feature = getFeature();
		feature.setLabel(label);
		assertEquals(label,feature.getLabel());
	}


	/**
	 * @see IFeature#testUpdateURL()
	 */
	public void testUpdateURL() throws CoreException,MalformedURLException {
		URL url = null;
		String label = "OTI Site";
		try {
			url = new URL("http://www.oti.com/");
			
		} catch (java.net.MalformedURLException e){} // impossible eh !
		Feature feature = getFeature();
		feature.setUpdateInfo(new Info(label, url));
		assertEquals(url,feature.getUpdateInfo().getURL());
		assertEquals(label,feature.getUpdateInfo().getText());

	}


	
	/**
	 * @see IFeature#testDiscoveryURLs()
	 */
	public void testDiscoveryURLs() throws CoreException,MalformedURLException {
		IInfo[] urlInfo = new Info[2];
		URL[] url = new URL[2];
		String[] label = new String[2];
		try {
			url[0] = new URL("http://www.oti.com/");
			url[1] = new URL("http://eclipse.org/");
			label[0] = "OTI Site";
			label[1] = "Eclipse Site";
		} catch (java.net.MalformedURLException e){} // impossible eh !
		Feature feature = getFeature();
		for (int i=0;i<2;i++){
				urlInfo[i] = new Info(label[i],url[i]);
		}
		feature.setDiscoveryInfos(urlInfo);
		for (int i=0; i<feature.getDiscoveryInfos().length;i++){
			assertEquals(urlInfo[i],feature.getDiscoveryInfos()[i]);
		}

	}


	/**
	 * @see IFeature#testProvider()
	 */
	public void testProvider() throws CoreException,MalformedURLException {
		String provider = "not so dummy provider";
		IFeature feature = getFeature();
		((Feature)feature).setProvider(provider);
		assertEquals(provider,feature.getProvider());
	}


	/**
	 * @see IFeature#testDescription()
	 */
	public void testDescription() throws CoreException,MalformedURLException {
		String desc = "pretty long description as a string with \r\n and \t and \n";
		IInfo info = new Info(desc);
		IFeature feature = getFeature();
		((Feature)feature).setDescription(info);
		assertEquals(desc,feature.getDescription().getText());
	}
	
	/**
	 * @see IFeature#testDescription()
	 */
	public void testDescription1() throws CoreException, MalformedURLException {
		URL desc = null;
		try {
			desc = new URL("http://www.oti.com");
		} catch (MalformedURLException e){/*pretty impossible*/}
		IInfo info = new Info(desc);
		IFeature feature = getFeature();
		((Feature)feature).setDescription(info);
		assertEquals(desc,feature.getDescription().getURL());
	}	
	
	/**
	 * @see IFeature#testDescription()
	 */
	public void testDescription2() throws CoreException, MalformedURLException {
		URL url = null;
		try {
			url = new URL("http://www.oti.com");
		} catch (MalformedURLException e){/*pretty impossible*/}
		String desc = "pretty long description as a string with \r\n and \t and \n";
		IInfo info = new Info(desc,url);
		IFeature feature = getFeature();
		((Feature)feature).setDescription(info);
		assertEquals(desc,feature.getDescription().getText());
		assertEquals(url,feature.getDescription().getURL());
	}	



}

