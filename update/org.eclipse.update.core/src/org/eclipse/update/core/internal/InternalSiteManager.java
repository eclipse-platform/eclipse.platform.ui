package org.eclipse.update.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.update.core.*;
import java.net.URL;

public class InternalSiteManager {
	
	private InternalSiteManager() {
	}
	public static ILocalSite getLocalSite() {
		return null;
	}
	
	public static ISite getSite(URL siteURL) {
		return null;
	}
}