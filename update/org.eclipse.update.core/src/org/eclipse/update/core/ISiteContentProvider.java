package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.URL;

import org.eclipse.core.runtime.CoreException;

/**
 * Site content provider.
 * A site content provider is an abstraction of each site internal 
 * organization. It allows the site content to be accessed in
 * a standard way regardless of the internal organization. All concrete site
 * implementations need to implement a site content provider.
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.SiteContentProvider
 * @since 2.0
 */
 
public interface ISiteContentProvider {
	
	/**
	 * Returns the URL of this site
	 * 
	 * @return site URL
	 * @since 2.0
	 */	
	public URL getURL();
			
	
	/**
	 * Returns a URL for the identified archive. 
	 * 
	 * @param id archive identifier
	 * @return archive URL
	 * @exception CoreException 
	 * @since 2.0 
	 */
	public URL getArchiveReference(String id)  throws CoreException;

	/**
	 * Returns the site for this provider.
	 * 
	 * @return provider site
	 * @since 2.0
	 */
	public ISite getSite();	
	
	/**
	 * Sets the site for this provider.
	 * In general, this method should only be called as part of
	 * site creation. Once set, the site should not be reset.
	 * 
	 * @param site provider site
	 * @since 2.0
	 */
	public void setSite(ISite site);
}


