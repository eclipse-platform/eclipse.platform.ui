package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DecoratorRegistryReader is the class that reads the
 * decorator descriptions from the registry
 */

class DecoratorRegistryReader extends RegistryReader {

	//The registry values are the ones read from the registry
	static Collection values;

	private static String EXTENSION_ID = "decorators"; //$NON-NLS-1$
	private static String ATT_OBJECT_CLASS = "objectClass"; //$NON-NLS-1$
	private static String ATT_LABEL = "label"; //$NON-NLS-1$
	private static String ATT_ADAPTABLE = "adaptable"; //$NON-NLS-1$
	private static String ATT_ID = "id"; //$NON-NLS-1$
	private static String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static String P_TRUE = "true"; //$NON-NLS-1$

	/**
	 * Constructor for DecoratorRegistryReader.
	 */
	protected DecoratorRegistryReader() {
		super();
	}

	/*
	 * @see RegistryReader#readElement(IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {

		String className = element.getAttribute(ATT_OBJECT_CLASS);

		String name = element.getAttribute(ATT_LABEL);

		String id = element.getAttribute(ATT_ID);
		
		String description =  ""; //$NON-NLS-1$
		
		IConfigurationElement[] descriptions = 
			element.getChildren(ATT_DESCRIPTION);
			
		if(descriptions.length > 0)
			description = descriptions[0].getValue();

		boolean adaptable = P_TRUE.equals(element.getAttribute(ATT_ADAPTABLE));

		values.add(
			new DecoratorDefinition(id, name, description, className, adaptable, element));

		return true;

	}

	/**
	 * Read the decorator extensions within a registry and set 
	 * up the registry values.
	 */
	Collection readRegistry(IPluginRegistry in) {
		values = new ArrayList();
		readRegistry(in, IWorkbenchConstants.PLUGIN_ID, EXTENSION_ID);
		return values;
	}

}