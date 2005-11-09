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
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.2
 */
public abstract class CommonCommandPersistence {

	/**
	 * The name of the class attribute, which appears on executable extensions.
	 */
	protected static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the command id.
	 */
	protected static final String ATTRIBUTE_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the description attribute, which appears on named handle
	 * objects.
	 */
	protected static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the disabled icon for a command image.
	 */
	protected static final String ATTRIBUTE_DISABLED_ICON = "disabledIcon"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the hover icon for a command image.
	 */
	protected static final String ATTRIBUTE_HOVER_ICON = "hoverIcon"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the default icon for a command image.
	 */
	protected static final String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$

	/**
	 * The name of the id attribute, which is used on handle objects.
	 */
	protected static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the label attribute, which appears on menus.
	 */
	protected static final String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$

	/**
	 * The name of the name attribute, which appears on named handle objects
	 */
	protected static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent attribute, which appears on scheme
	 * definitions.
	 */
	protected static final String ATTRIBUTE_PARENT = "parent"; //$NON-NLS-1$

	/**
	 * The name of the parent id attribute, which appears on scheme definitions.
	 */
	protected static final String ATTRIBUTE_PARENT_ID = "parentId"; //$NON-NLS-1$

	/**
	 * The name of the value attributed, used in several places.
	 */
	protected static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	/**
	 * The name of the visible attribute, which appears on action set elements.
	 */
	protected static final String ATTRIBUTE_VISIBLE = "visible"; //$NON-NLS-1$

	/**
	 * The name of the element storing an action set.
	 */
	protected static final String ELEMENT_ACTION_SET = "actionSet"; //$NON-NLS-1$

	/**
	 * The name of the class element, which appears on an executable extension.
	 */
	protected static final String ELEMENT_CLASS = ATTRIBUTE_CLASS;

	/**
	 * The name of the element storing a command.
	 */
	protected static final String ELEMENT_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	protected static final String ELEMENT_PARAMETER = "parameter"; //$NON-NLS-1$

	/**
	 * The name of the commands extension point, and the name of the key for the
	 * commands preferences.
	 */
	protected static final String EXTENSION_COMMANDS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_COMMANDS;

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
	protected static final void addElementToIndexedArray(
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
	 * Adds a warning to be logged at some later point in time.
	 * 
	 * @param warningsToLog
	 *            The collection of warnings to be logged; must not be
	 *            <code>null</code>.
	 * @param message
	 *            The mesaage to log; must not be <code>null</code>.
	 * @param pluginId
	 *            The identifier of the plug-in from which the warning
	 *            originates; may be <code>null</code>.
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message, final String pluginId) {
		addWarning(warningsToLog, message, pluginId, null, null, null);
	}

	/**
	 * Adds a warning to be logged at some later point in time. This logs the
	 * identifier of the item.
	 * 
	 * @param warningsToLog
	 *            The collection of warnings to be logged; must not be
	 *            <code>null</code>.
	 * @param message
	 *            The mesaage to log; must not be <code>null</code>.
	 * @param pluginId
	 *            The identifier of the plug-in from which the warning
	 *            originates; may be <code>null</code>.
	 * @param id
	 *            The identifier of the item for which a warning is being
	 *            logged; may be <code>null</code>.
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message, final String pluginId, final String id) {
		addWarning(warningsToLog, message, pluginId, id, null, null);
	}

	/**
	 * Adds a warning to be logged at some later point in time. This logs the
	 * identifier of the item, as well as an extra attribute.
	 * 
	 * @param warningsToLog
	 *            The collection of warnings to be logged; must not be
	 *            <code>null</code>.
	 * @param message
	 *            The mesaage to log; must not be <code>null</code>.
	 * @param pluginId
	 *            The identifier of the plug-in from which the warning
	 *            originates; may be <code>null</code>.
	 * @param id
	 *            The identifier of the item for which a warning is being
	 *            logged; may be <code>null</code>.
	 * @param extraAttributeName
	 *            The name of extra attribute to be logged; may be
	 *            <code>null</code>.
	 * @param extraAttributeValue
	 *            The value of the extra attribute to be logged; may be
	 *            <code>null</code>.
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message, final String pluginId, final String id,
			final String extraAttributeName, final String extraAttributeValue) {
		String statusMessage = message;
		if (pluginId != null) {
			statusMessage = statusMessage + ": plug-in='" + pluginId + '\''; //$NON-NLS-1$
		}
		if (id != null) {
			if (pluginId != null) {
				statusMessage = statusMessage + ',';
			} else {
				statusMessage = statusMessage + ':';
			}
			statusMessage = statusMessage + " id='" + id + '\''; //$NON-NLS-1$
		}
		if (extraAttributeName != null) {
			if ((pluginId != null) || (id != null)) {
				statusMessage = statusMessage + ',';
			} else {
				statusMessage = statusMessage + ':';
			}
			statusMessage = statusMessage + ' ' + extraAttributeName + "='" //$NON-NLS-1$
					+ extraAttributeValue + '\'';
		}

		final IStatus status = new Status(IStatus.WARNING,
				WorkbenchPlugin.PI_WORKBENCH, 0, statusMessage, null);
		warningsToLog.add(status);
	}

	/**
	 * Checks that the class attribute or element exists for this element. This
	 * is used for executable extensions that are being read in.
	 * 
	 * @param configurationElement
	 *            The configuration element which should contain a class
	 *            attribute or a class child element; must not be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings to be logged; never <code>null</code>.
	 * @param message
	 *            The message to log if something goes wrong; may be
	 *            <code>null</code>.
	 * @param id
	 *            The identifier of the handle object; may be <code>null</code>.
	 * @return <code>true</code> if the class attribute or element exists;
	 *         <code>false</code> otherwise.
	 */
	protected static final boolean checkClassFromRegistry(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final String message, final String id) {
		// Check to see if we have a handler class.
		if ((configurationElement.getAttribute(ATTRIBUTE_CLASS) == null)
				&& (configurationElement.getChildren(ELEMENT_CLASS).length == 0)) {
			addWarning(warningsToLog, message, configurationElement
					.getNamespace(), id);
			return false;
		}

		return true;
	}

	/**
	 * Logs any warnings in <code>warningsToLog</code>.
	 * 
	 * @param warningsToLog
	 *            The warnings to log; may be <code>null</code>.
	 * @param message
	 *            The message to include in the log entry; must not be
	 *            <code>null</code>.
	 */
	protected static final void logWarnings(final List warningsToLog,
			final String message) {
		// If there were any warnings, then log them now.
		if ((warningsToLog != null) && (!warningsToLog.isEmpty())) {
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}

	/**
	 * Reads a boolean attribute from an element.
	 * 
	 * @param configurationElement
	 *            The configuration element from which to read the attribute;
	 *            must not be <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @param defaultValue
	 *            The default boolean value.
	 * @return The attribute's value; may be <code>null</code> if none.
	 */
	protected static final boolean readBooleanFromRegistry(
			final IConfigurationElement configurationElement,
			final String attribute, final boolean defaultValue) {
		final String value = configurationElement.getAttribute(attribute);
		if (value == null) {
			return defaultValue;
		}

		if (defaultValue) {
			return !value.equalsIgnoreCase("false"); //$NON-NLS-1$
		}

		return !value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	/**
	 * Reads an optional attribute from an element. This converts zero-length
	 * strings into <code>null</code>.
	 * 
	 * @param configurationElement
	 *            The configuration element from which to read the attribute;
	 *            must not be <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @return The attribute's value; may be <code>null</code> if none.
	 */
	protected static final String readOptionalFromRegistry(
			final IConfigurationElement configurationElement,
			final String attribute) {
		String value = configurationElement.getAttribute(attribute);
		if ((value != null) && (value.length() == 0)) {
			value = null;
		}

		return value;
	}

	/**
	 * Reads the parameterized command from a parent configuration element. This
	 * is used to read the parameter sub-elements from a key element, as well as
	 * the command id. Each parameter is guaranteed to be valid. If invalid
	 * parameters are found, then a warning status will be appended to the
	 * <code>warningsToLog</code> list. The command id is required, or a
	 * warning will be logged.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the parameters should be
	 *            read; must not be <code>null</code>.
	 * @param commandService
	 *            The service providing commands for the workbench; must not be
	 *            <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings found during parsing. Warnings found will
	 *            parsing the parameters will be appended to this list. This
	 *            value must not be <code>null</code>.
	 * @param message
	 *            The message to print if the command identifier is not present;
	 *            must not be <code>null</code>.
	 * @return The array of parameters found for this configuration element;
	 *         <code>null</code> if none can be found.
	 */
	protected static final ParameterizedCommand readParameterizedCommandFromRegistry(
			final IConfigurationElement configurationElement,
			final ICommandService commandService, final List warningsToLog,
			final String message, final String id) {
		final String commandId = readRequiredFromRegistry(configurationElement,
				ATTRIBUTE_COMMAND_ID, warningsToLog, message, id);
		if (commandId == null) {
			return null;
		}

		final Command command = commandService.getCommand(commandId);
		final ParameterizedCommand parameterizedCommand = readParametersFromRegistry(
				configurationElement, warningsToLog, command);

		return parameterizedCommand;
	}

	/**
	 * Reads the parameters from a parent configuration element. This is used to
	 * read the parameter sub-elements from a key element. Each parameter is
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
	 * @param command
	 *            The command around which the parameterization should be
	 *            created; must not be <code>null</code>.
	 * @return The array of parameters found for this configuration element;
	 *         <code>null</code> if none can be found.
	 */
	protected static final ParameterizedCommand readParametersFromRegistry(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final Command command) {
		final IConfigurationElement[] parameterElements = configurationElement
				.getChildren(ELEMENT_PARAMETER);
		if ((parameterElements == null) || (parameterElements.length == 0)) {
			return new ParameterizedCommand(command, null);
		}

		final Collection parameters = new ArrayList();
		for (int i = 0; i < parameterElements.length; i++) {
			final IConfigurationElement parameterElement = parameterElements[i];

			// Read out the id.
			final String id = parameterElement.getAttribute(ATTRIBUTE_ID);
			if ((id == null) || (id.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a name: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Find the parameter on the command.
			IParameter parameter = null;
			try {
				final IParameter[] commandParameters = command.getParameters();
				if (parameters != null) {
					for (int j = 0; j < commandParameters.length; j++) {
						final IParameter currentParameter = commandParameters[j];
						if (Util.equals(currentParameter.getId(), id)) {
							parameter = currentParameter;
							break;
						}
					}

				}
			} catch (final NotDefinedException e) {
				// This should not happen.
			}
			if (parameter == null) {
				// The name should never be null. This is invalid.
				final String message = "Could not find a matching parameter: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace()
						+ "', parameterId='" + id //$NON-NLS-1$
						+ "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the value.
			final String value = parameterElement.getAttribute(ATTRIBUTE_VALUE);
			if ((value == null) || (value.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Parameters need a value: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace()
						+ "', parameterId='" //$NON-NLS-1$
						+ id + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			parameters.add(new Parameterization(parameter, value));
		}

		if (parameters.isEmpty()) {
			return new ParameterizedCommand(command, null);
		}

		return new ParameterizedCommand(command,
				(Parameterization[]) parameters
						.toArray(new Parameterization[parameters.size()]));
	}

	/**
	 * Reads a required attribute from the configuration element.
	 * 
	 * @param configurationElement
	 *            The configuration element from which to read; must not be
	 *            <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings; must not be <code>null</code>.
	 * @param message
	 *            The warning message to use if the attribute is missing; must
	 *            not be <code>null</code>.
	 * @return The required attribute; may be <code>null</code> if missing.
	 */
	protected static final String readRequiredFromRegistry(
			final IConfigurationElement configurationElement,
			final String attribute, final List warningsToLog,
			final String message) {
		return readRequiredFromRegistry(configurationElement, attribute,
				warningsToLog, message, null);
	}

	/**
	 * Reads a required attribute from the configuration element. This logs the
	 * identifier of the item if this required element cannot be found.
	 * 
	 * @param configurationElement
	 *            The configuration element from which to read; must not be
	 *            <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings; must not be <code>null</code>.
	 * @param message
	 *            The warning message to use if the attribute is missing; must
	 *            not be <code>null</code>.
	 * @param id
	 *            The identifier of the element for which this is a required
	 *            attribute; may be <code>null</code>.
	 * @return The required attribute; may be <code>null</code> if missing.
	 */
	protected static final String readRequiredFromRegistry(
			final IConfigurationElement configurationElement,
			final String attribute, final List warningsToLog,
			final String message, final String id) {
		final String value = configurationElement.getAttribute(attribute);
		if ((value == null) || (value.length() == 0)) {
			addWarning(warningsToLog, message, configurationElement
					.getNamespace(), id);
			return null;
		}

		return value;
	}

	/**
	 * Reads a <code>when</code> child element from the given configuration
	 * element. Warnings will be appended to <code>warningsToLog</code>.
	 * 
	 * @param parentElement
	 *            The configuration element which might have a <code>when</code>
	 *            element as a child; never <code>null</code>.
	 * @param whenElement
	 *            The name of the when element to find; never <code>null</code>.
	 * @param id
	 *            The identifier of the menu element whose <code>when</code>
	 *            expression is being read; never <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings while parsing the extension point; never
	 *            <code>null</code>.
	 * @return The <code>when</code> expression for the
	 *         <code>configurationElement</code>, if any; otherwise,
	 *         <code>null</code>.
	 */
	protected static final Expression readWhenElementFromRegistry(
			final IConfigurationElement parentElement,
			final String whenElement, final String id, final List warningsToLog) {
		// Check to see if we have an visibleWhen expression.
		final IConfigurationElement[] whenElements = parentElement
				.getChildren(whenElement);
		Expression whenExpression = null;
		if (whenElements.length > 0) {
			// Check if we have too many visible when elements.
			if (whenElements.length > 1) {
				// There should only be one visibleWhen element
				addWarning(warningsToLog,
						"There should only be one when element", //$NON-NLS-1$
						parentElement.getNamespace(), id, "whenElement", //$NON-NLS-1$
						whenElement);
				return null;
			}

			// Convert the activeWhen element into an expression.
			final ElementHandler elementHandler = ElementHandler.getDefault();
			final ExpressionConverter converter = ExpressionConverter
					.getDefault();
			try {
				whenExpression = elementHandler.create(converter,
						whenElements[0]);
			} catch (final CoreException e) {
				// There activeWhen expression could not be created.
				addWarning(warningsToLog, "Problem creating when element", //$NON-NLS-1$
						parentElement.getNamespace(), id, "whenElement", //$NON-NLS-1$
						whenElement);
			}
		}

		return whenExpression;
	}

	/**
	 * Whether the preference and registry change listeners have been attached
	 * yet.
	 */
	protected boolean listenersAttached = false;

	/**
	 * Constructs a new instance of <code>CommonCommandPersistence</code>.
	 */
	protected CommonCommandPersistence() {
		// Do nothing.
	}
}
