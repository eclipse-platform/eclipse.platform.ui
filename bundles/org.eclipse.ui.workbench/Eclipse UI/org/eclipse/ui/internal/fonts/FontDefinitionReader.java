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
package org.eclipse.ui.internal.fonts;


import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The FontDefinitionReader reads the font definitions in the
 * workspace.
 */
public class FontDefinitionReader extends RegistryReader {

	//The registry values are the ones read from the registry
	private Collection values = new ArrayList();

	private static String ATT_LABEL = "label"; //$NON-NLS-1$
	private static String ATT_ID = "id"; //$NON-NLS-1$
	private static String ATT_DEFAULTS_TO = "defaultsTo"; //$NON-NLS-1$
	private static String ATT_CATEGORYID = "categoryId"; //$NON-NLS-1$
	private static String CHILD_DESCRIPTION = "description"; //$NON-NLS-1$
	
	/*
	 * @see RegistryReader#readElement(IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {		
		String name = element.getAttribute(ATT_LABEL);

		String id = element.getAttribute(ATT_ID);

		String defaultMapping = element.getAttribute(ATT_DEFAULTS_TO);

		String categoryId = element.getAttribute(ATT_CATEGORYID);
		
		String description = null;

		IConfigurationElement[] descriptions =
			element.getChildren(CHILD_DESCRIPTION);

		if (descriptions.length > 0)
			description = descriptions[0].getValue();

		values.add(
			new FontDefinition(
				name,
				id,
				defaultMapping,
				categoryId,
				description));

		return true;

	}

	/**
	 * Read the decorator extensions within a registry and set
	 * up the registry values.
	 */
	public Collection readRegistry(IPluginRegistry in) {
		values.clear();
		readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_FONT_DEFINITIONS);
		return getValues();
	}
	
	// for dynamic UI
	public Collection getValues() {
		return values;
	}
}
