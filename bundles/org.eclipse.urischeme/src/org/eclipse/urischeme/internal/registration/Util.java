/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("javadoc")
public class Util {
	/**
	 * Checks if the given uriScheme is a valid uri scheme. Uses the {@link URI}
	 * class for checking.
	 *
	 * @param uriScheme
	 *
	 * @throws IllegalArgumentException if the uri scheme contains invalid
	 *                                  characters
	 *
	 * @see <a href= "https://tools.ietf.org/html/rfc3986#section-3.1">Uniform
	 *      Resource Identifier (URI): Generic Syntax</a>
	 */
	public static void assertUriSchemeIsLegal(String uriScheme) {
		try {
			new URI(uriScheme, "foo", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("'Scheme' does not conform to RFC 2396"); //$NON-NLS-1$
		}
	}
}