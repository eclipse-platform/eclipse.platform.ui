package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.update.internal.core.*;

public class SiteManager {
	
	private static ISite TEMP_SITE;
	
	private SiteManager() {
		//  Blocking instance creation
	}
	
	public static ILocalSite getLocalSite() {
		return InternalSiteManager.getLocalSite();
	}
	
	public static ISite getSite(URL siteURL) {
		return InternalSiteManager.getSite(siteURL);
	}
	
	
	/**
	 * return the local site where the feature will be temporary transfered
	 */
	/* package */ static ISite getTempSite(){
		if (TEMP_SITE==null){
			try {
			TEMP_SITE = new FileSite(new URL("file:///"+System.getProperty("java.io.tmpdir")));
			} catch (MalformedURLException e){
				//FIXME: should never occur... hardcoded ?
				e.printStackTrace();
			}
		}
		return TEMP_SITE;
	}
}