package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.ISite;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.core.DefaultExecutableFeature;
import org.eclipse.update.internal.core.URLSite;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestFeatureParse extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestFeatureParse(String arg0) {
		super(arg0);
	}
	
	
	public void testParse(){
	
		String xmlFile = "xmls/feature_1.0.0/";
		try {		
			ISite remoteSite = new URLSite(SOURCE_FILE_SITE);
			URL id = UpdateManagerUtils.getURL(remoteSite.getURL(),xmlFile,null);
			
			DefaultExecutableFeature feature = new DefaultExecutableFeature(id,remoteSite);
			feature.initializeFeature();
			
			String prov = feature.getProvider();
			assertEquals("Object Technology International",prov);
			
		} catch (Exception e){
			fail(e.toString());
			e.printStackTrace();
		}
	}
}

