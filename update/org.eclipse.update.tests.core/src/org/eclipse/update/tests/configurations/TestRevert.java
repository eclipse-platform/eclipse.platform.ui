package org.eclipse.update.tests.configurations;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.ConfigurationPolicy;
import org.eclipse.update.internal.core.InternalSiteManager;
import org.eclipse.update.internal.core.SiteLocal;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;
import org.eclipse.update.tests.regularInstall.*;

public class TestRevert extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestRevert(String arg0) {
		super(arg0);
	}
	
	public void testSimpleRevertInstall() throws Exception {

		// cleanup
		File localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		InternalSiteManager.localSite=null;		

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeatureReference featureRef = remoteSite.getFeatureReferences()[0];
		IFeatureReference featureRef2 = remoteSite.getFeatureReferences()[1];
		IFeature feature = featureRef.getFeature();
		
		IInstallConfiguration old = site.getCurrentConfiguration();
		
		IInstallConfiguration newConfig = site.cloneCurrentConfiguration(null,"new Label");
		IConfigurationSite configSite = newConfig.getConfigurationSites()[0];
		site.addConfiguration(newConfig);		
		IFeatureReference installedFeature = configSite.install(feature,null);
		site.save();

		configSite.unconfigure(installedFeature,null);

		IFeature feature2 = featureRef2.getFeature();
		IInstallConfiguration newConfig2 = site.cloneCurrentConfiguration(null,"new Label2");
		IConfigurationSite anotherConfigSite = newConfig2.getConfigurationSites()[0];
		site.addConfiguration(newConfig2);		
		anotherConfigSite.install(feature2,null);
		site.save();

		site.revertTo(old,null,null);
		site.save();
		
		// check
		// there are 4 configuration
		String time = ""+site.getCurrentConfiguration().getCreationDate().getTime();
		File file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),"DefaultConfig"+time+".xml").getFile());
		assertTrue("new configuration does not exist", file.exists());
		
		
		// teh current one points to a real fature
		// does not throw error.
		IConfigurationSite configSite2 = site.getCurrentConfiguration().getConfigurationSites()[0];
		int oldNumber = old.getConfigurationSites()[0].getConfiguredFeatures().length;
		int newNumber = site.getCurrentConfiguration().getConfigurationSites()[0].getConfiguredFeatures().length;		
		assertTrue("Wrong number of configured features",oldNumber==newNumber);
		
		// test only 2 install config in local site
		assertTrue("wrong number of unconfigured features",site.getCurrentConfiguration().getConfigurationSites()[0].getConfigurationPolicy().getUnconfiguredFeatures().length==2);
		
		// cleanup
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),SiteLocal.DEFAULT_CONFIG_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);				
		UpdateManagerUtils.removeFromFileSystem(file);	
		time = ""+newConfig.getCreationDate().getTime();
		file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),"DefaultConfig"+time+".xml").getFile());	
		UpdateManagerUtils.removeFromFileSystem(file);	
		time = ""+newConfig2.getCreationDate().getTime();
		file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocation(),"DefaultConfig"+time+".xml").getFile());	
		UpdateManagerUtils.removeFromFileSystem(file);	
	}

}

