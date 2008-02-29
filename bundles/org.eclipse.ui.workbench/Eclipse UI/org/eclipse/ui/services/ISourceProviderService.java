/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.services;

import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;

/**
 * <p>
 * A service from which all of the source providers can be retrieved.
 * </p>
 * <p>
 * Note: Clients should not extend or implement this interface.
 * </p>
 * <p>
 * This API is experimental and may change before the end of M6.
 * </p>
 * 
 * @since 3.4
 */
public interface ISourceProviderService {

	/**
	 * Retrieves a source provider providing the given source. This is used by
	 * clients who only need specific sources.
	 * 
	 * @param sourceName
	 *            The name of the source; must not be <code>null</code>.
	 * @return A source provider which provides the request source, or
	 *         <code>null</code> if no such source exists.
	 * @see ISources
	 */
	public ISourceProvider getSourceProvider(final String sourceName);

	/**
	 * Retrieves all of the source providers registered with this service.
	 * 
	 * @return The source providers registered with this service. This value is
	 *         never <code>null</code>, but may be empty.
	 */
	public ISourceProvider[] getSourceProviders();

}
