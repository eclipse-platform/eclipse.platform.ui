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

public class TestFeatureParse extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestFeatureParse(String arg0) {
		super(arg0);
	}
	
	
	public void testParse(){
	
		String xmlFile = "xmls/";
		try {		
			ISite remoteSite = new URLSite(new URL(SOURCE_FILE_SITE));
			VersionedIdentifier id = new VersionedIdentifier("feature","1.0.0");
			DefaultExecutableFeature feature = new DefaultExecutableFeature(id,remoteSite);
			feature.setURL(new URL(remoteSite.getURL(),xmlFile));
			
			String prov = feature.getProvider();
			assertEquals("Object Technology International",prov);
			
		} catch (Exception e){
			fail(e.toString());
			e.printStackTrace();
		}
	}
}

