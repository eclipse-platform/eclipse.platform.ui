package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
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
		InternalSiteManager.localSite=null;	


		ILocalSite site = SiteManager.getLocalSite();
		assertTrue("the local site already contains a config state, test cannot be executed",site.getCurrentConfiguration().getLabel().equals(SiteLocal.DEFAULT_CONFIG_LABEL));
		site.save();
		URL location = ((SiteLocal)site).getLocation();
		String filePath = new URL(location,SiteLocal.DEFAULT_CONFIG_FILE).getFile();
		File file = new File(filePath);
		assertTrue("config file hasn't been saved in :"+filePath, file.exists());
		assertTrue("Local site hasn't been saved in :"+localFile.getAbsolutePath(),localFile.exists());
		
		// cleanup
		UpdateManagerUtils.removeFromFileSystem(file);
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		

	}
	
	public void testDefaultConfigFile() throws Exception {

		//clean up
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		InternalSiteManager.localSite=null;

		ILocalSite site = SiteManager.getLocalSite();
		assertTrue("the local site already contains a config state, test cannot be executed",site.getCurrentConfiguration().getLabel().equals(SiteLocal.DEFAULT_CONFIG_LABEL));
		assertTrue("The local site does not contain an history of install configuration",site.getConfigurationHistory().length!=0);
		assertTrue("The local site does not contain an current install configuration",site.getCurrentConfiguration()!=null);
		assertTrue("The local site does not contain a default configuration site for the current install config",site.getCurrentConfiguration().getConfigurationSites().length!=0);
		
		System.out.println("Default Config Site is :"+site.getCurrentConfiguration().getConfigurationSites()[0].getSite().getURL().toExternalForm());
		
		// cleanup
		URL location = ((SiteLocal)site).getLocation();		
		String filePath = new URL(location,SiteLocal.DEFAULT_CONFIG_FILE).getFile();
		File file = new File(filePath);
		UpdateManagerUtils.removeFromFileSystem(file);		
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		

	}
	
	public void testInstallFeatureSaveConfig() throws Exception {

		// cleanup
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		InternalSiteManager.localSite=null;		

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature feature = remoteSite.getFeatureReferences()[0].getFeature();
		
		// we are not checking if this is read only
		IInstallConfiguration newConfig = site.cloneCurrentConfiguration(null,"new Label");
		//IInstallConfiguration newConfig = site.getCurrentConfiguration();
		IConfigurationSite configSite = newConfig.getConfigurationSites()[0];
		configSite.setConfigurationPolicy(SiteManager.createConfigurationPolicy(IPlatformConfiguration.ISitePolicy.USER_INCLUDE));
		site.addConfiguration(newConfig);		
		configSite.install(feature,null);

				
		// teh current one points to a real fature
		// does not throw error.
		IConfigurationSite configSite2 = site.getCurrentConfiguration().getConfigurationSites()[0];
		IFeatureReference ref = configSite2.getConfiguredFeatures()[0];
		IFeature feature2 = ref.getFeature();
		String configuredFeature = feature2.getLabel();

		assertEquals(feature2.getVersionIdentifier().toString(),"org.eclipse.update.core.tests.feature3_1.0.0");
		assertTrue("Wrong id  version of feature",feature2.getVersionIdentifier().toString().equalsIgnoreCase("org.eclipse.update.core.tests.feature3_1.0.0"));
		
		// only one feature configured
		assertTrue("too many features configured",configSite2.getConfiguredFeatures().length==1);
		
		// no feature unconfigured
		assertTrue("too many unconfigured features",configSite2.getConfigurationPolicy().getUnconfiguredFeatures().length==0);
		
		// test only 2 install config in local site
		assertTrue("wrong number of history in Local site",site.getConfigurationHistory().length==2);
		
		// test only 1 site in current config
		assertTrue("Wrong number of config sites in current config",site.getCurrentConfiguration().getConfigurationSites().length==1);
		
		//test only one feature for the site
		assertTrue("wrong number of configured features for config site",site.getCurrentConfiguration().getConfigurationSites()[0].getConfiguredFeatures().length==1);
		
		// test only 2 activities
		assertTrue("Wrong number of activities for install config",site.getCurrentConfiguration().getActivities().length==2);

		site.save();		

		// check
		// there are 2 configuration
		String time = ""+site.getCurrentConfiguration().getCreationDate().getTime();
		File file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),"DefaultConfig"+time+".xml").getFile());
		assertTrue("new configuration does not exist", file.exists());
		
		// cleanup
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.DEFAULT_CONFIG_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);	
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),Site.DEFAULT_FEATURE_PATH+File.separator+feature.getVersionIdentifier().toString()).getFile());		
		UpdateManagerUtils.removeFromFileSystem(localFile);	
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),Site.DEFAULT_FEATURE_PATH+File.separator+feature2.getVersionIdentifier().toString()).getFile());		
		UpdateManagerUtils.removeFromFileSystem(localFile);	
		UpdateManagerUtils.removeFromFileSystem(file);		
		localFile = new File(feature2.getURL().getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);

	}
	
	public void testRetriveConfig() throws Exception {

		// cleanup
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		InternalSiteManager.localSite=null;		

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		IFeature feature = remoteSite.getFeatureReferences()[0].getFeature();
		
		// we are not checking if this is read only
		IInstallConfiguration newConfig = site.cloneCurrentConfiguration(null,"new Label");
		IConfigurationSite configSite = newConfig.getConfigurationSites()[0];
		configSite.setConfigurationPolicy(SiteManager.createConfigurationPolicy(IPlatformConfiguration.ISitePolicy.USER_INCLUDE));
		site.addConfiguration(newConfig);		
		configSite.install(feature,null);
		site.save();
		
		// we created the second xml file

		//do not cleanup, we want to reuse previously created local site
		// but force re-read of xml File
		InternalSiteManager.localSite=null;
		site = SiteManager.getLocalSite();
		
		// check
		// there are 2 configuration
		String time = ""+site.getCurrentConfiguration().getCreationDate().getTime();
		File file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),"DefaultConfig"+time+".xml").getFile());
		assertTrue("new configuration does not exist", file.exists());
		
		// teh current one points to a real fature
		// does not throw error.
		IConfigurationSite configSite2 = site.getCurrentConfiguration().getConfigurationSites()[0];
		IFeatureReference ref = configSite2.getConfiguredFeatures()[0];
		IFeature feature2 = ref.getFeature();
		String configuredFeature = feature2.getLabel();
		assertEquals(feature2.getVersionIdentifier().toString(),"org.eclipse.update.core.tests.feature3_1.0.0");
		assertTrue("Wrong id  version of feature",feature2.getVersionIdentifier().toString().equalsIgnoreCase("org.eclipse.update.core.tests.feature3_1.0.0"));
		
		// test only 2 install config in local site
		assertTrue("wrong number of history in Local site",site.getConfigurationHistory().length==2);
		
		// test only 1 site in current config
		assertTrue("Wrong number of config sites in current config",site.getCurrentConfiguration().getConfigurationSites().length==1);
		
		//test only one feature for the site
		assertTrue("wrong number of configured features for config site",site.getCurrentConfiguration().getConfigurationSites()[0].getConfiguredFeatures().length==1);
		
		// test only 2 activities
		assertTrue("Wrong number of activities for install config",site.getCurrentConfiguration().getActivities().length==2);
		
		
		// cleanup
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.DEFAULT_CONFIG_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);			
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),Site.DEFAULT_FEATURE_PATH+File.separator+feature.getVersionIdentifier().toString()).getFile());		
		UpdateManagerUtils.removeFromFileSystem(localFile);	
			
		UpdateManagerUtils.removeFromFileSystem(file);		
	}

	public void testRetriveConfigHTTPInstall() throws Exception {

		// cleanup
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		InternalSiteManager.localSite=null;		

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeature feature = remoteSite.getFeatureReferences()[0].getFeature();
		
		// we are not checking if this is read only
		IInstallConfiguration newConfig = site.cloneCurrentConfiguration(null,"new Label");
		IConfigurationSite configSite = newConfig.getConfigurationSites()[0];
		configSite.setConfigurationPolicy(SiteManager.createConfigurationPolicy(IPlatformConfiguration.ISitePolicy.USER_INCLUDE));
		site.addConfiguration(newConfig);		
		configSite.install(feature,null);
		site.save();

		//do not cleanup, we want to reuse previously created local site
		// but force re-read of xml File
		InternalSiteManager.localSite=null;
		site = SiteManager.getLocalSite();
		feature = remoteSite.getFeatureReferences()[0].getFeature();
		
		// check
		// there are 2 configuration
		String time = ""+site.getCurrentConfiguration().getCreationDate().getTime();
		File file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),"DefaultConfig"+time+".xml").getFile());
		assertTrue("new configuration does not exist", file.exists());
		
		// teh current one points to a real fature
		// does not throw error.
		IConfigurationSite configSite2 = site.getCurrentConfiguration().getConfigurationSites()[0];
		IFeatureReference ref = configSite2.getConfiguredFeatures()[0];
		IFeature feature2 = ref.getFeature();
		String configuredFeature = feature2.getLabel();
		assertEquals(feature2.getVersionIdentifier().toString(),"org.test1.ident1_1.0.0");
		assertTrue("Wrong id  version of feature",feature2.getVersionIdentifier().toString().equalsIgnoreCase("org.test1.ident1_1.0.0"));
		
		// test only 2 install config in local site
		assertTrue("wrong number of history in Local site",site.getConfigurationHistory().length==2);
		
		// test only 1 site in current config
		assertTrue("Wrong number of config sites in current config",site.getCurrentConfiguration().getConfigurationSites().length==1);
		
		//test only one feature for the site
		assertTrue("wrong number of configured features for config site",site.getCurrentConfiguration().getConfigurationSites()[0].getConfiguredFeatures().length==1);
		
		// test only 2 activities
		assertTrue("Wrong number of activities for install config",site.getCurrentConfiguration().getActivities().length==2);
		
		
		// cleanup
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.DEFAULT_CONFIG_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);	
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),Site.DEFAULT_FEATURE_PATH+File.separator+feature.getVersionIdentifier().toString()).getFile());		
		UpdateManagerUtils.removeFromFileSystem(localFile);	
					
		UpdateManagerUtils.removeFromFileSystem(file);		
		localFile = new File(feature2.getURL().getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);
	}

}

