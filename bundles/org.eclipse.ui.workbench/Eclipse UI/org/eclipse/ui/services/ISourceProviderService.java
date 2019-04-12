/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.services;

import org.eclipse.ui.ISourceProvider;

/**
 * <p>
 * A service from which all of the source providers can be retrieved.
 * </p>
 * <p>
 * This service can be acquired from your service locator:
 * </p>
 * 
 * <pre>
 * ISourceProviderService service = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
 * </pre>
 * <ul>
 * <li>This service is available globally.</li>
 * </ul>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 3.4
 * @see org.eclipse.ui.services.IEvaluationService
 */
public interface ISourceProviderService {

	/**
	 * Retrieves a source provider providing the given source. This is used by
	 * clients who only need specific sources.
	 *
	 * @param sourceName The name of the source; must not be <code>null</code>.
	 * @return A source provider which provides the request source, or
	 *         <code>null</code> if no such source exists.
	 * @see org.eclipse.ui.ISources
	 */
	ISourceProvider getSourceProvider(final String sourceName);

	/**
	 * Retrieves all of the source providers registered with this service at the
	 * time of this call.
	 * <p>
	 * <code>org.eclipse.ui.services.IEvaluationService</code> can be used to
	 * receive notifications about source variable changes and to evaluate core
	 * expressions against source providers.
	 * </p>
	 *
	 * @return The source providers registered with this service. This value is
	 *         never <code>null</code>, but may be empty.
	 */
	ISourceProvider[] getSourceProviders();

}
