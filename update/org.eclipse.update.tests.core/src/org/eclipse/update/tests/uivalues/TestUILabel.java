package org.eclipse.update.tests.uivalues;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.URLSite;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestUILabel extends UpdateManagerTestCase {

	/**
	 * Test the getFeatures()
	 */
	public TestUILabel(String arg0) {
		super(arg0);
	}
	
	
	public void testHTTPSite() throws Exception{ 
		
		ISite remoteSite = new URLSite(new URL("http",bundle.getString("HTTP_HOST_1"),bundle.getString("HTTP_PATH_2")));
		ICategory[] categories = remoteSite.getCategories();
		for (int i =0; i<categories.length; i++){
			System.out.println("Category ->"+categories[i].getLabel()+":"+categories[i].getName());
		}
		System.out.println(remoteSite.getInfoURL().toExternalForm());
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier()+"->"+remoteFeatures[i].getLabel());
			print(remoteFeatures[i].getLicense(),"License");
			print(remoteFeatures[i].getCopyright(),"Copyright");
			print(remoteFeatures[i].getDescription(),"Description");						
		}
	}
	
	
	private void print(IInfo info, String text){
		System.out.print("->"+text+":");
		if (info.getURL()!=null) 
			System.out.println("<"+info.getURL().toExternalForm()+">");
		else 
			System.out.println(info.getText());
	}
}

