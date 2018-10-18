/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.urischeme;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.urischeme.internal.UriSchemeExtensionReader;

/**
 * API for reading available URI schemes from the extension registry
 *
 */
public interface IUriSchemeExtensionReader {

	/**
	 * @return an instance to read out URI scheme handlers as registered in
	 *         extension point
	 *         <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
	 */
	static IUriSchemeExtensionReader newInstance() {
		return new UriSchemeExtensionReader();
	}

	/**
	 *
	 * @return The list of available URI schemes
	 */
	Collection<IScheme> getSchemes();

	/**
	 * Creates the handler for a given URI scheme as registered in extension point
	 * <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
	 *
	 * @param uriScheme The URI scheme
	 * @return The handler implementation for the given URI scheme
	 * @throws CoreException
	 */
	IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) throws CoreException;

}
