package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
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

	/*
	 * @see ISiteContentProvider#getURL()
	 */
	public URL getURL() {
		return base;
	}

	/*
	 * @see ISiteContentProvider#getArchivesReferences(String)
	 */
	public URL getArchiveReference(String archiveID) throws CoreException{
		try {
			return new URL(getURL(),archiveID);
		} catch (MalformedURLException e){
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error creating URL",e);
			throw new CoreException(status);	
		}		
	}

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
