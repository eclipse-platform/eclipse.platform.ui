package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.URL;
import org.eclipse.update.internal.core.*;

public class SiteManager {
	
	private SiteManager() {
		//  Blocking instance creation
	}
	
	public static ILocalSite getLocalSite() {
		return InternalSiteManager.getLocalSite();
	}
	
	public static ISite getSite(URL siteURL) {
		return InternalSiteManager.getSite(siteURL);
	}
}