package org.eclipse.ui.internal;

import java.util.HashMap;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DecoratorRegistryReader is the class that reads the
 * decorator descriptions from the registry
 */

class DecoratorRegistryReader extends RegistryReader {

	//The registry values are the ones read from the registry
	static HashMap registryValues;

	private static String EXTENSION_ID = "decorators";
	private static String ATT_OBJECT_CLASS = "objectClass";

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

		try {
			Object contributor =
				WorkbenchPlugin.createExtension(element, WizardsRegistryReader.ATT_CLASS);

			String className = element.getAttribute(ATT_OBJECT_CLASS);

			//Prime with the exact match
			registryValues.put(className, contributor);
		} catch (CoreException exception) {
			MessageDialog.openError(
				null,
				WorkbenchMessages.getString("Internal_error"),
				exception.getLocalizedMessage());
			return false;
		}
		
		return true;

	}

	/**
	 * Read the decorator extensions within a registry and set 
	 * up the registry values.
	 */
	void readRegistry(IPluginRegistry in) {
		registryValues = new HashMap();
		readRegistry(in, IWorkbenchConstants.PLUGIN_ID, EXTENSION_ID);
	}

}