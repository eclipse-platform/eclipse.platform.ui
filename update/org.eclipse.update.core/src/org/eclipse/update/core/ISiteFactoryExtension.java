package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;

/**
 * <p>
 * This is an extension to the standard ISiteFactory interface.
 * If a factory implements this interface and is handling
 * URL connections, a progress monitor can be passed to 
 * allow canceling of frozen connections. 
 * </p>
 * <p>Input stream is obtained from the connection on
 * a separate thread. When connection is canceled, 
 * the thread is still active. It is allowed to terminate
 * when the connection times out on its own.
 * </p>
 * @see org.eclipse.update.core.BaseSiteFactory
 * @since 2.1
 */

public interface ISiteFactoryExtension {
	/**
	 * Returns a site defined by the supplied URL. 
	 * <p>
	 * The actual interpretation of the URL is site-type specific.
	 * In most cases the URL will point to some site-specific
	 * file that can be used (directly or indirectly) to construct
	 * the site object.
	 * </p>
	 * @param url URL interpreted by the site
	 * @param monitor a progress monitor that can be canceled
	 * @return site object
	 * @exception CoreException
	 * @exception InvalidSiteTypeException the referenced site type is
	 * not a supported type for this factory 
	 * @since 2.0 
	 */
	public ISite createSite(URL url, IProgressMonitor monitor)
		throws CoreException, InvalidSiteTypeException;
}