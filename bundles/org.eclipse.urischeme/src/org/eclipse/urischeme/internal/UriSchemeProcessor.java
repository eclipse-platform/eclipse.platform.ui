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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.urischeme.IUriSchemeHandler;
import org.eclipse.urischeme.IUriSchemeProcessor;

/**
 * Impelementation of the API to process URI scheme handling as defined in
 * extension point <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
 *
 */
public class UriSchemeProcessor implements IUriSchemeProcessor {

	private static final String PLUGIN_ID = "org.eclipse.urischeme"; //$NON-NLS-1$

	/**
	 * Id of the extension point for uri scheme handlers
	 */
	private static final String EXT_POINT_ID_URI_SCHEME_HANDLERS = "org.eclipse.urischeme.uriSchemeHandlers"; //$NON-NLS-1$
	/**
	 * Attribute "uriScheme" of an registered uri scheme handler
	 */
	private static final String EXT_POINT_ATTRIBUTE_URI_SCHEME = "uriScheme"; //$NON-NLS-1$
	/**
	 * Attribute "class" of an registered uri scheme handler
	 */
	private static final String EXT_POINT_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	private IConfigurationElement[] configurationElements = null;
	private Map<String, IUriSchemeHandler> createdHandlers = new HashMap<>();

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
			handler = getHandlerFromExtensionPoint(uriScheme);
			createdHandlers.put(uriScheme, handler);
		}
		if (handler != null) {
			handler.handle(uri);
		}
	}

	private IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) throws CoreException {
		IConfigurationElement[] elements = getOrReadConfigurationElements();

		for (IConfigurationElement element : elements) {
			if (uriScheme.equals(element.getAttribute(EXT_POINT_ATTRIBUTE_URI_SCHEME))) {
				return createExecutableSchemeHandler(element);
			}
		}
		return null;
	}

	private IConfigurationElement[] getOrReadConfigurationElements() {
		if (this.configurationElements == null) {
			IExtensionRegistry registry = RegistryFactory.getRegistry();
			this.configurationElements = registry.getConfigurationElementsFor(EXT_POINT_ID_URI_SCHEME_HANDLERS);
		}
		return configurationElements;
	}

	private IUriSchemeHandler createExecutableSchemeHandler(IConfigurationElement element) throws CoreException {
		Object executableExtension = element.createExecutableExtension(EXT_POINT_ATTRIBUTE_CLASS);
		if (executableExtension instanceof IUriSchemeHandler) {
			return (IUriSchemeHandler) executableExtension;
		}
		throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
				"Registered class has wrong type: " + executableExtension.getClass())); //$NON-NLS-1$
	}
}