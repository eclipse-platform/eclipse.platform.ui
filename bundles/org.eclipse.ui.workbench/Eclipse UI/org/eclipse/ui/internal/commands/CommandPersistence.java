/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * <p>
 * A static class for accessing the registry and the preference store.
 * </p>
 * 
 * @since 3.1
 */
public final class CommandPersistence extends RegistryPersistence {

	/**
	 * The index of the category elements in the indexed array.
	 * 
	 * @see CommandPersistence#read()
	 */
	private static final int INDEX_CATEGORY_DEFINITIONS = 0;

	/**
	 * The index of the command elements in the indexed array.
	 * 
	 * @see CommandPersistence#read()
	 */
	private static final int INDEX_COMMAND_DEFINITIONS = 1;

	/**
	 * The index of the commandParameterType elements in the indexed array.
	 * 
	 * @see CommandPersistence#read()
	 * @since 3.2
	 */
	private static final int INDEX_PARAMETER_TYPE_DEFINITIONS = 2;

	/**
	 * Reads all of the commandParameterType definitions from the commands
	 * extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param commandService
	 *            The command service to which the commands should be added;
	 *            must not be <code>null</code>.
	 * @since 3.2
	 */
	private static final void readParameterTypesFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final ICommandService commandService) {

		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = commandService
				.getDefinedParameterTypes();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the commandParameterType identifier.
			final String parameterTypeId = readRequired(configurationElement,
					ATT_ID, warningsToLog, "Command parameter types need an id"); //$NON-NLS-1$
			if (parameterTypeId == null) {
				continue;
			}

			// Read out the type.
			final String type = readOptional(configurationElement, ATT_TYPE);

			// Read out the converter.
			final String converter = readOptional(configurationElement,
					ATT_CONVERTER);

			/*
			 * if the converter attribute was given, create a proxy
			 * AbstractParameterValueConverter for the ParameterType, otherwise
			 * null indicates there is no converter
			 */
			final AbstractParameterValueConverter parameterValueConverter = (converter == null) ? null
					: new ParameterValueConverterProxy(configurationElement);

			final ParameterType parameterType = commandService
					.getParameterType(parameterTypeId);
			parameterType.define(type, parameterValueConverter);
		}

		// If there were any warnings, then log them now.
		logWarnings(
				warningsToLog,
				"Warnings while parsing the commandParameterTypes from the 'org.eclipse.ui.commands' extension point."); //$NON-NLS-1$

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
	 * @param command
	 *            The command for which the state is being read; may be
	 *            <code>null</code>.
	 */
	static final void readState(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final Command command) {
		final IConfigurationElement[] stateElements = configurationElement
				.getChildren(TAG_STATE);
		if ((stateElements == null) || (stateElements.length == 0)) {
			return;
		}

		// TODO we need state, from somewhere anyway

		for (int i = 0; i < stateElements.length; i++) {
			final IConfigurationElement stateElement = stateElements[i];

			final String id = readRequired(stateElement, ATT_ID, warningsToLog,
					"State needs an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			if (checkClass(stateElement, warningsToLog,
					"State must have an associated class", id)) { //$NON-NLS-1$
				final State state = new CommandStateProxy(stateElement,
						ATT_CLASS, PrefUtil.getInternalPreferenceStore(),
						CommandService.createPreferenceKey(command, id));
				command.addState(id, state);
			}
		}
	}

	/**
	 * The command service with which this persistence class is associated;
	 * never <code>null</code>.
	 */
	private final ICommandService commandService;

	/**
	 * Constructs a new instance of <code>CommandPersistence</code>.
	 * 
	 * @param commandService
	 *            The command service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	CommandPersistence(final ICommandService commandService) {
		if (commandService == null) {
			throw new NullPointerException("The command service cannot be null"); //$NON-NLS-1$
		}
		this.commandService = commandService;
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		return false;
	}

	public boolean commandsNeedUpdating(final IRegistryChangeEvent event) {
		final IExtensionDelta[] commandDeltas = event.getExtensionDeltas(
				PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_COMMANDS);
		if (commandDeltas.length == 0) {
			final IExtensionDelta[] actionDefinitionDeltas = event
					.getExtensionDeltas(PlatformUI.PLUGIN_ID,
							IWorkbenchRegistryConstants.PL_ACTION_DEFINITIONS);
			if (actionDefinitionDeltas.length == 0) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Reads all of the commands and categories from the registry,
	 * 
	 * @param commandService
	 *            The command service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	protected final void read() {
		super.read();
		reRead();
	}
	
	public void reRead() {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int commandDefinitionCount = 0;
		int categoryDefinitionCount = 0;
		int parameterTypeDefinitionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[3][];

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] commandsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (int i = 0; i < commandsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = commandsExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_COMMAND.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			} else if (TAG_CATEGORY.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CATEGORY_DEFINITIONS, categoryDefinitionCount++);
			} else if (TAG_COMMAND_PARAMETER_TYPE.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_PARAMETER_TYPE_DEFINITIONS,
						parameterTypeDefinitionCount++);
			}
		}

		final IConfigurationElement[] actionDefinitionsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACTION_DEFINITIONS);
		for (int i = 0; i < actionDefinitionsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = actionDefinitionsExtensionPoint[i];
			final String name = configurationElement.getName();

			if (TAG_ACTION_DEFINITION.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			}
		}

		readParameterTypesFromRegistry(
				indexedConfigurationElements[INDEX_PARAMETER_TYPE_DEFINITIONS],
				parameterTypeDefinitionCount, commandService);
	}
}
