package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;

/**
 * Base site content provider
 */
public abstract class SiteContentProvider implements ISiteContentProvider {


	private URL base;
	private ISite site;

	/**
	 * Content selector used in archive operations
	 * 
	 * @since 2.0
	 */
	public interface IContentSelector {
		
		/**
		 * Indicates whether the archive content entry should be
		 * selected for the operation
		 * 
		 * @since 2.0
		 */
		public boolean include(String entry);
		
		/**
		 * Defines a content reference identifier for the 
		 * archive content entry
		 * 
		 * @since 2.0
		 */
		public String defineIdentifier(String entry);
	}

	/**
	 * Constructor for SiteContentProvider.
	 */
	public SiteContentProvider(URL url) {
		super();
		this.base = url;
	}

	/*
	 * @see ISiteContentProvider#getURL()
	 */
	public URL getURL() {
		return base;
	}

	/*
	 * @see ISiteContentProvider#getArchivesReferences(String)
	 */
	public abstract URL getArchiveReference(String archiveID) throws CoreException;

	/**
	 * Sets the site.
	 * @param site The site to set
	 */
	public void setSite(ISite site) {
		this.site = site;
	}

	/**
	 * Gets the site.
	 * @return Returns a ISite
	 */
	public ISite getSite() {
		return site;
	}

}
