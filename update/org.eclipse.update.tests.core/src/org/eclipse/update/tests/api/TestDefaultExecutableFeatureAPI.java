package org.eclipse.update.tests.api;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.update.core.AbstractFeature;
import org.eclipse.update.core.IInfo;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.core.DefaultExecutableFeature;
import org.eclipse.update.internal.core.FileSite;
import org.eclipse.update.internal.core.Info;
import org.eclipse.update.tests.UpdateManagerTestCase;



public class TestDefaultExecutableFeatureAPI extends UpdateManagerTestCase {
	
	private	AbstractFeature remoteFeature=null;
	
	/**
	 * Test the testFeatures()
	 */
	public TestDefaultExecutableFeatureAPI(String arg0) {
		super(arg0);
	}
	
	/**
	 * the feature to test
	 */
	private AbstractFeature getFeature(){
		if (remoteFeature == null){
		try {
		ISite site = new FileSite(new URL(SOURCE_FILE_SITE));
		VersionedIdentifier id = new VersionedIdentifier("org.eclipse.update.core.feature1","1.0.0");
		remoteFeature = new DefaultExecutableFeature(id,site);
		} catch (java.net.MalformedURLException e){
			fail("Wrong source site URL");
		}
		}
		return remoteFeature;
	}
	
	/**
	 * @see IFeature#testIdentifier()
	 */
	public void testIdentifier() {
		String ident1 = "org.eclipse.test.feature_1.0.0";
		String ident2 = "org.eclipse_test_feature";
		String ver2   = "2.0.2";
		
		try {
			ISite site = new FileSite(new URL(SOURCE_FILE_SITE));
			
			VersionedIdentifier id = new VersionedIdentifier(ident1);
			remoteFeature = new DefaultExecutableFeature(id,site);
			assertEquals(id,remoteFeature.getIdentifier());
		
			VersionedIdentifier id2 = new VersionedIdentifier(ident2,ver2);
			remoteFeature = new DefaultExecutableFeature(id2,site);
			assertEquals(id2,remoteFeature.getIdentifier());
		
		} catch (java.net.MalformedURLException e){
			fail("Wrong source site URL");
		}
	}


	/**
	 * @see IFeature#testSite()
	 */
	public void testSite() {
		String ident1 = "org.eclipse.test.feature_1.0.0";		
		try {
			ISite site = new FileSite(new URL(SOURCE_FILE_SITE));
			
			VersionedIdentifier id = new VersionedIdentifier(ident1);
			remoteFeature = new DefaultExecutableFeature(id,site);
			assertEquals(site,remoteFeature.getSite());
		} catch (java.net.MalformedURLException e){
			fail("Wrong source site URL");
		}
	}


	/**
	 * @see IFeature#testLabel()
	 */
	public void testLabel() {
		String label = "dummy label";
		AbstractFeature feature = getFeature();
		feature.setLabel(label);
		assertEquals(label,feature.getLabel());
	}


	/**
	 * @see IFeature#testUpdateURL()
	 */
	public void testUpdateURL() {
		URL url = null;
		String label = "OTI Site";
		try {
			url = new URL("http://www.oti.com/");
			
		} catch (java.net.MalformedURLException e){} // impossible eh !
		AbstractFeature feature = getFeature();
		feature.setUpdateInfo(new Info(label, url));
		assertEquals(url,feature.getUpdateInfo().getURL());
		assertEquals(label,feature.getUpdateInfo().getText());

	}


	
	/**
	 * @see IFeature#testDiscoveryURLs()
	 */
	public void testDiscoveryURLs() {
		IInfo[] urlInfo = new Info[2];
		URL[] url = new URL[2];
		String[] label = new String[2];
		try {
			url[0] = new URL("http://www.oti.com/");
			url[1] = new URL("http://eclipse.org/");
			label[0] = "OTI Site";
			label[1] = "Eclipse Site";
		} catch (java.net.MalformedURLException e){} // impossible eh !
		AbstractFeature feature = getFeature();
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
	public void testProvider() {
		String provider = "not so dummy provider";
		AbstractFeature feature = getFeature();
		feature.setProvider(provider);
		assertEquals(provider,feature.getProvider());
	}


	/**
	 * @see IFeature#testDescription()
	 */
	public void testDescription() {
		String desc = "pretty long description as a string with \r\n and \t and \n";
		IInfo info = new Info(desc);
		AbstractFeature feature = getFeature();
		feature.setDescription(info);
		assertEquals(desc,feature.getDescription().getText());
	}
	
	/**
	 * @see IFeature#testDescription()
	 */
	public void testDescription1() {
		URL desc = null;
		try {
			desc = new URL("http://www.oti.com");
		} catch (MalformedURLException e){/*pretty impossible*/}
		IInfo info = new Info(desc);
		AbstractFeature feature = getFeature();
		feature.setDescription(info);
		assertEquals(desc,feature.getDescription().getURL());
	}	
	
	/**
	 * @see IFeature#testDescription()
	 */
	public void testDescription2() {
		URL url = null;
		try {
			url = new URL("http://www.oti.com");
		} catch (MalformedURLException e){/*pretty impossible*/}
		String desc = "pretty long description as a string with \r\n and \t and \n";
		IInfo info = new Info(desc,url);
		AbstractFeature feature = getFeature();
		feature.setDescription(info);
		assertEquals(desc,feature.getDescription().getText());
		assertEquals(url,feature.getDescription().getURL());
	}	



}

