package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.ParsingException;
import org.eclipse.update.core.model.SiteModelFactory;
import org.xml.sax.SAXException;

public class SiteURLFactory extends BaseSiteFactory {


	/*
	 * @see ISiteFactory#createSite(URL, boolean)
	 * 
	 * the URL can be of the following form
	 * 1 protocol://...../
	 * 2 protocol://.....
	 * 3 protocol://..../site.xml
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
	 */
	public ISite createSite(URL url) throws IOException,  InvalidSiteTypeException, ParsingException {

		Site site = null;
		InputStream siteStream = null;
		
		try {		
			SiteURLContentProvider contentProvider = new SiteURLContentProvider(url);
		
			URL resolvedURL = URLEncoder.encode(url);
			siteStream = resolvedURL.openStream();
			
			SiteModelFactory factory = (SiteModelFactory) this;
			site = (Site)factory.parseSite(siteStream);
			
			site.setSiteContentProvider(contentProvider);
			contentProvider.setSite(site);			
			site.resolve(url, getResourceBundle(url));
			site.markReadOnly();			

		} finally {
			try {
				siteStream.close();
			} catch (Exception e) {
			}
		}
		return site;
	}
		
	/*
	 * @see SiteModelFactory#canParseSiteType(String)
	 */
	public boolean canParseSiteType(String type) {
		return (super.canParseSiteType(type) || SiteURLContentProvider.SITE_TYPE.equalsIgnoreCase(type));
	}
	
}
