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
package org.eclipse.ui.internal.presentation;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The <code>ColorDefinitionReader</code> reads the color definitions from the 
 * plugin registry.
 * 
 * @since 3.0
 */
public class PresentationDefinitionReader extends RegistryReader {
	public static String ATT_ID = "id"; //$NON-NLS-1$
	public static String ATT_CLASS = "class"; //$NON-NLS-1$
	public static String ATT_LABEL = "label"; //$NON-NLS-1$
	public static String TAG_PRESENTATIONCATEGORY = "presentationCategory"; //$NON-NLS-1$

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	//private final static ResourceBundle RESOURCE_BUNDLE =
	//	ResourceBundle.getBundle(PresentationDefinitionReader.class.getName());

	private Collection categories = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {

	    if (element.getName().equals(TAG_PRESENTATIONCATEGORY)) {
			String name = element.getAttribute(ATT_LABEL);
			
			String id = element.getAttribute(ATT_ID);
		
			String description = null;
	
			IConfigurationElement[] descriptions =
				element.getChildren(TAG_DESCRIPTION);
	
			if (descriptions.length > 0)
				description = descriptions[0].getValue();
	
			categories.add(
				new PresentationCategory(
					name,
					id,
					description,
					element
						.getDeclaringExtension()
						.getDeclaringPluginDescriptor()
						.getUniqueIdentifier(),
					element));
	        
	        return true;
	    }
	    return false;
	}

	/**
	 * Read the color extensions within a registry.
	 * 
	 * @param registry the <code>IPluginRegistry</code> to read from.
	 */
	void readRegistry(IPluginRegistry in) {
		readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PRESENTATION);
	}	
	
	public Collection getCategories() {
	    return categories;
	}
}
