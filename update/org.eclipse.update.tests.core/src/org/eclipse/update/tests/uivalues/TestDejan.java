package org.eclipse.update.tests.uivalues;

import java.io.File;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestDejan extends UpdateManagerTestCase {


	public String SOURCE = "http://9.26.150.182/Dejan/";
		/**
	 * Test the getFeatures()
	 */
	public TestDejan(String arg0) {
		super(arg0);
	}
	
	
	public void testHTTPSite() throws Exception{ 
		
		ISite remoteSite = new URLSite(new URL(SOURCE));
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

