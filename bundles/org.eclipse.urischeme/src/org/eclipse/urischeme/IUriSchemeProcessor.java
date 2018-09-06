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
package org.eclipse.urischeme;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.urischeme.internal.UriSchemeProcessor;

/**
 * API to process URI scheme handling as defined in extension point
 * <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
 *
 */
public interface IUriSchemeProcessor {
	/**
	 * The singleton instance
	 */
	IUriSchemeProcessor INSTANCE = new UriSchemeProcessor();

	/**
	 * Handle an URI with the given uriScheme
	 *
	 * @param uriScheme the scheme of the URI
	 * @param uri       the complete URI
	 * @throws CoreException
	 */
	void handleUri(String uriScheme, String uri) throws CoreException;
}