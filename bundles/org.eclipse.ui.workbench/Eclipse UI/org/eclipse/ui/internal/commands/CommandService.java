/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.Collection;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * <p>
 * Provides services related to the command architecture within the workbench.
 * This service can be used to access the set of commands and handlers.
 * </p>
 * 
 * @since 3.1
 */
public final class CommandService implements ICommandService {

	/**
	 * The preference key prefix for all handler state.
	 */
	private static final String PREFERENCE_KEY_PREFIX = "org.eclipse.ui.commands/state"; //$NON-NLS-1$

	/**
	 * Creates a preference key for the given piece of state on the given
	 * command.
	 * 
	 * @param command
	 *            The command for which the preference key should be created;
	 *            must not be <code>null</code>.
	 * @param stateId
	 *            The identifier of the state for which the preference key
	 *            should be created; must not be <code>null</code>.
	 * @return A suitable preference key; never <code>null</code>.
	 */
	static final String createPreferenceKey(final Command command,
			final String stateId) {
		return PREFERENCE_KEY_PREFIX + '/' + command.getId() + '/' + stateId;
	}

	/**
	 * The command manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final CommandManager commandManager;

	/**
	 * The persistence class for this command service.
	 */
	private final CommandPersistence commandPersistence;

	/**
	 * Constructs a new instance of <code>CommandService</code> using a
	 * command manager.
	 * 
	 * @param commandManager
	 *            The command manager to use; must not be <code>null</code>.
	 */
	public CommandService(final CommandManager commandManager) {
		if (commandManager == null) {
			throw new NullPointerException(
					"Cannot create a command service with a null manager"); //$NON-NLS-1$
		}
		this.commandManager = commandManager;
		this.commandPersistence = new CommandPersistence(this);
	}

	public final void addExecutionListener(final IExecutionListener listener) {
		commandManager.addExecutionListener(listener);
	}

	public final void defineUncategorizedCategory(final String name,
			final String description) {
		commandManager.defineUncategorizedCategory(name, description);
	}

	public final ParameterizedCommand deserialize(
			final String serializedParameterizedCommand)
			throws NotDefinedException, SerializationException {
		return commandManager.deserialize(serializedParameterizedCommand);
	}

	public final void dispose() {
		commandPersistence.dispose();

		/*
		 * All state on all commands neeeds to be disposed. This is so that the
		 * state has a chance to persist any changes.
		 */
		final Command[] commands = commandManager.getAllCommands();
		for (int i = 0; i < commands.length; i++) {
			final Command command = commands[i];
			final String[] stateIds = command.getStateIds();
			for (int j = 0; j < stateIds.length; j++) {
				final String stateId = stateIds[j];
				final State state = command.getState(stateId);
				if (state instanceof PersistentState) {
					final PersistentState persistentState = (PersistentState) state;
					if (persistentState.shouldPersist()) {
						persistentState.save(PrefUtil
								.getInternalPreferenceStore(),
								createPreferenceKey(command, stateId));
					}
				}
			}
		}
	}

	public final Category getCategory(final String categoryId) {
		return commandManager.getCategory(categoryId);
	}

	public final Command getCommand(final String commandId) {
		return commandManager.getCommand(commandId);
	}

	public final Category[] getDefinedCategories() {
		return commandManager.getDefinedCategories();
	}

	public final Collection getDefinedCategoryIds() {
		return commandManager.getDefinedCategoryIds();
	}

	public final Collection getDefinedCommandIds() {
		return commandManager.getDefinedCommandIds();
	}

	public final Command[] getDefinedCommands() {
		return commandManager.getDefinedCommands();
	}

	public Collection getDefinedParameterTypeIds() {
		return commandManager.getDefinedParameterTypeIds();
	}

	public ParameterType[] getDefinedParameterTypes() {
		return commandManager.getDefinedParameterTypes();
	}

	public ParameterType getParameterType(final String parameterTypeId) {
		return commandManager.getParameterType(parameterTypeId);
	}

	public final void readRegistry() {
		commandPersistence.read();
	}

	public final void removeExecutionListener(final IExecutionListener listener) {
		commandManager.removeExecutionListener(listener);
	}
}
