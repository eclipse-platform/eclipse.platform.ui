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
package org.eclipse.urischeme.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.urischeme.IUriSchemeExtensionReader;
import org.eclipse.urischeme.IUriSchemeHandler;
import org.eclipse.urischeme.IUriSchemeProcessor;

/**
 * Implementation of the API to process URI scheme handling as defined in
 * extension point <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
 *
 */
public class UriSchemeProcessor implements IUriSchemeProcessor {

	private Map<String, IUriSchemeHandler> createdHandlers = new HashMap<>();
	IUriSchemeExtensionReader reader = IUriSchemeExtensionReader.newInstance();

	/**
	 * Call the handler for a given uri scheme. If multiple handlers for the same
	 * uri scheme exists, only the first one is called. Order of handlers (in case
	 * of same scheme) is undefined.
	 *
	 * @param uriScheme just the uri scheme
	 * @param uri       the complete uri
	 * @throws CoreException
	 */
	@Override
	public void handleUri(String uriScheme, String uri) throws CoreException {
		IUriSchemeHandler handler = null;

		if (createdHandlers.containsKey(uriScheme)) {
			handler = createdHandlers.get(uriScheme);
		} else {
			handler = reader.getHandlerFromExtensionPoint(uriScheme);
			createdHandlers.put(uriScheme, handler);
		}
		if (handler != null) {
			handler.handle(uri);
		}
	}

}