package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.model.InvalidSiteTypeException;

/**
 * Site factory interface.
 * A site factory is used to construct new instances of concrete
 * sites. 
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.BaseSiteFactory
 * @since 2.0
 */

public interface ISiteFactory {

	/**
	 * Returns a site defined by the supplied URL. 
	 * <p>
	 * The actual interpretation of the URL is site-type specific.
	 * In most cases the URL will point to some site-specific
	 * file that can be used (directly or indirectly) to construct
	 * the site object.
	 * </p>
	 * @param url URL interpreted by the site
	 * @param forceCreation
	 * @return concrete site object
	 * @exception IOException
	 * @exception ParsingException
	 * @exception InvalidSiteTypeException the referenced site type is
	 * not a supported type for this factory 
	 * @since 2.0 
	 */

	public ISite createSite(URL url)
		throws CoreException, InvalidSiteTypeException;

}