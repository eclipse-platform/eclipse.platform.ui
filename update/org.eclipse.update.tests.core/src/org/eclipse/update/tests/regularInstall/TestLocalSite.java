package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.update.core.*;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.internal.core.Category;
import org.eclipse.update.internal.core.FeatureReference;
import org.eclipse.update.internal.core.SiteLocal;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestLocalSite extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestLocalSite(String arg0) {
		super(arg0);
	}
	
	public void testCreationConfigFile() throws Exception {

		ILocalSite site = SiteManager.getLocalSite();
		assertTrue("the local site already contains a config state, test cannot be executed",site.getCurrentConfiguration().getLabel().equals(SiteLocal.DEFAULT_LABEL));
		URL location = ((SiteLocal)site).getLocation();
		String filePath = location.getFile();
		File file = new File(filePath);
		assertTrue("config file hasn't been saved in :"+filePath, file.exists());
		
		// cleanup
		//UpdateManagerUtils.removeFromFileSystem(file);

	}
	
	
}

