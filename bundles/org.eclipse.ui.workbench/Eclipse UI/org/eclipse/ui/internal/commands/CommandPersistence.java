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
import org.eclipse.core.commands.IHandlerState;
import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A static class for accessing the registry and the preference store.
 * </p>
 * 
 * @since 3.1
 */
final class CommandPersistence extends CommonCommandPersistence {

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
	 * The name of the element storing some state.
	 */
	private static final String ELEMENT_STATE = "state"; //$NON-NLS-1$

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
	 * The preference key prefix for all handler state.
	 */
	private static final String PREFERENCE_KEY_PREFIX = "org.eclipse.ui.handlers/state"; //$NON-NLS-1$

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

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the category identifier.
			final String categoryId = readRequired(configurationElement,
					ATTRIBUTE_ID, warningsToLog, "Categories need an id"); //$NON-NLS-1$
			if (categoryId == null) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(configurationElement,
					ATTRIBUTE_NAME, warningsToLog, "Categories need a name", //$NON-NLS-1$
					categoryId);
			if (name == null) {
				continue;
			}

			// Read out the description.
			final String description = readOptional(configurationElement,
					ATTRIBUTE_DESCRIPTION);

			final Category category = commandManager.getCategory(categoryId);
			category.define(name, description);
		}

		// If there were any warnings, then log them now.
		logWarnings(
				warningsToLog,
				"Warnings while parsing the commands from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.actionDefinitions' extension points."); //$NON-NLS-1$
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

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(configurationElement,
					ATTRIBUTE_ID, warningsToLog, "Commands need an id"); //$NON-NLS-1$
			if (commandId == null) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(configurationElement,
					ATTRIBUTE_NAME, warningsToLog, "Commands need a name"); //$NON-NLS-1$
			if (name == null) {
				continue;
			}

			// Read out the description.
			final String description = readOptional(configurationElement,
					ATTRIBUTE_DESCRIPTION);

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
				addWarning(
						warningsToLog,
						"Commands should really have a category", //$NON-NLS-1$
						configurationElement.getNamespace(), commandId,
						"categoryId", categoryId); //$NON-NLS-1$
			}

			command.define(name, description, category, parameters);

			// Read out the state.
			final IHandlerState[] states = readState(configurationElement,
					warningsToLog, commandId);
			if (states != null) {
				for (int j = 0; j < states.length; j++) {
					final IHandlerState state = states[j];
					command.addState(state);
				}
			}
		}

		// If there were any warnings, then log them now.
		logWarnings(
				warningsToLog,
				"Warnings while parsing the commands from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.actionDefinitions' extension points."); //$NON-NLS-1$
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
	 *            The list of warnings found during parsing. Warnings found
	 *            while parsing the parameters will be appended to this list.
	 *            This value must not be <code>null</code>.
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
			final String id = readRequired(parameterElement, ATTRIBUTE_ID,
					warningsToLog, "Parameters need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(parameterElement, ATTRIBUTE_NAME,
					warningsToLog, "Parameters need a name"); //$NON-NLS-1$
			if (name == null) {
				continue;
			}

			/*
			 * The IParameterValues will be initialized lazily as an
			 * IExecutableExtension.
			 */

			// Read out the optional attribute, if present.
			final boolean optional = readBoolean(parameterElement,
					ATTRIBUTE_OPTIONAL, true);

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
	 * Reads the states from a parent configuration element. This is used to
	 * read the state sub-elements from a command element. Each state is
	 * guaranteed to be valid. If invalid states are found, then a warning
	 * status will be appended to the <code>warningsToLog</code> list.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the states should be
	 *            read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings found during parsing. Warnings found
	 *            while parsing the parameters will be appended to this list.
	 *            This value must not be <code>null</code>.
	 * @param commandId
	 *            The identifier of the command for which the state is being
	 *            read; may be <code>null</code>.
	 * @return The array of states found for this configuration element;
	 *         <code>null</code> if none can be found.
	 */
	private static final IHandlerState[] readState(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final String commandId) {
		final IConfigurationElement[] stateElements = configurationElement
				.getChildren(ELEMENT_STATE);
		if ((stateElements == null) || (stateElements.length == 0)) {
			return null;
		}

		int insertionIndex = 0;
		IHandlerState[] states = new IHandlerState[stateElements.length];
		for (int i = 0; i < stateElements.length; i++) {
			final IConfigurationElement stateElement = stateElements[i];

			final String id = readRequired(stateElement, ATTRIBUTE_ID,
					warningsToLog, "State needs an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			if (checkClass(stateElement, warningsToLog,
					"State must have an associated class", id)) { //$NON-NLS-1$
				final String preferenceKey = PREFERENCE_KEY_PREFIX + '/'
						+ commandId + '/' + id;
				final IHandlerState state = new HandlerStateProxy(stateElement,
						ELEMENT_STATE, PrefUtil.getInternalPreferenceStore(),
						preferenceKey);
				states[insertionIndex++] = state;
			}
		}

		if (insertionIndex != states.length) {
			final IHandlerState[] compactedStates = new IHandlerState[insertionIndex];
			System.arraycopy(states, 0, compactedStates, 0, insertionIndex);
			states = compactedStates;
		}

		return states;
	}

	/**
	 * Constructs a new instance of <code>CommandPersistence</code>.
	 */
	CommandPersistence() {
		// Does nothing
	}

	/**
	 * Reads all of the commands and categories from the registry,
	 * 
	 * @param commandManager
	 *            The command manager which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	final void read(final CommandManager commandManager) {
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
}
