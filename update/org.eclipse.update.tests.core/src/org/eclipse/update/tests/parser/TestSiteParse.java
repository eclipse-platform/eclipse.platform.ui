package org.eclipse.update.tests.parser;

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

public class TestSiteParse extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestSiteParse(String arg0) {
		super(arg0);
	}
	
	
	public void testParse(){
	
		try {		
			URL remoteUrl = new URL(SOURCE_FILE_SITE+"/xmls/site1/");
			ISite remoteSite = new URLSite(remoteUrl);
			
			IFeature[] feature = remoteSite.getFeatures();
			ICategory[] categories = remoteSite.getCategories();
			
			assertEquals(remoteUrl.getPath()+"info/siteInfo.html",remoteSite.getInfoURL().getPath());
			
		} catch (Exception e){
			fail(e.toString());
			e.printStackTrace();
		}
	}
}

