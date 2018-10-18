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
package org.eclipse.urischeme.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.IUriSchemeExtensionReader;
import org.eclipse.urischeme.IUriSchemeHandler;

/**
 * Implementation of the API to read available URI schemes from the extension
 * registry as defined in extension point
 * <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
 */
public class UriSchemeExtensionReader implements IUriSchemeExtensionReader {

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
	 * Attribute "uriSchemeDecription" of an registered uri scheme handler
	 *
	 */
	private static final String EXT_POINT_ATTRIBUTE_URI_SCHEME_DESCRIPTION = "uriSchemeDescription"; //$NON-NLS-1$
	/**
	 * Attribute "class" of an registered uri scheme handler
	 */
	private static final String EXT_POINT_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	IConfigurationElement[] configurationElements = null;

	@Override
	public Collection<IScheme> getSchemes() {
		IConfigurationElement[] elements = getOrReadConfigurationElements();
		Collection<IScheme> schemes = new ArrayList<>();
		for (IConfigurationElement element : elements) {
			String schemeName = element.getAttribute(EXT_POINT_ATTRIBUTE_URI_SCHEME);
			String schemeDescription = element.getAttribute(EXT_POINT_ATTRIBUTE_URI_SCHEME_DESCRIPTION);
			IScheme scheme = new Scheme(schemeName, schemeDescription);
			schemes.add(scheme);
		}
		return schemes;
	}


	@Override
	public IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) throws CoreException {
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

	private static class Scheme implements IScheme {

		private String uriScheme;
		private String uriSchemeDescription;

		public Scheme(String uriScheme, String uriSchemeDescription) {
			super();
			this.uriScheme = uriScheme;
			this.uriSchemeDescription = uriSchemeDescription;
		}

		@Override
		public String getName() {
			return uriScheme;
		}

		@Override
		public String getDescription() {
			return uriSchemeDescription;
		}

	}
}
