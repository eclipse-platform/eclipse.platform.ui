package org.eclipse.update.tests.regularInstall;

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

public class TestGetFeature extends UpdateManagerTestCase {
	/**
	 * Test the getFeatures()
	 */
	public TestGetFeature(String arg0) {
		super(arg0);
	}
	
	
	public void testFileSite() throws Exception{
		
		ISite remoteSite = new URLSite(new URL(SOURCE_FILE_SITE));
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier());
		}
	}

	public void testHTTPSite() throws Exception{ 
		
		ISite remoteSite = new URLSite(new URL(SOURCE_HTTP_SITE));
		IFeature[] remoteFeatures = remoteSite.getFeatures();
		if (remoteFeatures==null || remoteFeatures.length==0) fail("No feature available for testing");		
		for (int i=0;i<remoteFeatures.length;i++){
			System.out.println("feature:"+remoteFeatures[i].getIdentifier());
		}
	}
}

