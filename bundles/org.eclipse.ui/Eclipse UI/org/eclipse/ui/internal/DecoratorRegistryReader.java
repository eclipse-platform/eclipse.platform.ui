package org.eclipse.ui.internal;

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

	private static String EXTENSION_ID = "decorators";
	private static String ATT_OBJECT_CLASS = "objectClass";
	private static String ATT_NAME = "name";
	private static String ATT_ENABLED = "state";
	private static String P_TRUE = "true";

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

			String name = element.getAttribute(ATT_NAME);

			boolean enabled = P_TRUE.equals(element.getAttribute(ATT_NAME));

			values.add(
				new DecoratorDefinition(
					name,
					className,
					enabled,
					(ILabelDecorator) contributor));
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
	Collection readRegistry(IPluginRegistry in) {
		values = new ArrayList();
		readRegistry(in, IWorkbenchConstants.PLUGIN_ID, EXTENSION_ID);
		return values;
	}

}