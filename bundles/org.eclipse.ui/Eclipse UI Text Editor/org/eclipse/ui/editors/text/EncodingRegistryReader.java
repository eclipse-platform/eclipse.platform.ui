/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

/**
 * The EncodingRegistryReader is the class that read all of the encoding
 * defintions from the plugin registry.
 */

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

class EncodingRegistryReader extends RegistryReader {

	

	//The registry values are the ones read from the registry
	static List definitions;            

	private static String EXTENSION_ID = "fileEncodings"; //$NON-NLS-1$
	private static String ATT_LOCALE = "locale"; //$NON-NLS-1$
	private static String ATT_VALUE = "value"; //$NON-NLS-1$
	private static String ATT_ID = "id"; //$NON-NLS-1$
	private static String ATT_HELP_CONTEXT_ID = "helpContextId"; //$NON-NLS-1$
	private static String ATT_LABEL = "label"; //$NON-NLS-1$
	private static String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static String ATT_TOOLTIP = "tooltip"; //$NON-NLS-1$

	/**
	 * Constructor for EncodingRegistryReader.
	 */
	protected EncodingRegistryReader() {
		super();
	}

	/*
	 * @see RegistryReader#readElement(IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
	
		String localeString = element.getAttribute(ATT_LOCALE);
		
		if(localeString != null){
			
			boolean noMatch = true;
			Locale currentLocale = Locale.getDefault();
			if(currentLocale.getLanguage().equals(localeString))
				noMatch = false;
			if(currentLocale.toString().equals(localeString))
				noMatch = false;
			//If the locale is specified but does not match the 
			//current one leave.
			if(noMatch)
				return true;
		}

		String value = element.getAttribute(ATT_VALUE);
		String id = element.getAttribute(ATT_ID);
		String label = element.getAttribute(ATT_LABEL);

		EncodingDefinition definition = 
			new EncodingDefinition(id,label,value);
			
		String helpID = element.getAttribute(ATT_HELP_CONTEXT_ID);
		if(helpID != null)
			definition.setHelpContextId(helpID);
			
		String description = element.getAttribute(ATT_DESCRIPTION);
		if(helpID != null)
			definition.setDescription(description);
			
		String toolTip = element.getAttribute(ATT_TOOLTIP);
		if(toolTip != null)
			definition.setHelpContextId(toolTip);
		
		definitions.add(definition);

		return true;

	}

	/**
	 * Read the decorator extensions within a registry and set 
	 * up the registry values.
	 */
	List readRegistry(IPluginRegistry in) {
		definitions = new ArrayList();
		readRegistry(in, IWorkbenchConstants.PLUGIN_ID, EXTENSION_ID);
		return definitions;
	}



}
