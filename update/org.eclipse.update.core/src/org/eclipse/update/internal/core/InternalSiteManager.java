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
	public static ILocalSite localSite;

	private static void init() throws CoreException {
		inst = new InternalSiteManager();
		inst.initVariables();
	}

	private void initVariables() throws CoreException{

		// sites types
		sitesTypes = new HashMap();
	/*	String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IConfigurationElement[] elements = pluginRegistry.getConfigurationElementsFor(pluginID,ISite.SIMPLE_EXTENSION_ID);
		if (elements==null || elements.length==0){
			IStatus status = new Status(IStatus.ERROR,pluginID,IStatus.OK,"Cannot find any site factory  ",null);
			throw new CoreException(status);
		} else {
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element = elements[i];
				String protocol = element.getAttribute("protocol");
				String siteType = element.getAttribute("siteFactory");
				sitesTypes.put(protocol,siteType);			
			}
		}*/

		//sitesTypes.put("http", "org.eclipse.update.internal.core.SiteURL");
		//sitesTypes.put("file", "org.eclipse.update.internal.core.SiteFile");
		
		sitesTypes.put("http", "org.eclipse.update.core.http");
		sitesTypes.put("file", "org.eclipse.update.core.file");		
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
		
		// protocol
		String protocol = siteURL.getProtocol();
		String type = (String) inst.getSitesTypes().get(protocol);
		
		// if error
		Exception caughtException = null;
		
		if (type != null) {
			try {
					site = createSite(type,siteURL);
			}  catch (InvalidSiteTypeException e){
				// the type in the site.xml is not the one expected				
				try {
						InvalidSiteTypeException exception = (InvalidSiteTypeException) e;
						site = createSite(exception.getNewType(),siteURL);
					} catch (Exception e2){
						caughtException = e2;
					}
			}catch (Exception e) {
				caughtException = e;
			}
		} else {
			// protocol not found, attempt to use default
			// if the site hasn't a specific type, then cancel...
			// otherwise use the type in site.xml
			try {
				URL newURL = new URL("http",siteURL.getHost(), siteURL.getFile());
				ISite newSite = getSite(newURL);
				if (newSite.getType().equals(inst.getSitesTypes().get(protocol))){
					//The type hasn't changed so we haven't found one in the Site.xml
					// does not continue.... 
					caughtException = new Exception("Protocol not found and site.xml does not contain specific type");
				}
			} catch (Exception e){
				String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
				IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "The protocol of the URL is not recognized", null);
				throw new CoreException(status);
			}
		}


		if (caughtException!=null){
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR, id, IStatus.OK, "cannot create an instance of the Site Object", caughtException);
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