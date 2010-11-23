/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.ui.internal.commands.CommandPersistence;
import org.eclipse.ui.internal.services.RegistryPersistence;

/**
 * @since 3.5
 * 
 */
public class CommandToModelProcessor extends RegistryPersistence {

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

	private Map<String, MCategory> categories = new HashMap<String, MCategory>();

	private Map<String, MCommand> commands = new HashMap<String, MCommand>();

	@Execute
	public void read(MApplication application) {
		super.read();
		process(application);
	}

	void process(MApplication application) {
		for (MCategory catModel : application.getCategories()) {
			categories.put(catModel.getElementId(), catModel);
		}

		for (MCommand cmdModel : application.getCommands()) {
			commands.put(cmdModel.getElementId(), cmdModel);
		}

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
				addElementToIndexedArray(configurationElement, indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			} else if (TAG_CATEGORY.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements,
						INDEX_CATEGORY_DEFINITIONS, categoryDefinitionCount++);
			} else if (TAG_COMMAND_PARAMETER_TYPE.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements,
						INDEX_PARAMETER_TYPE_DEFINITIONS, parameterTypeDefinitionCount++);
			}
		}

		final IConfigurationElement[] actionDefinitionsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACTION_DEFINITIONS);
		for (int i = 0; i < actionDefinitionsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = actionDefinitionsExtensionPoint[i];
			final String name = configurationElement.getName();

			if (TAG_ACTION_DEFINITION.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			}
		}

		readCategoriesFromRegistry(indexedConfigurationElements[INDEX_CATEGORY_DEFINITIONS],
				categoryDefinitionCount, application);
		readCommandsFromRegistry(indexedConfigurationElements[INDEX_COMMAND_DEFINITIONS],
				commandDefinitionCount, application);
	}

	/**
	 * @param iConfigurationElements
	 * @param commandDefinitionCount
	 * @param application
	 */
	private void readCommandsFromRegistry(IConfigurationElement[] configurationElements,
			int configurationElementCount, MApplication application) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(configurationElement, ATT_ID, warningsToLog,
					"Commands need an id"); //$NON-NLS-1$
			if (commandId == null) {
				continue;
			}
			if (commands.containsKey(commandId)) {
				continue;
			}
			if (commandId.contains("(")) { //$NON-NLS-1$
				addWarning(warningsToLog, "Invalid command id", configurationElement, commandId); //$NON-NLS-1$
				continue;
			}

			// Read out the name.
			final String name = readRequired(configurationElement, ATT_NAME, warningsToLog,
					"Commands need a name"); //$NON-NLS-1$
			if (name == null) {
				continue;
			}

			// Read out the description.
			final String description = readOptional(configurationElement, ATT_DESCRIPTION);

			// Read out the category id.
			String categoryId = configurationElement.getAttribute(ATT_CATEGORY_ID);
			if ((categoryId == null) || (categoryId.length() == 0)) {
				categoryId = configurationElement.getAttribute(ATT_CATEGORY);
				if ((categoryId != null) && (categoryId.length() == 0)) {
					categoryId = null;
				}
			}

			// Read out the parameters.
			final List<MCommandParameter> parameters = readParameters(configurationElement,
					warningsToLog, application);

			// Read out the returnTypeId.
			final String returnTypeId = readOptional(configurationElement, ATT_RETURN_TYPE_ID);
			if (returnTypeId != null) {
				addWarning(warningsToLog, "Command model has no return type", //$NON-NLS-1$
						configurationElement, commandId, "returnTypeId", returnTypeId); //$NON-NLS-1$
			}

			// Read out the help context identifier.
			final String helpContextId = readOptional(configurationElement, ATT_HELP_CONTEXT_ID);
			if (helpContextId != null) {
				addWarning(warningsToLog, "Command model has no help context id", //$NON-NLS-1$
						configurationElement, commandId, "helpContextId", helpContextId); //$NON-NLS-1$
			}

			final MCategory catModel = categories.get(categoryId);
			if (catModel == null) {
				addWarning(warningsToLog, "Commands should really have a category", //$NON-NLS-1$
						configurationElement, commandId, "categoryId", categoryId); //$NON-NLS-1$
			}

			MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
			command.setElementId(commandId);
			command.setCategory(catModel);
			command.setCommandName(name);
			command.setDescription(description);
			command.getParameters().addAll(parameters);

			application.getCommands().add(command);
			commands.put(command.getElementId(), command);

			// command.define(name, description, category, parameters,
			// returnType, helpContextId);
			// readState(configurationElement, warningsToLog, command);
		}

		// If there were any warnings, then log them now.
		logWarnings(
				warningsToLog,
				"Warnings while parsing the commands from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.actionDefinitions' extension points."); //$NON-NLS-1$

	}

	/**
	 * @param configurationElement
	 * @param warningsToLog
	 * @param application
	 * @return
	 */
	private List<MCommandParameter> readParameters(IConfigurationElement configurationElement,
			List warningsToLog, MApplication application) {
		ArrayList<MCommandParameter> result = new ArrayList<MCommandParameter>();
		final IConfigurationElement[] parameterElements = configurationElement
				.getChildren(TAG_COMMAND_PARAMETER);
		if ((parameterElements == null) || (parameterElements.length == 0)) {
			return result;
		}

		for (int i = 0; i < parameterElements.length; i++) {
			final IConfigurationElement parameterElement = parameterElements[i];
			// Read out the id
			final String id = readRequired(parameterElement, ATT_ID, warningsToLog,
					"Parameters need an id"); //$NON-NLS-1$
			if (id == null) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(parameterElement, ATT_NAME, warningsToLog,
					"Parameters need a name"); //$NON-NLS-1$
			if (name == null) {
				continue;
			}

			/*
			 * The IParameterValues will be initialized lazily as an
			 * IExecutableExtension.
			 */

			// Read out the typeId attribute, if present.
			final String typeId = readOptional(parameterElement, ATT_TYPE_ID);

			// Read out the optional attribute, if present.
			final boolean optional = readBoolean(parameterElement, ATT_OPTIONAL, true);

			MCommandParameter parmModel = CommandsFactoryImpl.eINSTANCE.createCommandParameter();
			parmModel.setElementId(id);
			parmModel.setName(name);
			parmModel.setOptional(optional);
			if (typeId != null) {
				parmModel.setTypeId(typeId);
			}
			result.add(parmModel);
		}

		return result;
	}

	/**
	 * @param iConfigurationElements
	 * @param categoryDefinitionCount
	 * @param application
	 */
	private void readCategoriesFromRegistry(IConfigurationElement[] configurationElements,
			int configurationElementCount, MApplication application) {

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the category identifier.
			final String categoryId = readRequired(configurationElement, ATT_ID, warningsToLog,
					"Categories need an id"); //$NON-NLS-1$
			if (categoryId == null) {
				continue;
			}

			if (categories.containsKey(categoryId)) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(configurationElement, ATT_NAME, warningsToLog,
					"Categories need a name", //$NON-NLS-1$
					categoryId);
			if (name == null) {
				continue;
			}

			// Read out the description.
			final String description = readOptional(configurationElement, ATT_DESCRIPTION);

			MCategory catModel = CommandsFactoryImpl.eINSTANCE.createCategory();
			catModel.setElementId(categoryId);
			catModel.setName(name);
			catModel.setDescription(description);
			application.getCategories().add(catModel);
			categories.put(catModel.getElementId(), catModel);
		}
		// If there were any warnings, then log them now.
		logWarnings(
				warningsToLog,
				"Warnings while parsing the commands from the 'org.eclipse.ui.commands' and 'org.eclipse.ui.actionDefinitions' extension points."); //$NON-NLS-1$

	}

	@Override
	protected boolean isChangeImportant(IRegistryChangeEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
