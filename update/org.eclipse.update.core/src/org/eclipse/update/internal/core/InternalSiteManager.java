package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * 
 */

public class InternalSiteManager {

	private static ISite TEMP_SITE;
	public static final String TEMP_NAME = "update_tmp";

	private Map sites;
	private Map sitesTypes;
	private static InternalSiteManager inst;
	private static ILocalSite localSite;

	private static void init() {
		inst = new InternalSiteManager();
		inst.initVariables();
	}

	private void initVariables() {
		// sites

		// sites types
		sitesTypes = new HashMap();
		sitesTypes.put("http", "org.eclipse.update.internal.core.SiteURL");
		sitesTypes.put("file", "org.eclipse.update.internal.core.SiteFile");
	}

	/**
	 * Returns the LocalSite i.e the different sites
	 * the user has access to (either read only or read write)
	 */
	public static ILocalSite getLocalSite() throws CoreException {
		if (localSite == null)
			localSite = new SiteLocal();
		return localSite;
	}

	/** 
	 * Returns an ISite based on teh protocol of the URL
	 * If the Site has a different Type/Site Handler not known up to no,
	 * it will be discovered when parsing the site.xml file.
	 */
	public static ISite getSite(URL siteURL) throws CoreException {
		ISite site = null;
		if (inst == null)
			init();
		String protocol = siteURL.getProtocol();
		String clazz = (String) inst.getSitesTypes().get(protocol);
		if (clazz != null) {
			try {
				Class siteClass = Class.forName(clazz);
				Class classArgs[] = { siteURL.getClass()};
				Constructor constructor = siteClass.getConstructor(classArgs);
				Object objArgs[] = { siteURL };
				site = (ISite) constructor.newInstance(objArgs);

			} catch (Exception e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "cannot create an instance of the Site Object", e);
				throw new CoreException(status);
			}
		} else {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "The protocol of the URL is not recognized", null);
			throw new CoreException(status);
		}
		return site;
	}

	/**
	 * return the local site where the feature will be temporary transfered
	 */
	public static ISite getTempSite() throws CoreException {
		if (TEMP_SITE == null) {
			try {
				String tempDir = System.getProperty("java.io.tmpdir");
				if (!tempDir.endsWith(File.separator))
					tempDir += File.separator;
				TEMP_SITE = new SiteFile(new URL("file", null, tempDir + TEMP_NAME + '/')); // URL must end with '/' if they refer to a path/directory
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "Cannot create Temporary Site", e);
				throw new CoreException(status);
			}
		}
		return TEMP_SITE;
	}
	/**
	 * Gets the sites
	 * @return Returns a Map
	 */
	public Map getSites() {
		return sites;
	}
	/**
	 * Sets the sites
	 * @param sites The sites to set
	 */
	public void setSites(Map sites) {
		this.sites = sites;
	}

	/**
	 * Gets the sitesTypes
	 * @return Returns a Map
	 */
	public Map getSitesTypes() {
		return sitesTypes;
	}
	/**
	 * Sets the sitesTypes
	 * @param sitesTypes The sitesTypes to set
	 */
	public void setSitesTypes(Map sitesTypes) {
		this.sitesTypes = sitesTypes;
	}

	/**
	 * Creates a new site on the file system
	 * This is the only Site we can create.
	 * 
	 * @param siteLocation
	 * @throws CoreException
	 */
	public static ISite createSite(File siteLocation) throws CoreException {
		Site site = null;
		if (siteLocation != null) {
			try {
				URL siteURL = new URL("file", null, siteLocation.getAbsolutePath());
				site = (Site) getSite(siteURL);
				site.save();
			} catch (MalformedURLException e) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "cannot create a URL from:"+siteLocation.getAbsolutePath(), e);
				throw new CoreException(status);
			}
		}
		return site;
	}

	/**
	 * Creates a Configuration Site for an  ISite
	 * The policy is from <code> org.eclipse.core.boot.IPlatformConfiguration</code>
	 */
	public static IConfigurationSite createConfigurationSite(ISite site, int policy) {
		return new ConfigurationSite(site, createConfigurationPolicy(policy));
	}
	
	/**
	 * Creates a Configuration policy
	 * The policy is from <code> org.eclipse.core.boot.IPlatformConfiguration</code>
	 */
	public static IConfigurationPolicy createConfigurationPolicy(int policy){
		return new ConfigurationPolicy(policy);
	}
	

}