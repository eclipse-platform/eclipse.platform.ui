package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.SiteModelFactory;
import org.xml.sax.SAXException;

public class SiteURLFactory extends BaseSiteFactory {


	/*
	 * @see ISiteFactory#createSite(URL)
	 */
	public ISite createSite(URL url) throws CoreException, InvalidSiteTypeException {

		Site site = null;
		URL siteXML = null;
		InputStream siteStream = null;
		
		try {		
			SiteURLContentProvider contentProvider = new SiteURLContentProvider(url);
		
			siteXML = new URL(url,Site.SITE_XML);
			siteStream = siteXML.openStream();
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

}
