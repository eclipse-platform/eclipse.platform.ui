package org.eclipse.update.tests.sitevalidation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteValidation extends UpdateManagerTestCase {


	/**
	 * Constructor 
	 */
	public TestSiteValidation(String arg0) {
		super(arg0);
	}

	public void testSite1() throws Exception {

		URL remoteUrl = new URL(TARGET_FILE_SITE + "validation/site1");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		String msg = "The site "+file+" should be updatable.";
		if (!status.isOK()){
			fail(msg+status.getMessage());
		}
	}
	
	public void testSite2() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site2/children");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		UpdateManagerUtils.removeFromFileSystem(new File(file,".eclipseUM"));

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
		if (status.getMessage().indexOf("This site is contained in another site:")==-1){
			fail("Wrong validation:"+status.getMessage());
		}
	}	

	public void testSite3() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site3");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		UpdateManagerUtils.removeFromFileSystem(new File(file,".eclipseUM"));

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
		if (status.getMessage().indexOf("This site is contained in another site:")==-1){
			fail("Wrong validation:"+status.getMessage());
		}
	}	

	public void testSite4() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site4");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
		if (status.getMessage().indexOf("The site cannot be modifed by this product. It is already associated with product:")==-1){
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
	}
	
	public void testSite6() throws Exception {

		URL remoteUrl = new URL(SOURCE_FILE_SITE + "validation/site6/children/children");
		File file = new File(remoteUrl.getFile());
		ILocalSite local = SiteManager.getLocalSite();
		IInstallConfiguration currentConfig = local.getCurrentConfiguration();
		IConfiguredSite configuredSite = currentConfig.createConfiguredSite(file);
		IStatus status = configuredSite.verifyUpdatableStatus();

		UpdateManagerUtils.removeFromFileSystem(new File(file,".eclipseUM"));

		String msg = "The site "+file+" should not be updatable.";
		if (status.isOK()){
			fail(msg+status.getMessage());
		}
		if (status.getMessage().indexOf("This site is contained in another site:")==-1){
			fail("Wrong validation:"+status.getMessage());
		}
	}	
}