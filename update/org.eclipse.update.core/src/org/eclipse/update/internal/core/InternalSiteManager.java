package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.ParsingException;

/**
 * 
 */

public class InternalSiteManager {

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
		if (localSite == null) {
			localSite = SiteLocal.getLocalSite();
		}
		return localSite;
	}

	/** 
	 * Returns an ISite based on teh protocol of the URL
	 * If the Site has a different Type/Site Handler not known up to now,
	 * it will be discovered when parsing the site.xml file.
	 */
	public static ISite getSite(URL siteURL, boolean forceCreation) throws CoreException {
		ISite site = null;

		if (siteURL == null)
			return null;

		// obtain type based on protocol
		String protocol = siteURL.getProtocol();
		String type = null;
		if (getSitesTypes().containsKey(protocol)) {
			type = (String) getSitesTypes().get(protocol);
		}

		// attempt creation of site based on the type found
		try {
			if (type != null) {
				site = attemptCreateSite(type, siteURL, forceCreation);
			} else {
				//DEBUG:
				if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS) {
					UpdateManagerPlugin.getPlugin().debug("URL's protocol :" + protocol + " is not recongnize, attempting to discover site type in site.xml");
				}

				// protocol not found, attempt to use default site
				// we will attempt to read the site.xml 
				// if the site.xml hasn't a specific type in it, then we fail...
				// otherwise use the type in site.xml to create the *real* site
				type = (String) getSitesTypes().get("http");
				site = attemptCreateSite(type, siteURL, forceCreation);

				// same type as we forced ? do not continue
				if (site != null) {
					if (site.getType() == null || site.getType().trim().equals("") || site.getType().equals(type)) {
						throw newCoreException("The site.xml does not contain any type and the protocol of the URL is not a default protocol:" + protocol + "\r\n the site provider should provide a site factory and set a type in the site.", null);
					}
				}
			}

		} catch (CoreException e) {
			String siteString = (siteURL != null) ? siteURL.toExternalForm() : "<NO URL>";
			throw newCoreException(
				"Cannot create an instance of the Site using URL "
					+ siteURL.toExternalForm()
					+ "\r\n\r\nVerify that the site of type: "
					+ type
					+ " understands the URL. \r\nYou may have to add a '/' or speficy the exact file (i.e site.xml) instead of a directory.\r\n\r\n"
					+ e.getStatus().getMessage(),
				e);
		}

		return site;
	}

	/**
	 * Gets the sitesTypes
	 * @return Returns a Map
	 */
	public static Map getSitesTypes() {
		if (sitesTypes == null)
			init();
		return sitesTypes;
	}

	/**
	 * Attempt to create a site
	 * if the site guessed is not the type found,
	 * attempt to create a type with the type found in the site.xml
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
			// attempt to use this type instead			
			try {
				InvalidSiteTypeException exception = (InvalidSiteTypeException) e;
				if (exception.getNewType() == null)
					throw e;
				site = createSite(exception.getNewType(), siteURL, forceCreation);
			} catch (InvalidSiteTypeException e1) {
				throw newCoreException("An error occured when trying to create the Site:" + siteURL.toExternalForm() + " with the new type:" + e.getNewType(), e1);
			}
		}

		return site;
	}

	/**
	 * create an instance of a class that implements ISite
	 * 
	 * the URL can be of the following form
	 * 1 protocol://...../
	 * 2 protocol://.....
	 * 3 protocol://..../site.xml
	 * 4 protocol://...#...
	 * 
	 * 1 If the file of the file of teh url ends with '/', attempt to open the stream.
	 * if it fails, add site.xml and attempt to open the stream
	 * 
	 * 2 attempt to open the stream
	 * 	fail
	 * 		add '/site.xml' and attempt to open the stream
	 * 	sucess
	 * 		attempt to parse, if it fails, add '/site.xml' and attempt to open teh stream
	 * 
	 * 3 open the stream
	 * 
	 * 4 open the stream	
	 */
	private static ISite createSite(String siteType, URL url, boolean forceCreation) throws CoreException, InvalidSiteTypeException {
		ISite site = null;
		ISiteFactory factory = SiteTypeFactory.getInstance().getFactory(siteType);

		try {
			
			site = factory.createSite(url, forceCreation);
			
		} catch (IOException e) {
			// if the URL is pointing to either a file 
			// or a directory, without reference			
			if (url.getRef() != null) {
				// 4 nothing we can do
				throw newCoreException("Error accessing url:"+url.toExternalForm()+"\r\n"+e.getMessage(),e);
			} else if (url.getFile().endsWith("/")) {
				// 1 try to add site.xml
				try {
					url = new URL(url,Site.SITE_XML);
				} catch (MalformedURLException e1){
					throw newCoreException("Cannot create URL:"+url.toExternalForm()+"+"+Site.SITE_XML,e1);
				}
				try {
					site = factory.createSite(url, forceCreation);
				} catch (ParsingException e1){
					throw newCoreException("Error parsing URL:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);					
				} catch (IOException e1){
					throw newCoreException("Error accessing url:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);					
				}
			} else if (url.getFile().endsWith(Site.SITE_XML)) {
				// 3 nothing we can do
				throw newCoreException("Error accessing url:"+url.toExternalForm()+"\r\n"+e.getMessage(),e);
			} else {
				// 2 try to add /site.xml
				try {
					url = new URL(url.getProtocol(),url.getHost(), url.getPort(), url.getFile()+"/"+Site.SITE_XML);
				} catch (MalformedURLException e1){
					throw newCoreException("Cannot create URL:"+url.toExternalForm()+"+"+Site.SITE_XML,e1);
				}
				
				try {
					site = factory.createSite(url, forceCreation);
				} catch (ParsingException e1){
					throw newCoreException("Error parsing URL:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);					
				} catch (IOException e1){
					throw newCoreException("Error accessing url:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);					
				}				
			}

		} catch (ParsingException e) {
			
			// if the URL is pointing to either a file 
			// or a directory, without reference			
			if (url.getRef() != null) {
				// 4 nothing we can do
				throw newCoreException("Error parsing URL:"+url.toExternalForm()+"\r\n"+e.getMessage(),e);
			} else if (url.getFile().endsWith("/")) {
				// 1 try to add site.xml
				try {
					url = new URL(url,Site.SITE_XML);
				} catch (MalformedURLException e1){
					throw newCoreException("Cannot create URL:"+url.toExternalForm()+"+"+Site.SITE_XML,e1);
				}
				try {
					site = factory.createSite(url, forceCreation);
				} catch (ParsingException e1){
					throw newCoreException("Error parsing URL:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);					
				} catch (IOException e1){
					throw newCoreException("Error accessing url:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);
				}				

			} else if (url.getFile().endsWith(Site.SITE_XML)) {
				// 3 nothing we can do
				throw newCoreException("Error parsing URL:"+url.toExternalForm()+"\r\n"+e.getMessage(),e);					
			} else {
				// 2 try to add /site.xml
				
				try {
					url = new URL(url.getProtocol(),url.getHost(), url.getPort(), url.getFile()+"/"+Site.SITE_XML);
				} catch (MalformedURLException e1){
					throw newCoreException("Cannot create URL:"+url.toExternalForm()+"+"+Site.SITE_XML,e1);
				}
				try {
					site = factory.createSite(url, forceCreation);
				} catch (ParsingException e1){
					throw newCoreException("Error parsing URL:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);					
				} catch (IOException e1){
					throw newCoreException("Error accessing url:"+url.toExternalForm()+"\r\n"+e1.getMessage(),e1);
				}							
		
			}
		} 

		return site;
	}

	/**
	 * Creates a new site on the file system
	 * This is the only Site we can create.
	 * 
	 * @param siteLocation
	 * @throws CoreException
	 */
	 private static ISite createSite(File siteLocation) throws CoreException {
		Site site = null;
		if (siteLocation != null) {
			try {
				siteLocation.mkdirs();
				URL siteURL = siteLocation.toURL();
				site = (Site) getSite(siteURL, true);
				// FIXME, when creating a site, should we manage site.xml ?
				//site.save();
			} catch (MalformedURLException e) {
				throw newCoreException("Cannot create a URL from:" + siteLocation.getAbsolutePath(), e);
			}
		}
		return site;
	}

	/**
	 * Creates a Configuration Site and a new Site
	 * The policy is from <code> org.eclipse.core.boot.IPlatformConfiguration</code>
	 */
	public static IConfigurationSite createConfigurationSite(File file, int policy) throws CoreException {
	
		ISite site = createSite(file);

		//create config site
		BaseSiteLocalFactory factory = new BaseSiteLocalFactory();
		ConfigurationSite configSite = (ConfigurationSite)factory.createConfigurationSiteModel((SiteMapModel)site,policy);
		configSite.setPlatformURLString(site.getURL().toExternalForm());
		configSite.setInstallSite(true);
		
		// obtain the list of plugins
		IPlatformConfiguration runtimeConfiguration = BootLoader.getCurrentPlatformConfiguration();		
		ConfigurationPolicy configurationPolicy = (ConfigurationPolicy)configSite.getConfigurationPolicy();
		String[] pluginPath = configurationPolicy.getPluginPath(site,null);
		IPlatformConfiguration.ISitePolicy sitePolicy = runtimeConfiguration.createSitePolicy(configurationPolicy.getPolicy(), pluginPath);
		
		// change runtime					
		IPlatformConfiguration.ISiteEntry siteEntry = runtimeConfiguration.createSiteEntry(site.getURL(), sitePolicy);
		runtimeConfiguration.configureSite(siteEntry);
		
		return configSite;
	}


	/**
	 * returns a Core Exception
	 */
	private static CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR, "org.eclipse.update.core", 0, s, e));
	}
}