/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.Collection;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.ui.commands.ICommandService;

/**
 * <p>
 * Provides services related to the command architecture within the workbench.
 * This service can be used to access the set of commands and handlers.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
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
	}

	public final Category getCategory(final String categoryId) {
		/*
		 * TODO Need to put in place protection against the category being
		 * changed.
		 */
		return commandManager.getCategory(categoryId);
	}

	public final Command getCommand(final String commandId) {
		/*
		 * TODO Need to put in place protection against the command being
		 * changed.
		 */
		return commandManager.getCommand(commandId);
	}

	public final Collection getDefinedCategoryIds() {
		return commandManager.getDefinedCategoryIds();
	}

	public final Collection getDefinedCommandIds() {
		return commandManager.getDefinedCommandIds();
	}
}
