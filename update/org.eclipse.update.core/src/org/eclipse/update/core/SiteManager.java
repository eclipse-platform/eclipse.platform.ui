package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.internal.plugins.ConfigurationProperty;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.internal.core.*;

public class SiteManager {
	

	/**
	 * @since 2.0 
	 */
	private SiteManager() {
		//  Blocking instance creation
	}

	/**
	 * Returns the LocalSite i.e the different sites
	 * the user has access to (either read only or read write)
	 * 
	 * @return the local site
	 * @since 2.0 
	 */
	
	public static ILocalSite getLocalSite() throws CoreException{
		return InternalSiteManager.getLocalSite();
	}
	
	/** 
	 * Returns an site based on the protocol of the URL
	 * 
	 * If the Site has a different Type/Site Handler not known up to now,
	 * it will be discovered when parsing the <code>site.xml</code> file.
	 * 
	 * If there is no XML file, and the site is on the file system, we will attempt to 
	 * discover any feature or archives.
	 * 
	 * If a site doesn't exists, it will be created in memory. use <code>ISite.save()</code>
	 * to persist the site.
	 * 
	 * The site is not created in the LocalSite.
	 * 
	 * @return the site which maps to this URL
	 * @since 2.0 
	 */
	
	public static ISite getSite(URL siteURL) throws CoreException {
		return InternalSiteManager.getSite(siteURL);
	}
	
	
	/**
	 * Returns a local temporary site where 
	 * some feature may be temporary transfered before
	 * being installed.
	 * 
	 * @return the temporary site
	 * @since 2.0 
	 */

	public static ISite getTempSite() throws CoreException {
		return InternalSiteManager.getTempSite();
	}
	

		
	/**
	 * Creates a new site on the file system
	 * This is the only Site we can create.
	 * 
	 * Does not add the Site to the LocalSite
	 * 
	 * @param siteLocation
	 * @throws CoreException
	 * @since 2.0 
	 */

	public static ISite createSite(File siteLocation) throws CoreException {
		return InternalSiteManager.createSite(siteLocation);	
	}
	
	/**
	 * Creates a Configuration Site for an  ISite
	 * The policy is from <code> org.eclipse.core.boot.IPlatformConfiguration</code>
	 * @since 2.0 
	 */

	public static IConfigurationSite createConfigurationSite(ISite site,int policy){
		return InternalSiteManager.createConfigurationSite(site,policy);
	}
	
	/**
	 * Creates a Configuration policy
	 * The policy is from <code> org.eclipse.core.boot.IPlatformConfiguration</code>
	 * @since 2.0 
	 */

	public static IConfigurationPolicy createConfigurationPolicy(int policy){
		return InternalSiteManager.createConfigurationPolicy(policy);
	}
}