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
package org.eclipse.update.core;
 
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.core.*;

/**
 * Base site content provider
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
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
						"SiteContentProvider.ErrorCreatingURLForArchiveID", //$NON-NLS-1$
						archiveID,
						getURL().toExternalForm()),
					e);
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
