package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.ConfigurationPolicy;
import org.eclipse.update.internal.core.SiteLocal;
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

		//clean up
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		


		ILocalSite site = SiteManager.getLocalSite();
		assertTrue("the local site already contains a config state, test cannot be executed",site.getCurrentConfiguration().getLabel().equals(SiteLocal.DEFAULT_CONFIG_LABEL));
		site.save();
		URL location = ((SiteLocal)site).getLocation();
		String filePath = new URL(location,SiteLocal.SITE_LOCAL_FILE).getFile();
		File file = new File(filePath);
		assertTrue("config file hasn't been saved in :"+filePath, file.exists());
		
		// cleanup
		UpdateManagerUtils.removeFromFileSystem(file);
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		

	}
	
	public void testInstallFeatureSaveConfig() throws Exception {

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature feature = remoteSite.getFeatureReferences()[0].getFeature();
		// we are not checking if this is read only
		IInstallConfiguration newConfig = site.createConfiguration(null,"new Label");
		IConfigurationSite configSite = newConfig.getConfigurationSites()[0];
		configSite.setConfigurationPolicy(new ConfigurationPolicy(IPlatformConfiguration.ISitePolicy.USER_INCLUDE));
		configSite.install(feature,null);
		site.addConfiguration(newConfig);
		site.save();
		
		// check
		//URL location = ((SiteLocal)site).getLocation();
		//String filePath = location.getFile();
		//File file = new File(filePath);
		//assertTrue("config file hasn't been saved in :"+filePath, file.exists());
		
		// cleanup
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		

	}
	
}

