package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.internal.core.*;

public class SiteManager {
	

	private SiteManager() {
		//  Blocking instance creation
	}

	/**
	 * Returns the LocalSite i.e the different sites
	 * the user has access to (either read only or read write)
	 */	
	public static ILocalSite getLocalSite() throws CoreException{
		return InternalSiteManager.getLocalSite();
	}
	
	/** 
	 * Returns an ISite based on teh protocol of the URL
	 * If the Site has a different Type/Site Handler not known up to no,
	 * it will be discovered when parsing the site.xml file.
	 */	
	public static ISite getSite(URL siteURL) throws CoreException {
		return InternalSiteManager.getSite(siteURL);
	}
	
	
	/**
	 * return the local site where the feature will be temporary transfered
	 */
	public static ISite getTempSite(){
		return InternalSiteManager.getTempSite();
	}
}