package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.URLSite;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestCategories extends UpdateManagerTestCase {
	/**
	 * Constructor for Test1
	 */
	public TestCategories(String arg0) {
		super(arg0);
	}
	
	
	public void testCategories() throws Exception { 
	

			URL remoteUrl = new URL(SOURCE_FILE_SITE+"xmls/site1/");
			ISite remoteSite = SiteManager.getSite(remoteUrl);
			
			IFeature[] feature = remoteSite.getFeatures();
			ICategory[] categories = remoteSite.getCategories();
			
			ICategory featureCategory = feature[0].getCategories()[0];
			
			assertEquals("UML tools",featureCategory.getLabel());
			
	}
}

