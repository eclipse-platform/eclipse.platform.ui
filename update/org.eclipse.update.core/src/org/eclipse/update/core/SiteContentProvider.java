package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * Base site content provider
 */
public abstract class SiteContentProvider implements ISiteContentProvider {

	private URL base;
	private ISite site;

	/**
	 * Constructor for SiteContentProvider.
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
	 * Returns a URL for the identified archive. 
	 * 
	 * @see ISiteContentProvider#getArchivesReferences(String)
	 * @since 2.0
	 */
	public URL getArchiveReference(String archiveID) throws CoreException {
		try {
			return new URL(getURL(), archiveID);
		} catch (MalformedURLException e) {
			String id =
				UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status =
				new Status(
					IStatus.ERROR,
					id,
					IStatus.OK,
					Policy.bind(
						"SiteContentProvider.ErrorCreatingURLForArchiveID",
						archiveID,
						getURL().toExternalForm()),
					e);
			//$NON-NLS-1$
			throw new CoreException(status);
		}
	}

	/**
	 * Returns the site for this provider.
	 * 
	 * @see ISiteContentProvider#getSite()
	 * @since 2.0
	 */
	public ISite getSite() {
		return site;
	}

	/**
	 * Sets the site for this provider.
	 * 
	 * @param site site for this provider
	 * @since 2.0
	 */
	public void setSite(ISite site) {
		this.site = site;
	}
}