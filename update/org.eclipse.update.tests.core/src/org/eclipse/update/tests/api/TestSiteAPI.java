/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.api;
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestSiteAPI(String arg0) {
		super(arg0);
	}
	
	public void testURL() throws Exception {

		ISite site = SiteManager.getSite(SOURCE_FILE_SITE, null);
		assertEquals(new File(site.getURL().getFile()),new File(SOURCE_FILE_SITE.getFile()));
		
		ISite site2 = SiteManager.getSite(SOURCE_HTTP_SITE, null);
		assertEquals(site2.getURL(),new URL("http", getHttpHost(),getHttpPort(), bundle.getString("HTTP_PATH_1")+"site.xml"));

	}

}

