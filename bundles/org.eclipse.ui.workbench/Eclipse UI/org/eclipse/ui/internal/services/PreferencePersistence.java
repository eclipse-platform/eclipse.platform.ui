/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A manager for items parsed from the preference store. This attaches a
 * listener to the registry after the first read, and monitors the preference
 * for changes from that point on. When {@link #dispose()} is called, the
 * listener is detached.
 * </p>
 * <p>
 * This class is only intended for internal use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public abstract class PreferencePersistence extends RegistryPersistence {

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
			final IMemento elementToAdd, final IMemento[][] indexedArray,
			final int index, final int currentCount) {
		final IMemento[] elements;
		if (currentCount == 0) {
			elements = new IMemento[1];
			indexedArray[index] = elements;
		} else {
			if (currentCount >= indexedArray[index].length) {
				final IMemento[] copy = new IMemento[indexedArray[index].length * 2];
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
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message) {
		addWarning(warningsToLog, message, null, null, null);
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
	 * @param id
	 *            The identifier of the item for which a warning is being
	 *            logged; may be <code>null</code>.
	 */
	protected static final void addWarning(final List warningsToLog,
			final String message, final String id) {
		addWarning(warningsToLog, message, id, null, null);
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
			final String message, final String id,
			final String extraAttributeName, final String extraAttributeValue) {
		String statusMessage = message;
		if (id != null) {
			statusMessage = statusMessage + ": id='" + id + '\''; //$NON-NLS-1$
		}
		if (extraAttributeName != null) {
			if (id != null) {
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
	 * Reads a boolean attribute from a memnto.
	 * 
	 * @param memento
	 *            The memento from which to read the attribute; must not be
	 *            <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @param defaultValue
	 *            The default boolean value.
	 * @return The attribute's value; may be <code>null</code> if none.
	 */
	protected static final boolean readBoolean(final IMemento memento,
			final String attribute, final boolean defaultValue) {
		final String value = memento.getString(attribute);
		if (value == null) {
			return defaultValue;
		}

		if (defaultValue) {
			return !value.equalsIgnoreCase("false"); //$NON-NLS-1$
		}

		return !value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	/**
	 * Reads an optional attribute from a memento. This converts zero-length
	 * strings into <code>null</code>.
	 * 
	 * @param memento
	 *            The memento from which to read the attribute; must not be
	 *            <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @return The attribute's value; may be <code>null</code> if none.
	 */
	protected static final String readOptional(final IMemento memento,
			final String attribute) {
		String value = memento.getString(attribute);
		if ((value != null) && (value.length() == 0)) {
			value = null;
		}

		return value;
	}

	/**
	 * Reads the parameterized command from a parent memento. This is used to
	 * read the parameter sub-elements from a key element, as well as the
	 * command id. Each parameter is guaranteed to be valid. If invalid
	 * parameters are found, then a warning status will be appended to the
	 * <code>warningsToLog</code> list. The command id is required, or a
	 * warning will be logged.
	 * 
	 * @param memento
	 *            The memento from which the parameters should be read; must not
	 *            be <code>null</code>.
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
			final IMemento memento, final ICommandService commandService,
			final List warningsToLog, final String message, final String id) {
		final String commandId = readRequired(memento, ATT_COMMAND_ID,
				warningsToLog, message, id);
		if (commandId == null) {
			return null;
		}

		final Command command = commandService.getCommand(commandId);
		final ParameterizedCommand parameterizedCommand = readParameters(
				memento, warningsToLog, command);

		return parameterizedCommand;
	}

	/**
	 * Reads the parameters from a parent memento. This is used to read the
	 * parameter sub-elements from a key element. Each parameter is guaranteed
	 * to be valid. If invalid parameters are found, then a warning status will
	 * be appended to the <code>warningsToLog</code> list.
	 * 
	 * @param memento
	 *            The memento from which the parameters should be read; must not
	 *            be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings found during parsing. Warnings found will
	 *            parsing the parameters will be appended to this list. This
	 *            value must not be <code>null</code>.
	 * @param command
	 *            The command around which the parameterization should be
	 *            created; must not be <code>null</code>.
	 * @return The array of parameters found for this memento; <code>null</code>
	 *         if none can be found.
	 */
	protected static final ParameterizedCommand readParameters(
			final IMemento memento, final List warningsToLog,
			final Command command) {
		final IMemento[] parameterMementos = memento
				.getChildren(TAG_PARAMETER);
		if ((parameterMementos == null) || (parameterMementos.length == 0)) {
			return new ParameterizedCommand(command, null);
		}

		final Collection parameters = new ArrayList();
		for (int i = 0; i < parameterMementos.length; i++) {
			final IMemento parameterMemento = parameterMementos[i];

			// Read out the id.
			final String id = parameterMemento.getString(ATT_ID);
			if ((id == null) || (id.length() == 0)) {
				// The name should never be null. This is invalid.
				addWarning(warningsToLog, "Parameters need a name"); //$NON-NLS-1$
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
						"Could not find a matching parameter", id); //$NON-NLS-1$
				continue;
			}

			// Read out the value.
			final String value = parameterMemento.getString(ATT_VALUE);
			if ((value == null) || (value.length() == 0)) {
				// The name should never be null. This is invalid.
				addWarning(warningsToLog, "Parameters need a value", id); //$NON-NLS-1$
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
	 * Reads a required attribute from the memento.
	 * 
	 * @param memento
	 *            The memento from which to read; must not be <code>null</code>.
	 * @param attribute
	 *            The attribute to read; must not be <code>null</code>.
	 * @param warningsToLog
	 *            The list of warnings; must not be <code>null</code>.
	 * @param message
	 *            The warning message to use if the attribute is missing; must
	 *            not be <code>null</code>.
	 * @return The required attribute; may be <code>null</code> if missing.
	 */
	protected static final String readRequired(final IMemento memento,
			final String attribute, final List warningsToLog,
			final String message) {
		return readRequired(memento, attribute, warningsToLog, message, null);
	}

	/**
	 * Reads a required attribute from the memento. This logs the identifier of
	 * the item if this required element cannot be found.
	 * 
	 * @param memento
	 *            The memento from which to read; must not be <code>null</code>.
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
	protected static final String readRequired(final IMemento memento,
			final String attribute, final List warningsToLog,
			final String message, final String id) {
		final String value = memento.getString(attribute);
		if ((value == null) || (value.length() == 0)) {
			addWarning(warningsToLog, message, id);
			return null;
		}

		return value;
	}

	/**
	 * Whether the preference and registry change listeners have been attached
	 * yet.
	 */
	protected boolean preferenceListenerAttached = false;

	/**
	 * The registry change listener for this class.
	 */
	private final IPropertyChangeListener preferenceChangeListener;

	/**
	 * Detaches the preference change listener from the registry.
	 */
	public final void dispose() {
		super.dispose();

		final IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		store.removePropertyChangeListener(preferenceChangeListener);
	}

	/**
	 * Checks whether the preference change could affect this persistence class.
	 * 
	 * @param event
	 *            The event indicating the preference change; must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the persistence instance is affected by
	 *         this change; <code>false</code> otherwise.
	 */
	protected abstract boolean isChangeImportant(final PropertyChangeEvent event);

	/**
	 * Reads the various elements from the registry. Subclasses should extend,
	 * but must not override.
	 */
	protected void read() {
		super.read();

		if (!preferenceListenerAttached) {
			final IPreferenceStore store = WorkbenchPlugin.getDefault()
					.getPreferenceStore();
			store.addPropertyChangeListener(preferenceChangeListener);
		}
	}

	/**
	 * Constructs a new instance of {@link PreferencePersistence}. A preference
	 * change listener is created.
	 */
	protected PreferencePersistence() {
		super();

		preferenceChangeListener = new IPropertyChangeListener() {
			public final void propertyChange(final PropertyChangeEvent event) {
				if (isChangeImportant(event)) {
					read();
				}
			}
		};
	}
}
