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
package org.eclipse.update.tests.sitevalidation;
import java.io.*;
import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configurator.*;
import org.eclipse.update.configurator.IPlatformConfiguration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.tests.*;

public class TestSiteValidation extends UpdateManagerTestCase {


	/**
	 * Constructor 
	 */
	public TestSiteValidation(String arg0) {
		super(arg0);
	}

	private void removeConfigSite(URL url) throws Exception {
		// get new config object
		IPlatformConfiguration cfig = ConfiguratorUtils.getCurrentPlatformConfiguration();
		ISiteEntry s1 = cfig.findConfiguredSite(url);
		assertNotNull("Unable to find site entry:"+url,s1);
		cfig.unconfigureSite(s1);
	}

	public void testSite1() throws Exception {

		URL remoteUrl = new URL(TARGET_FILE_SITE + "validation/site1/eclipse");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		file.mkdirs();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		String msg = "The site "+file+" should be updatable.";
		if (!status.isOK()){
			fail(msg+status.getMessage());
		}
		currentConfig.removeConfiguredSite(configuredSite);
		removeConfigSite(configuredSite.getSite().getURL());
	}
	
	public void testSite2() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site2/eclipse/");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite;
		try {
		 configuredSite = currentConfig.createConfiguredSite(file);
		} catch (CoreException e){
			return;
		}
		IStatus status = configuredSite.verifyUpdatableStatus();

//		UpdateManagerUtils.removeFromFileSystem(new File(file,".eclipseUM"));

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
		if (status.getMessage().indexOf("This site is contained in another site:")==-1){
			fail("Wrong validation:"+status.getMessage());
		}
	}	

	public void testSite3() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site3/eclipse/");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

//		UpdateManagerUtils.removeFromFileSystem(new File(file,".eclipseUM"));

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
//		if (status.getMessage().indexOf("This site is contained in another site:")==-1){
//			fail("Wrong validation:"+status.getMessage());
//		}
	}	

	public void testSite4() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site4/eclipse/");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
		if (status.getMessage().indexOf("The site cannot be modified by this product. It is already associated with product:")==-1){
			fail("Wrong validation:"+status.getMessage());
		}
	}	

	public void testSite5() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site5/");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		String msg = "The site "+file+" should be updatable.";
		if (!status.isOK()){
			fail(msg+status.getMessage());
		}
		
			// get new config object
		URL url = configuredSite.getSite().getURL();
		IPlatformConfiguration cfig = ConfiguratorUtils.getCurrentPlatformConfiguration();
		ISiteEntry s1 = cfig.findConfiguredSite(url);
		assertNotNull("Site entry not found:"+url,s1);
		cfig.unconfigureSite(s1);
		cfig.save();
	}
	
	public void testSite6() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site6/children/children/eclipse/");
		File file = new File(remoteUrl.getFile());
		if (!file.exists()) file.mkdirs();
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

//		UpdateManagerUtils.removeFromFileSystem(new File(file,".eclipseUM"));

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
//		if (status.getMessage().indexOf("This site is contained in another site:")==-1){
//			fail("Wrong validation:"+status.getMessage());
//		}
	}	
}
