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

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.*;

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
	 * @return site object
	 * @exception CoreException
	 * @exception InvalidSiteTypeException the referenced site type is
	 * not a supported type for this factory 
	 * @since 2.0 
	 */
	public ISite createSite(URL url)
		throws CoreException, InvalidSiteTypeException;
}
