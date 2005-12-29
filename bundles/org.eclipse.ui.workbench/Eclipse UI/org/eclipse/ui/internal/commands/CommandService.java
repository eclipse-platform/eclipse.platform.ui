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
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.commands.ICommandService;

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
