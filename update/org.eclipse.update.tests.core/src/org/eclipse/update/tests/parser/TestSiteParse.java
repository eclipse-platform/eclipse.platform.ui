package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.URLSite;
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
			URL remoteUrl = new URL(SOURCE_FILE_SITE+"xmls/site1/");
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

