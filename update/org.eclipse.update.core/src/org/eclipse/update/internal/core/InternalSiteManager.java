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

	private static Map sitesTypes;
	public static ILocalSite localSite;

	private static void init() {
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
	 * If the Site has a different Type/Site Handler not known up to now,
	 * it will be discovered when parsing the site.xml file.
	 */
	public static ISite getSite(URL siteURL, boolean forceCreation) throws CoreException {
		ISite site = null;

		if (siteURL == null) return null;

		// obtain type based on protocol
		String protocol = siteURL.getProtocol();
		String type = null;
		if (getSitesTypes().containsKey(protocol)) {
			type = (String) getSitesTypes().get(protocol);
		}

		try {
		// encode URL if not already encoded
		if (siteURL.toExternalForm().indexOf("%")==-1)
			siteURL = UpdateManagerUtils.encode(siteURL);
		} catch (MalformedURLException e){
			throw newCoreException("Cannot encode URL:",e);
		}

		// if error
		Exception caughtException = null;

		try {
			if (type != null) {
				site = attemptCreateSite(type, siteURL, forceCreation);
			} else {
				//DEBUG:
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					UpdateManagerPlugin.getPlugin().debug("URL's protocol :" + protocol + " is not recongnize, attempting to discover site type in site.xml");
				}

				// protocol not found, attempt to use default site
				// we will atemopt to read teh site.xml 
				// if the site.xml hasn't a specific type in it, then cancel...
				// otherwise use the type in site.xml to create the *real* site
				type = (String) getSitesTypes().get("http");
				site = attemptCreateSite(type, siteURL, forceCreation);

				// same type as we forced ? do not continue
				if (site!=null && site.getType().equals(type)) {
					throw newCoreException("The site.xml does not contain any type and the protocol of the URL is not recognized. protocol:" + protocol, null);
				}
			}

		} catch (CoreException e) {
			String siteString = (siteURL!=null)?siteURL.toExternalForm():"<NO URL>";
			throw newCoreException("Cannot create an instance of the Site using URL "+siteURL.toExternalForm()+"\r\n\r\nVerify that the site of type: "+type+" understands the URL. \r\nYou may have to add a '/' or speficy the exact file (i.e site.xml) instead of a directory.\r\n\r\n"+e.getStatus().getMessage(),e);
		}

		return site;
	}
	
		
	/**
	 * return the local site where the feature will be temporary transfered
	 */
	public static ISite getTempSite() throws CoreException {
		if (TEMP_SITE == null) {
			String tempDir = System.getProperty("java.io.tmpdir");
			if (!tempDir.endsWith(File.separator))
				tempDir += File.separator;
			String fileAsURL = (tempDir+TEMP_NAME+"/site.xml").replace(File.separatorChar,'/');
			try {				
				URL tempURL = new URL("file",null,fileAsURL);
				TEMP_SITE = InternalSiteManager.getSite(tempURL,true);
			} catch (MalformedURLException e) {
				throw newCoreException("Cannot create Temporary Site:"+fileAsURL, e);
			}
		}
		return TEMP_SITE;
	}
		
	/**
	 * Gets the sitesTypes
	 * @return Returns a Map
	 */
	public static Map getSitesTypes() {
		if (sitesTypes==null) init();
		return sitesTypes;
	}
	
	/**
	 * Attempt to create a site
	 * if the site guessed is not teh type found,
	 * attempt to create a type with the type found
	 */
	private static ISite attemptCreateSite(String guessedTypeSite, URL siteURL, boolean forceCreation) throws CoreException {
		ISite site = null;
		Exception caughtException;

		try {
			site = createSite(guessedTypeSite, siteURL, forceCreation);
		} catch (InvalidSiteTypeException e) {

			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_TYPE) {
				UpdateManagerPlugin.getPlugin().debug("The Site :" + siteURL.toExternalForm() + " is a different type than the guessed type based on the protocol. new Type:" + e.getNewType());
			}

			// the type in the site.xml is not the one expected				
			try {
				InvalidSiteTypeException exception = (InvalidSiteTypeException) e;
				// site not found and forceCreation=false
				if (exception.getNewType()==null) throw e;
				site = createSite(exception.getNewType(), siteURL, forceCreation);
			} catch (InvalidSiteTypeException e1) {
				throw newCoreException("An error occured when trying to create the Site:"+siteURL.toExternalForm()+" with the new type:"+e.getNewType(), e1);
			}
		}

		return site;
	}

	/**
	 * create an instance of a class that implements ISite
	 */
	private static ISite createSite(String siteType, URL url, boolean forceCreation) throws CoreException, InvalidSiteTypeException {
		ISite site = null;
		ISiteFactory factory = SiteTypeFactory.getInstance().getFactory(siteType);
		site = factory.createSite(url, forceCreation);
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
				URL siteURL = siteLocation.toURL();
				site = (Site) getSite(siteURL,true);
				site.save();
			} catch (MalformedURLException e) {
				throw newCoreException("Cannot create a URL from:" + siteLocation.getAbsolutePath(), e);
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

	/**
	 * returns a Core Exception
	 */
	private static CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.core",0,s,e));
	}
}