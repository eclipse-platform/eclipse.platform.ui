package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;

/**
 * 
 */

public class InternalSiteManager {

	private static ISite TEMP_SITE;
	public static final String TEMP_NAME = "update_tmp";

	private Map sites;
	private Map sitesTypes;
	private static InternalSiteManager singleton;
	public static ILocalSite localSite;

	private static void init() throws CoreException {
		singleton = new InternalSiteManager();
		singleton.initVariables();
	}

	private void initVariables() throws CoreException {

		// sites types
		sitesTypes = new HashMap();

		// assign default type to protocol		
		sitesTypes.put("http", SiteURLContentProvider.SITE_TYPE);
		sitesTypes.put("file", SiteFileContentProvider.SITE_TYPE);
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
		if (singleton == null)
			init();

		// protocol
		String protocol = siteURL.getProtocol();
		String type = null;
		if (singleton.getSitesTypes().containsKey(protocol)) {
			type = (String) singleton.getSitesTypes().get(protocol);
		}

		// if error
		Exception caughtException = null;

		try {
			if (type != null) {
				site = attemptCreateSite(type, siteURL);
			} else {
				//DEBUG:
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					UpdateManagerPlugin.getPlugin().debug("URL's protocol :" + protocol + " is not recongnize, attempting to discover site type in site.xml");
				}

				// protocol not found, attempt to use default site
				// we will atemopt to read teh site.xml 
				// if the site.xml hasn't a specific type in it, then cancel...
				// otherwise use the type in site.xml to create the *real* site
				type = (String) singleton.getSitesTypes().get("http");
				site = attemptCreateSite(type, siteURL);

				// same type as we forced ? do not continue
				if (site!=null && site.getType().equals(type)) {
					String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
					IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "The site.xml does not contain any type and the protocol of the URL is not recognized. protocol:" + protocol, null);
					throw new CoreException(status);
				}
			}

		} catch (CoreException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "cannot create an instance of the Site Object", e);
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
				TEMP_SITE = InternalSiteManager.getSite(new URL("file", null, tempDir + TEMP_NAME + '/')); // URL must end with '/' if they refer to a path/directory
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
	 * Attempt to create a site
	 * if the site guessed is not teh type found,
	 * attempt to create a type with the type found
	 */
	private static ISite attemptCreateSite(String guessedTypeSite, URL siteURL) throws CoreException {
		ISite site = null;
		Exception caughtException;

		try {
			site = createSite(guessedTypeSite, siteURL);
		} catch (InvalidSiteTypeException e) {

			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_TYPE) {
				UpdateManagerPlugin.getPlugin().debug("The Site :" + siteURL.toExternalForm() + " is a different type than the guessed type based on the protocol. new Type:" + e.getNewType());
			}

			// the type in the site.xml is not the one expected				
			try {
				InvalidSiteTypeException exception = (InvalidSiteTypeException) e;
				site = createSite(exception.getNewType(), siteURL);
			} catch (InvalidSiteTypeException e1) {
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "An error occured when trying to create a Site with the new type", e1);
				throw new CoreException(status);
			}
		}

		return site;
	}

	/**
	 * create an instance of a class that implements ISite
	 */
	private static ISite createSite(String siteType, URL url) throws CoreException, InvalidSiteTypeException {
		ISite site = null;
		ISiteFactory factory = SiteTypeFactory.getInstance().getFactory(siteType);
		site = factory.createSite(url);
		return site;
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
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "cannot create a URL from:" + siteLocation.getAbsolutePath(), e);
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
	public static IConfigurationPolicy createConfigurationPolicy(int policy) {
		return new ConfigurationPolicy(policy);
	}

}