package org.eclipse.update.tests.configurations;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.ConfigurationPolicy;
import org.eclipse.update.internal.core.InternalSiteManager;
import org.eclipse.update.internal.core.SiteLocal;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.internal.model.*;
import org.eclipse.update.internal.model.ConfiguredSiteModel;
import org.eclipse.update.internal.model.InstallConfigurationModel;
import org.eclipse.update.tests.UpdateManagerTestCase;
import org.eclipse.update.tests.regularInstall.*;

public class TestBackward extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestBackward(String arg0) {
		super(arg0);
	}
	
	public void testSimpleBackward() throws Exception {

		// cleanup
		SiteLocal siteLocal = ((SiteLocal)SiteManager.getLocalSite());
		File localFile = new File(new URL(siteLocal.getLocationURL(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		UpdateManagerUtils.removeFromFileSystem(new File(((InstallConfiguration)siteLocal.getCurrentConfiguration()).getURL().getFile()));
		InternalSiteManager.localSite=null;		

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeatureReference featureRef = remoteSite.getFeatureReferences()[0];
		
		IInstallConfiguration oldInstallConfig = site.getCurrentConfiguration();
		IConfiguredSite oldConfigSite = oldInstallConfig.getConfiguredSites()[0];
		
		((ConfiguredSite)oldConfigSite).isUpdatable(true);		
		oldConfigSite.install(featureRef.getFeature(),null,null);
		site.save();
	
		
		// Activity -> InstallConfig
		IInstallConfiguration current = site.getCurrentConfiguration();
		IActivity activity = current.getActivities()[0];	
		assertTrue(activity.getInstallConfiguration().equals(current));
		
		// ConfigSite->InstallConfig
		IConfiguredSite newConfigSite = current.getConfiguredSites()[0];
		assertTrue(newConfigSite.getInstallConfiguration().equals(current));
		
		// cleanup
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),SiteLocal.DEFAULT_CONFIG_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);	
	
	}

}

