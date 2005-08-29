/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A static class for accessing the registry and the preference store.
 * </p>
 * 
 * @since 3.1
 */
final class CommandPersistence {

	/**
	 * The name of the category attribute, which appears on a command
	 * definition.
	 */
	private static final String ATTRIBUTE_CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * The name of the category identifier attribute, which appears on a command
	 * definition.
	 */
	private static final String ATTRIBUTE_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

	/**
	 * The name of the description attribute, which appears on a command
	 * definition.
	 */
	private static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The name of the id attribute, which is used on command, category and
	 * parameter definitions.
	 */
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the name attribute, which appears on command, category and
	 * parameter definitions.
	 */
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	/**
	 * The name of the optional attribute, which appears on parameter
	 * definitions.
	 */
	private static final String ATTRIBUTE_OPTIONAL = "optional"; //$NON-NLS-1$

	/**
	 * The name of the element storing an action definition. This element only
	 * existed in
	 */
	private static final String ELEMENT_ACTION_DEFINITION = "actionDefinition"; //$NON-NLS-1$

	/**
	 * The name of the element storing a category.
	 */
	private static final String ELEMENT_CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * The name of the element storing a command.
	 */
	private static final String ELEMENT_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	private static final String ELEMENT_COMMAND_PARAMETER = "commandParameter"; //$NON-NLS-1$

	/**
	 * The name of the action definitions extension point.
	 */
	private static final String EXTENSION_ACTION_DEFINITIONS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_ACTION_DEFINITIONS;

	/**
	 * The name of the commands extension point.
	 */
	private static final String EXTENSION_COMMANDS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_COMMANDS;

	/**
	 * The index of the category elements in the indexed array.
	 * 
	 * @see CommandPersistence#read(CommandManager)
	 */
	private static final int INDEX_CATEGORY_DEFINITIONS = 0;

	/**
	 * The index of the command elements in the indexed array.
	 * 
	 * @see CommandPersistence#read(CommandManager)
	 */
	private static final int INDEX_COMMAND_DEFINITIONS = 1;

	/**
	 * Whether the preference and registry change listeners have been attached
	 * yet.
	 */
    private static boolean listenersAttached = false;

	/**
	 * Inserts the given element into the indexed two-dimensional array in the
	 * array at the index. The array is grown as necessary.
	 * 
	 * @param elementToAdd
	 *            The element to add to the indexed array; may be
	 *            <code>null</code>
	 * @param indexedArray
	 *            The two-dimensional array that is indexed by element type;
	 *            must not be <code>null</code>.
	 * @param index
	 *            The index at which the element should be added; must be a
	 *            valid index.
	 * @param currentCount
	 *            The current number of items in the array at the index.
	 */
	private static final void addElementToIndexedArray(
			final IConfigurationElement elementToAdd,
			final IConfigurationElement[][] indexedArray, final int index,
			final int currentCount) {
		final IConfigurationElement[] elements;
		if (currentCount == 0) {
			elements = new IConfigurationElement[1];
			indexedArray[index] = elements;
		} else {
			if (currentCount >= indexedArray[index].length) {
				final IConfigurationElement[] copy = new IConfigurationElement[indexedArray[index].length * 2];
				System.arraycopy(indexedArray[index], 0, copy, 0, currentCount);
				elements = copy;
				indexedArray[index] = elements;
			} else {
				elements = indexedArray[index];
			}
		}
		elements[currentCount] = elementToAdd;
	}

	/**
	 * Reads all of the commands and categories from the registry,
	 * 
	 * @param commandManager
	 *            The command manager which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	static final void read(final CommandManager commandManager) {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int commandDefinitionCount = 0;
		int categoryDefinitionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[2][];

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] commandsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (int i = 0; i < commandsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = commandsExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (ELEMENT_COMMAND.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			} else if (ELEMENT_CATEGORY.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CATEGORY_DEFINITIONS, categoryDefinitionCount++);
			}
		}

		final IConfigurationElement[] actionDefinitionsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACTION_DEFINITIONS);
		for (int i = 0; i < actionDefinitionsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = actionDefinitionsExtensionPoint[i];
			final String name = configurationElement.getName();

			if (ELEMENT_ACTION_DEFINITION.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			}
		}

		readCategoriesFromCommandsExtensionPoint(
				indexedConfigurationElements[INDEX_CATEGORY_DEFINITIONS],
				categoryDefinitionCount, commandManager);
		readCommandsFromCommandsExtensionPoint(
				indexedConfigurationElements[INDEX_COMMAND_DEFINITIONS],
				commandDefinitionCount, commandManager);
		
        /*
		 * Adds listener so that future registry changes trigger an update of
		 * the command manager automatically.
		 */
		if (!listenersAttached) {
			registry.addRegistryChangeListener(new IRegistryChangeListener() {
				public final void registryChanged(
						final IRegistryChangeEvent event) {
					final IExtensionDelta[] commandDeltas = event
							.getExtensionDeltas(PlatformUI.PLUGIN_ID,
									IWorkbenchConstants.PL_COMMANDS);
					if (commandDeltas.length == 0) {
						final IExtensionDelta[] actionDefinitionDeltas = event
								.getExtensionDeltas(
										PlatformUI.PLUGIN_ID,
										IWorkbenchConstants.PL_ACTION_DEFINITIONS);
						if (actionDefinitionDeltas.length == 0) {
							return;
						}
					}

					/*
					 * At least one of the deltas is non-zero, so re-read all of
					 * the bindings.
					 */
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							read(commandManager);
						}
					});
				}
			}, PlatformUI.PLUGIN_ID);

			listenersAttached = true;
		}
	}

	/**
	 * Reads all of the category definitions from the commands extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param commandManager
	 *            The command manager to which the categories should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readCategoriesFromCommandsExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final CommandManager commandManager) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = commandManager
				.getDefinedCategories();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}
		
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = null;

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String categoryId = configurationElement
					.getAttribute(ATTRIBUTE_ID);
			if ((categoryId == null) || (categoryId.length() == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Categories need an id: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				if (warningsToLog == null) {
					warningsToLog = new ArrayList();
				}
				warningsToLog.add(status);
				continue;
			}

			// Read out the name.
			final String name = configurationElement
					.getAttribute(ATTRIBUTE_NAME);
			if ((name == null) || (name.length() == 0)) {
				// The scheme id should never be null. This is invalid.
				final String message = "Categories need a name: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "', categoryId='" //$NON-NLS-1$
						+ categoryId + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				if (warningsToLog == null) {
					warningsToLog = new ArrayList();
				}
				warningsToLog.add(status);
				continue;
			}

			// Read out the description.
			String description = configurationElement
					.getAttribute(ATTRIBUTE_DESCRIPTION);
			if ((description != null) && (description.length() == 0)) {
				description = null;
			}

			final Category category = commandManager.getCategory(categoryId);
			category.define(name, description);
		}

		// If there were any warnings, then log them now.
		if (warningsToLog != null) {
			final String message = "Warnings while parsing the commands from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.actionDefinitions' extension points."; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}

	/**
	 * Reads all of the command definitions from the commands extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param commandManager
	 *            The command manager to which the commands should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readCommandsFromCommandsExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final CommandManager commandManager) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = commandManager
				.getDefinedCommands();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}
		
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = configurationElement
					.getAttribute(ATTRIBUTE_ID);
			if ((commandId == null) || (commandId.length() == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Commands need an id: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the name.
			final String name = configurationElement
					.getAttribute(ATTRIBUTE_NAME);
			if ((name == null) || (name.length() == 0)) {
				// The scheme id should never be null. This is invalid.
				final String message = "Commands need a name: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "', commandId='" //$NON-NLS-1$
						+ commandId + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the description.
			String description = configurationElement
					.getAttribute(ATTRIBUTE_DESCRIPTION);
			if ((description != null) && (description.length() == 0)) {
				description = null;
			}

			// Read out the category id.
			String categoryId = configurationElement
					.getAttribute(ATTRIBUTE_CATEGORY_ID);
			if ((categoryId == null) || (categoryId.length() == 0)) {
				categoryId = configurationElement
						.getAttribute(ATTRIBUTE_CATEGORY);
				if (categoryId == null) {
					categoryId = Util.ZERO_LENGTH_STRING;
				}
			}

			// Read out the parameters.
			final Parameter[] parameters = readParameters(configurationElement,
					warningsToLog);

			final Command command = commandManager.getCommand(commandId);
			final Category category = commandManager.getCategory(categoryId);
			if (!category.isDefined()) {
				final String message = "Commands should really have a category, not categoryId='" //$NON-NLS-1$
						+ categoryId + "': plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "', commandId='" //$NON-NLS-1$
						+ commandId + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.INFO,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
			}
			command.define(name, description, category, parameters);
		}

		// If there were any warnings, then log them now.
		if (!warningsToLog.isEmpty()) {
			final String message = "Warnings while parsing the commands from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.actionDefinitions' extension points."; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}

	/**
	 * Reads the parameters from a parent configuration element. This is used to
	 * read the parameter sub-elements from a command element. Each parameter is
	 * guaranteed to be valid. If invalid parameters are found, then a warning
	 * status will be appended to the <code>warningsToLog</code> list.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the parameters should be
	 *            read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings found during parsing. Warnings found will
	 *            parsing the parameters will be appended to this list. This
	 *            value must not be <code>null</code>.
	 * @return The array of parameters found for this configuration element;
	 *         <code>null</code> if none can be found.
	 */
	private static final Parameter[] readParameters(
			final IConfigurationElement configurationElement,
			final List warningsToLog) {
		final IConfigurationElement[] parameterElements = configurationElement
				.getChildren(ELEMENT_COMMAND_PARAMETER);
		if ((parameterElements == null) || (parameterElements.length == 0)) {
			return null;
		}

		int insertionIndex = 0;
		Parameter[] parameters = new Parameter[parameterElements.length];
		for (int i = 0; i < parameterElements.length; i++) {
			final IConfigurationElement parameterElement = parameterElements[i];
			// Read out the id
			final String id = parameterElement.getAttribute(ATTRIBUTE_ID);
			if ((id == null) || (id.length() == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Parameters need an id: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the name.
			final String name = parameterElement.getAttribute(ATTRIBUTE_NAME);
			if ((name == null) || (name.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a name: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "', parameterId='" //$NON-NLS-1$
						+ id + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			/*
			 * The IParameterValues will be initialized lazily as an
			 * IExecutableExtension.
			 */

			// Read out the optional attribute, if present.
			final String optionalString = parameterElement
					.getAttribute(ATTRIBUTE_OPTIONAL);
			boolean optional;
			if ((optionalString == null) || (optionalString.length() == 0)) {
				optional = true;
			} else {
				optional = !("false".equalsIgnoreCase(optionalString)); //$NON-NLS-1$
			}

			final Parameter parameter = new Parameter(id, name,
					parameterElement, optional);
			parameters[insertionIndex++] = parameter;
		}

		if (insertionIndex != parameters.length) {
			final Parameter[] compactedParameters = new Parameter[insertionIndex];
			System.arraycopy(parameters, 0, compactedParameters, 0,
					insertionIndex);
			parameters = compactedParameters;
		}

		return parameters;
	}

	/**
	 * This class should not be constructed.
	 */
	private CommandPersistence() {
		// Should not be called.
	}
}
