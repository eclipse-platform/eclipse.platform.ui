package org.eclipse.update.tests.reconciliation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy;
import org.eclipse.core.internal.boot.PlatformConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.InternalSiteManager;
import org.eclipse.update.internal.core.SiteLocal;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteReconciliation extends UpdateManagerTestCase {
	

	

	/**
	 * Test the getFeatures()
	 */
	public TestSiteReconciliation(String arg0) {
		super(arg0);
	}

	private void addConfigSite(int policy, URL url,String[] listOfPlugins) throws Exception {
		String xmlFile = ((SiteLocal)SiteManager.getLocalSite()).getLocationURL().getFile();
		UpdateManagerUtils.removeFromFileSystem(new File(xmlFile));		
		InternalSiteManager.localSite=null;
		// get new config object
		PlatformConfiguration cfig = (PlatformConfiguration)BootLoader.getCurrentPlatformConfiguration();
		ISitePolicy p1 = cfig.createSitePolicy(policy, listOfPlugins);	
		ISiteEntry s1 = cfig.createSiteEntry(url,p1);
		cfig.configureSite(s1);	
	}
	
	private void removeConfigSite(URL url) throws Exception {
		// get new config object
		PlatformConfiguration cfig = (PlatformConfiguration)BootLoader.getCurrentPlatformConfiguration();
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
		((SiteLocal)local).setStamp(0);
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (sameURL(newSites[i].getSite().getURL(),url)){
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
		((SiteLocal)local).setStamp(0);		
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (sameURL(newSites[i].getSite().getURL(),url)){
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
		((SiteLocal)local).setStamp(0);		
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (sameURL(newSites[i].getSite().getURL(),url)){
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
		((SiteLocal)local).setStamp(0);		
		IConfiguredSite[] newSites = local.getCurrentConfiguration().getConfiguredSites();
		IConfiguredSite newSite = null;
		for (int i = 0; i < newSites.length; i++) {
			if (sameURL(newSites[i].getSite().getURL(),url)){
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

	/*
	 * Compares two URL for equality
	 * Return false if one of them is null
	 */
	private boolean sameURL(URL url1, URL url2) {
		if (url1 == null)
			return false;
		if (url1.equals(url2))
			return true;

		// check if URL are file: URL as we may
		// have 2 URL pointing to the same featureReference
		// but with different representation
		// (i.e. file:/C;/ and file:C:/)
		if (!"file".equalsIgnoreCase(url1.getProtocol()))
			return false;
		if (!"file".equalsIgnoreCase(url2.getProtocol()))
			return false;

		File file1 = new File(url1.getFile());
		File file2 = new File(url2.getFile());

		if (file1 == null)
			return false;

		return (file1.equals(file2));
	}
}

