package org.eclipse.update.tests.regularInstall;

import java.io.File;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;

public class Test2 extends TestCase {

	private static String SOURCE_FILE_SITE;
	private static String SOURCE_HTTP_SITE;	

	/**
	 * Test the getFeatures()
	 */
	public Test2(String arg0) {
		super(arg0);
	}
	
	
	protected void setUp(){
		
		String home = System.getProperty("user.home");
				
		// get bundle variables
		ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.update.tests.regularInstall.Resources");
		SOURCE_FILE_SITE = "file:///"+home+(String)bundle.getObject("SOURCE_FILE_SITE");
		SOURCE_HTTP_SITE = "http://"+(String)bundle.getObject("SOURCE_HTTP_SITE");
	}
	
	public void testExecute1() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_FILE_SITE));
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) throw new Exception("No feature available for testing");
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier());
		}
	}

	public void testExecute2() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_HTTP_SITE));
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) throw new Exception("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier());
		}
	}
	public static void main(String[] args){
		junit.textui.TestRunner.run(Test2.class);
	}	

}

