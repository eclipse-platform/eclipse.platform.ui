/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.reconciliation;
import java.io.*;
import java.net.*;

import org.eclipse.update.configuration.*;
import org.eclipse.update.configurator.*;
import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.configurator.IPlatformConfiguration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.tests.*;

public class TestSiteReconciliation extends UpdateManagerTestCase {
	

	

	/**
	 * Test the getFeatures()
	 */
	public TestSiteReconciliation(String arg0) {
		super(arg0);
	}

	private void addConfigSite(int policy, URL url,String[] listOfPlugins) throws Exception {
		String xmlFile = ((LocalSite)SiteManager.getLocalSite()).getLocationURL().getFile();
		UpdateManagerUtils.removeFromFileSystem(new File(xmlFile));		
		InternalSiteManager.localSite=null;
		// get new config object
		IPlatformConfiguration cfig = ConfiguratorUtils.getCurrentPlatformConfiguration();
		ISitePolicy p1 = cfig.createSitePolicy(policy, listOfPlugins);	
		ISiteEntry s1 = cfig.createSiteEntry(url,p1);
		cfig.configureSite(s1);	
	}
	
	private void removeConfigSite(URL url) throws Exception {
		// get new config object
		IPlatformConfiguration cfig = ConfiguratorUtils.getCurrentPlatformConfiguration();
		ISiteEntry s1 = cfig.findConfiguredSite(url);
		assertNotNull("Unable to find site entry:"+url,s1);
		cfig.unconfigureSite(s1);
		cfig.save();
	}	
	/**
	 * Site 1 contains a feature which needs a plugin taht is not on the path when we start
	 * it will never be configured
	 */
	public void testNewSiteInclude1() throws Exception {
	
		int policy  = ISitePolicy.USER_INCLUDE;
		URL url = new URL("file",null,dataPath+"reconciliationSites/site1/");
		String[] plugins = new String[]{};
		addConfigSite(policy,url, plugins);

		ILocalSite local = SiteManager.getLocalSite();
		((LocalSite)local).setStamp(0);
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (UpdateManagerUtils.sameURL(newSites[i].getSite().getURL(),url)){
				newSite = newSites[i];
			}
		}
		
		if (newSite==null) fail("Site not found in configuration");
		
		IFeatureReference[] ref = newSite.getConfiguredFeatures();
		assertEquals("Wrong number of configured features",0,ref.length);		
		ref = ((ConfiguredSite)newSite).getConfigurationPolicy().getUnconfiguredFeatures();
		assertEquals("Wrong number of unconfigured features",0,ref.length);		
		
		removeConfigSite(url);
	}

	public void testNewSiteExclude1() throws Exception {
		
		int policy  = ISitePolicy.USER_EXCLUDE;
		URL url = new URL("file",null,dataPath+"reconciliationSites/site1/");
		String[] plugins = new String[]{};
		addConfigSite(policy,url, plugins);
		
		ILocalSite local = SiteManager.getLocalSite();
		((LocalSite)local).setStamp(0);		
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (UpdateManagerUtils.sameURL(newSites[i].getSite().getURL(),url)){
				newSite = newSites[i];
			}
		}
		
		if (newSite==null) fail("Site not found in configuration");
		
		IFeatureReference[] ref = newSite.getConfiguredFeatures();
		assertEquals("Wrong number of configured features",0,ref.length);		
		ref = ((ConfiguredSite)newSite).getConfigurationPolicy().getUnconfiguredFeatures();
		assertEquals("Wrong number of unconfigured features",0,ref.length);				
		removeConfigSite(url);
	}

	/**
	 * Site 2 contains a feature which needs a plugin taht is on the path when we start
	 * it will be configured
	 */
	public void testNewSiteInclude2() throws Exception {
		
		int policy  = ISitePolicy.USER_INCLUDE;
		URL url = new URL("file",null,dataPath+"reconciliationSites/site2/");
		String[] plugins = new String[]{};
		addConfigSite(policy,url, plugins);
		ILocalSite local = SiteManager.getLocalSite();
		((LocalSite)local).setStamp(0);		
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (UpdateManagerUtils.sameURL(newSites[i].getSite().getURL(),url)){
				newSite = newSites[i];
			}
		}
		
		if (newSite==null) fail("Site not found in configuration");
		
		IFeatureReference[] ref = newSite.getConfiguredFeatures();
		assertEquals("Wrong number of configured features",0,ref.length);
		ref = ((ConfiguredSite)newSite).getConfigurationPolicy().getUnconfiguredFeatures();
		assertEquals("Wrong number of unconfigured features",0,ref.length);
		
		removeConfigSite(url);
	}



	public void testNewSiteExclude2() throws Exception {
		
		int policy  = ISitePolicy.USER_EXCLUDE;
		URL url = new URL("file",null,dataPath+"reconciliationSites/site2/");
		String[] plugins = new String[]{};
		addConfigSite(policy,url, plugins);
		ILocalSite local = SiteManager.getLocalSite();
		((LocalSite)local).setStamp(0);		
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (UpdateManagerUtils.sameURL(newSites[i].getSite().getURL(),url)){
				newSite = newSites[i];
			}
		}
		
		if (newSite==null) fail("Site not found in configuration");
		
		IFeatureReference[] ref = newSite.getConfiguredFeatures();
		assertEquals("Wrong number of configured features",0,ref.length);
		ref = ((ConfiguredSite)newSite).getConfigurationPolicy().getUnconfiguredFeatures();
		assertEquals("Wrong number of unconfigured features",0,ref.length);

		removeConfigSite(url);
	}
}

