/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.colors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The <code>ColorDefinitionReader</code> reads the color definitions from the 
 * plugin registry.
 * 
 * @since 3.0
 */
class ColorDefinitionReader extends RegistryReader {
	private static String ATT_DEFAULTS_TO = "defaultsTo"; //$NON-NLS-1$
	private static String ATT_ID = "id"; //$NON-NLS-1$
	private static String ATT_LABEL = "label"; //$NON-NLS-1$
	private static String ATT_VALUE = "value"; //$NON-NLS-1$
	private static String CHILD_DESCRIPTION = "description"; //$NON-NLS-1$

	private static String EXTENSION_ID = "colorDefinitions"; //$NON-NLS-1$

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(ColorDefinitionReader.class.getName());

	private Collection values;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {

		String name = element.getAttribute(ATT_LABEL);

		String id = element.getAttribute(ATT_ID);

		String defaultMapping = element.getAttribute(ATT_DEFAULTS_TO);

		String value = element.getAttribute(ATT_VALUE);

		if ((value == null && defaultMapping == null)
			|| (value != null && defaultMapping != null)) {
			logError(element, RESOURCE_BUNDLE.getString("ColorDefinitionReader.badDefault")); //$NON-NLS-1$
			return true;
		}

		String description = null;

		IConfigurationElement[] descriptions =
			element.getChildren(CHILD_DESCRIPTION);

		if (descriptions.length > 0)
			description = descriptions[0].getValue();

		values.add(
			new ColorDefinition(
				name,
				id,
				defaultMapping,
				value,
				description,
				element
					.getDeclaringExtension()
					.getDeclaringPluginDescriptor()
					.getUniqueIdentifier()));

		return true;

	}

	/**
	 * Read the color extensions within a registry.
	 * 
	 * @param registry the <code>IPluginRegistry</code> to read from.
	 */
	Collection readRegistry(IPluginRegistry in) {
		if (values == null)
			values = new ArrayList();
		else
			values.clear();
		readRegistry(in, PlatformUI.PLUGIN_ID, EXTENSION_ID);
		return values;
	}
}
