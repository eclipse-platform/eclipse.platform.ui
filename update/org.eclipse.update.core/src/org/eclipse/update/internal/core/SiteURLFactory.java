/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;

public class SiteURLFactory extends BaseSiteFactory implements ISiteFactoryExtension {
	
	/*
	 * For backward compatibility.
	 */
	public ISite createSite(URL url) throws CoreException, InvalidSiteTypeException {
		return createSite(url, null);
	}
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
	public ISite createSite(URL url, IProgressMonitor monitor) throws CoreException, InvalidSiteTypeException {
		Site site = null;
		InputStream siteStream = null;
	
		try {
			SiteURLContentProvider contentProvider = new SiteURLContentProvider(url);
	
			URL resolvedURL = URLEncoder.encode(url);
			Response response = UpdateCore.getPlugin().get(resolvedURL);
			UpdateManagerUtils.checkConnectionResult(response, resolvedURL);
			siteStream = response.getInputStream(monitor);
			// the stream can be null if the user cancels the connection
			if (siteStream==null) return null;

			SiteModelFactory factory = this;
			site = (Site) factory.parseSite(siteStream);
	
			site.setSiteContentProvider(contentProvider);
			contentProvider.setSite(site);
			site.resolve(url, url);
			site.markReadOnly();
		} catch (MalformedURLException e) {
			throw Utilities.newCoreException(Policy.bind("SiteURLFactory.UnableToCreateURL", url == null ? "" : url.toExternalForm()), e);	//$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			throw Utilities.newCoreException(Policy.bind("SiteURLFactory.UnableToAccessSiteStream", url == null ? "" : url.toExternalForm()), ISite.SITE_ACCESS_EXCEPTION, e);	//$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			if (siteStream != null) {
				try {
					siteStream.close();
				} catch (IOException e) {
				}
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
