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
	public ISite createSite(URL url, boolean forceCreation) throws CoreException, InvalidSiteTypeException {

		Site site = null;
		URL siteXML = null;
		InputStream siteStream = null;
		
		try {		
			url =removeSiteXML(url);
			SiteURLContentProvider contentProvider = new SiteURLContentProvider(url);
		
			siteXML = new URL(url,Site.SITE_XML);
			
			URL resolvedURL = URLEncoder.encode(siteXML);
			siteStream = resolvedURL.openStream();
			
			SiteModelFactory factory = (SiteModelFactory) this;
			site = (Site)factory.parseSite(siteStream);
			
			site.setSiteContentProvider(contentProvider);
			contentProvider.setSite(site);			
			site.resolve(url, getResourceBundle(url));
			site.markReadOnly();			
			
		} catch (IOException e) {
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "WARNING: cannot find site.xml in the site:" + url.toExternalForm(), e);
			throw new CoreException(status);
		} catch (Exception e) {
			
			if (e instanceof SAXException){
				SAXException exception = (SAXException) e;
				if(exception.getException() instanceof InvalidSiteTypeException){
					throw (InvalidSiteTypeException)exception.getException();
				}
			}
			
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.WARNING, id, IStatus.OK, "Error parsing site.xml in the site:" + url.toExternalForm(), e);
			throw new CoreException(status);
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

	/**
	 * removes site.xml from the URL
	 */
	private URL removeSiteXML(URL url) throws MalformedURLException{
		URL result = url;
		
		// no need for decode encode
		if (url!=null && url.getFile().endsWith(Site.SITE_XML)){
			String ref = url.getRef();
			int index = url.getFile().lastIndexOf(Site.SITE_XML);
			String newPath = url.getFile().substring(0, index);	
			if (ref!=null) newPath += newPath+"#"+ref;
			result = new URL(url.getProtocol(), url.getHost(), url.getPort(), newPath);
		}
		return result;
	}
	
}
