/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.core;
 
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.internal.core.Policy;

/**
 * Base site content provider
 */
public abstract class SiteContentProvider implements ISiteContentProvider {

	private URL base;
	private ISite site;

	/**
	 * Constructor for SiteContentProvider
	 */
	public SiteContentProvider(URL url) {
		super();
		this.base = url;
	}

	/**
	 * Returns the URL of this site
	 * 
	 * @see ISiteContentProvider#getURL()
	 * @since 2.0
	 */
	public URL getURL() {
		return base;
	}

	/**
	 * Returns a URL for the identified archive
	 * 
	 * @see ISiteContentProvider#getArchiveReference(String)
	 * @since 2.0
	 */
	public URL getArchiveReference(String archiveID) throws CoreException {
		try {
			return new URL(getURL(), archiveID);
		} catch (MalformedURLException e) {
			throw Utilities.newCoreException(
					Policy.bind(
						"SiteContentProvider.ErrorCreatingURLForArchiveID",
						archiveID,
						getURL().toExternalForm()),
					e);
			//$NON-NLS-1$
		}
	}

	/**
	 * Returns the site for this provider
	 * 
	 * @see ISiteContentProvider#getSite()
	 * @since 2.0
	 */
	public ISite getSite() {
		return site;
	}

	/**
	 * Sets the site for this provider
	 * 
	 * @param site site for this provider
	 * @since 2.0
	 */
	public void setSite(ISite site) {
		this.site = site;
	}
}
