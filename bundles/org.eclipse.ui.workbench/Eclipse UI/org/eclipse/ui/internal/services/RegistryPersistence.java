/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.services.IDisposable;

/**
 * <p>
 * A manager for items parsed from the registry. This attaches a listener to the
 * registry after the first read, and monitors the registry for changes from
 * that point on. When {@link #dispose()} is called, the listener is detached.
 * </p>
 * <p>
 * This class is only intended for internal use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public abstract class RegistryPersistence implements IDisposable,
		IWorkbenchRegistryConstants {

	/**
	 * The expression to return when there is an error. Never <code>null</code>.
	 */
	protected static final Expression ERROR_EXPRESSION = new Expression() {
		public final EvaluationResult evaluate(final IEvaluationContext context) {
			return null;
		}
	};

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
	 * @param element
	 *            The element from which the warning originates; may be
	 *            <code>null</code>.
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message, final IConfigurationElement element) {
		addWarning(warningsToLog, message, element, null, null, null);
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
	 * @param element
	 *            The element from which the warning originates; may be
	 *            <code>null</code>.
	 * @param id
	 *            The identifier of the item for which a warning is being
	 *            logged; may be <code>null</code>.
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message, final IConfigurationElement element,
			final String id) {
		addWarning(warningsToLog, message, element, id, null, null);
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
	 * @param element
	 *            The element from which the warning originates; may be
	 *            <code>null</code>.
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
			final String message, final IConfigurationElement element,
			final String id, final String extraAttributeName,
			final String extraAttributeValue) {
		String statusMessage = message;
		if (element != null) {
			statusMessage = statusMessage
					+ ": plug-in='" + element.getNamespace() + '\''; //$NON-NLS-1$
		}
		if (id != null) {
			if (element != null) {
				statusMessage = statusMessage + ',';
			} else {
				statusMessage = statusMessage + ':';
			}
			statusMessage = statusMessage + " id='" + id + '\''; //$NON-NLS-1$
		}
		if (extraAttributeName != null) {
			if ((element != null) || (id != null)) {
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
	protected static final boolean checkClass(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final String message, final String id) {
		// Check to see if we have a handler class.
		if ((configurationElement.getAttribute(ATT_CLASS) == null)
				&& (configurationElement.getChildren(TAG_CLASS).length == 0)) {
			addWarning(warningsToLog, message, configurationElement, id);
			return false;
		}

		return true;
	}

	/**
	 * Checks to see whether the configuration element represents a pulldown
	 * action. This involves reading the <code>style</code> and
	 * <code>pulldown</code> attributes.
	 * 
	 * @param element
	 *            The element to check; must not be <code>null</code>.
	 * @return <code>true</code> if the element is a pulldown action;
	 *         <code>false</code> otherwise.
	 */
	protected static final boolean isPulldown(
			final IConfigurationElement element) {
		final String style = readOptional(element, ATT_STYLE);
		final boolean pulldown = readBoolean(element, ATT_PULLDOWN, false);
		return (pulldown || STYLE_PULLDOWN.equals(style));
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
	protected static final boolean readBoolean(
			final IConfigurationElement configurationElement,
			final String attribute, final boolean defaultValue) {
		final String value = configurationElement.getAttribute(attribute);
		if (value == null) {
			return defaultValue;
		}

		if (defaultValue) {
			return !value.equalsIgnoreCase("false"); //$NON-NLS-1$
		}

		return value.equalsIgnoreCase("true"); //$NON-NLS-1$
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
	protected static final String readOptional(
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
	protected static final ParameterizedCommand readParameterizedCommand(
			final IConfigurationElement configurationElement,
			final ICommandService commandService, final List warningsToLog,
			final String message, final String id) {
		final String commandId = readRequired(configurationElement,
				ATT_COMMAND_ID, warningsToLog, message, id);
		if (commandId == null) {
			return null;
		}

		final Command command = commandService.getCommand(commandId);
		final ParameterizedCommand parameterizedCommand = readParameters(
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
	protected static final ParameterizedCommand readParameters(
			final IConfigurationElement configurationElement,
			final List warningsToLog, final Command command) {
		final IConfigurationElement[] parameterElements = configurationElement
				.getChildren(TAG_PARAMETER);
		if ((parameterElements == null) || (parameterElements.length == 0)) {
			return new ParameterizedCommand(command, null);
		}

		final Collection parameters = new ArrayList();
		for (int i = 0; i < parameterElements.length; i++) {
			final IConfigurationElement parameterElement = parameterElements[i];

			// Read out the id.
			final String id = parameterElement.getAttribute(ATT_ID);
			if ((id == null) || (id.length() == 0)) {
				// The name should never be null. This is invalid.
				addWarning(warningsToLog, "Parameters need a name", //$NON-NLS-1$
						configurationElement);
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
				addWarning(warningsToLog,
						"Could not find a matching parameter", //$NON-NLS-1$
						configurationElement, id);
				continue;
			}

			// Read out the value.
			final String value = parameterElement.getAttribute(ATT_VALUE);
			if ((value == null) || (value.length() == 0)) {
				// The name should never be null. This is invalid.
				addWarning(warningsToLog, "Parameters need a value", //$NON-NLS-1$
						configurationElement, id);
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
	protected static final String readRequired(
			final IConfigurationElement configurationElement,
			final String attribute, final List warningsToLog,
			final String message) {
		return readRequired(configurationElement, attribute, warningsToLog,
				message, null);
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
	protected static final String readRequired(
			final IConfigurationElement configurationElement,
			final String attribute, final List warningsToLog,
			final String message, final String id) {
		final String value = configurationElement.getAttribute(attribute);
		if ((value == null) || (value.length() == 0)) {
			addWarning(warningsToLog, message, configurationElement, id);
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
	 * @param whenElementName
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
	protected static final Expression readWhenElement(
			final IConfigurationElement parentElement,
			final String whenElementName, final String id,
			final List warningsToLog) {
		// Check to see if we have an when expression.
		final IConfigurationElement[] whenElements = parentElement
				.getChildren(whenElementName);
		Expression whenExpression = null;
		if (whenElements.length > 0) {
			// Check if we have too many when elements.
			if (whenElements.length > 1) {
				// There should only be one when element
				addWarning(warningsToLog,
						"There should only be one when element", parentElement, //$NON-NLS-1$
						id, "whenElementName", //$NON-NLS-1$
						whenElementName);
				return ERROR_EXPRESSION;
			}

			final IConfigurationElement whenElement = whenElements[0];
			final IConfigurationElement[] expressionElements = whenElement
					.getChildren();
			if (expressionElements.length > 0) {
				// Check if we have too many expression elements
				if (expressionElements.length > 1) {
					// There should only be one expression element
					addWarning(
							warningsToLog,
							"There should only be one expression element", parentElement, //$NON-NLS-1$
							id, "whenElementName", //$NON-NLS-1$
							whenElementName);
					return ERROR_EXPRESSION;
				}

				// Convert the activeWhen element into an expression.
				final ElementHandler elementHandler = ElementHandler
						.getDefault();
				final ExpressionConverter converter = ExpressionConverter
						.getDefault();
				final IConfigurationElement expressionElement = expressionElements[0];
				try {
					whenExpression = elementHandler.create(converter,
							expressionElement);
				} catch (final CoreException e) {
					// There when expression could not be created.
					addWarning(
							warningsToLog,
							"Problem creating when element", //$NON-NLS-1$
							parentElement, id,
							"whenElementName", whenElementName); //$NON-NLS-1$
					return ERROR_EXPRESSION;
				}
			}
		}

		return whenExpression;
	}

	/**
	 * The registry change listener for this class.
	 */
	private final IRegistryChangeListener registryChangeListener;

	/**
	 * Whether the preference and registry change listeners have been attached
	 * yet.
	 */
	protected boolean registryListenerAttached = false;

	/**
	 * Constructs a new instance of {@link RegistryPersistence}. A registry
	 * change listener is created.
	 */
	protected RegistryPersistence() {
		registryChangeListener = new IRegistryChangeListener() {
			public final void registryChanged(final IRegistryChangeEvent event) {
				if (isChangeImportant(event)) {
					Display.getDefault().asyncExec(new Runnable() {
						public final void run() {
							read();
						}
					});
				}
			}
		};
	}

	/**
	 * Detaches the registry change listener from the registry.
	 */
	public void dispose() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.removeRegistryChangeListener(registryChangeListener);
		registryListenerAttached = false;
	}

	/**
	 * Checks whether the registry change could affect this persistence class.
	 * 
	 * @param event
	 *            The event indicating the registry change; must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the persistence instance is affected by
	 *         this change; <code>false</code> otherwise.
	 */
	protected abstract boolean isChangeImportant(
			final IRegistryChangeEvent event);

	/**
	 * Reads the various elements from the registry. Subclasses should extend,
	 * but must not override.
	 */
	protected void read() {
		if (!registryListenerAttached) {
			final IExtensionRegistry registry = Platform.getExtensionRegistry();
			registry.addRegistryChangeListener(registryChangeListener);
			registryListenerAttached = true;
		}
	}
}
