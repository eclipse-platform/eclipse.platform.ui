package org.eclipse.update.tests.uivalues;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestUILabel extends UpdateManagerTestCase {

	/**
	 * Test the getFeatures()
	 */
	public TestUILabel(String arg0) {
		super(arg0);
	}
	
	
	/**
	 * Method testHTTPSite.
	 * @throws Exception
	 */
	public void testDejanSite() throws Exception{ 
		
		ISite remoteSite = SiteManager.getSite(new URL("http",bundle.getString("HTTP_HOST_1"),bundle.getString("HTTP_PATH_2")));
		ICategory[] categories = remoteSite.getCategories();
		for (int i =0; i<categories.length; i++){
			System.out.println("Category ->"+categories[i].getLabel()+":"+categories[i].getName());
		}
		System.out.println(remoteSite.getInfoURL().toExternalForm());
		IFeatureReference[] remoteFeatures = remoteSite.getFeatureReferences();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			IFeature feature = remoteFeatures[i].getFeature();
			System.out.println("feature:"+feature.getVersionIdentifier()+"->"+feature.getLabel());
			print(feature.getLicense(),"License");
			print(feature.getCopyright(),"Copyright");			
			print(feature.getDescription(),"Description");				
			
			// check that it downloads the feature.jar under the cover
			// and unpack it
			
			URL url = feature.getLicense().getURL();
			if (url!=null){
				assertTrue((new File(url.getFile())).exists());
			}

			url = feature.getCopyright().getURL();
			if (url!=null){
				assertTrue((new File(url.getFile())).exists());
			}
			
			url = feature.getDescription().getURL();
			if (url!=null){
				assertTrue((new File(url.getFile())).exists());
			}
			
		}
	}
	
	
	/**
	 * Method print.
	 * @param info
	 * @param text
	 */
	private void print(IURLEntry info, String text){
		System.out.print("->"+text+":");
		if (info.getURL()!=null) 
			System.out.println("<"+info.getURL().toExternalForm()+">");
		else 
			System.out.println(info.getAnnotation());
	}
} 

