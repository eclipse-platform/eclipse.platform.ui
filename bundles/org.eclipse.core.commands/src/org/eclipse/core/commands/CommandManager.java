/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A central repository for commands -- both in the defined and undefined
 * states. Commands can be created and retrieved using this manager. It is
 * possible to listen to changes in the collection of commands by attaching a
 * listener to the manager.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @see CommandManager#getCommand(String)
 * @since 3.1
 */
public final class CommandManager implements ICategoryListener,
		ICommandListener {

	/**
	 * The map of category identifiers (<code>String</code>) to categories (
	 * <code>Category</code>). This collection may be empty, but it is never
	 * <code>null</code>.
	 */
	private final Map categoriesById = new HashMap();

	/**
	 * The map of command identifiers (<code>String</code>) to commands (
	 * <code>Command</code>). This collection may be empty, but it is never
	 * <code>null</code>.
	 */
	private final Map commandsById = new HashMap();

	/**
	 * The set of identifiers for those categories that are defined. This value
	 * may be empty, but it is never <code>null</code>.
	 */
	private final Set definedCategoryIds = new HashSet();

	/**
	 * The set of identifiers for those commands that are defined. This value
	 * may be empty, but it is never <code>null</code>.
	 */
	private final Set definedCommandIds = new HashSet();

	/**
	 * The collection of listener to this command manager. This collection is
	 * <code>null</code> if there are no listeners.
	 */
	private Collection listeners = null;

	/**
	 * Adds a listener to this command manager. The listener will be notified
	 * when the set of defined commands changes. This can be used to track the
	 * global appearance and disappearance of commands.
	 * 
	 * @param listener
	 *            The listener to attach; must not be <code>null</code>.
	 */
	public final void addCommandManagerListener(
			final ICommandManagerListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}

		if (listeners == null) {
			listeners = new HashSet();
		}

		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.ICategoryListener#categoryChanged(org.eclipse.core.commands.CategoryEvent)
	 */
	public final void categoryChanged(CategoryEvent categoryEvent) {
		if (categoryEvent.hasDefinedChanged()) {
			final Category category = categoryEvent.getCategory();
			final String categoryId = category.getId();
			final boolean categoryIdAdded = category.isDefined();
			if (categoryIdAdded) {
				definedCategoryIds.add(categoryId);
			} else {
				definedCategoryIds.remove(categoryId);
			}
			fireCommandManagerChanged(new CommandManagerEvent(this, null,
					false, false, categoryId, categoryIdAdded, true));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.commands.ICommandListener#commandChanged(org.eclipse.commands.CommandEvent)
	 */
	public final void commandChanged(final CommandEvent commandEvent) {
		if (commandEvent.hasDefinedChanged()) {
			final Command command = commandEvent.getCommand();
			final String commandId = command.getId();
			final boolean commandIdAdded = command.isDefined();
			if (commandIdAdded) {
				definedCommandIds.add(commandId);
			} else {
				definedCommandIds.remove(commandId);
			}
			fireCommandManagerChanged(new CommandManagerEvent(this, commandId,
					commandIdAdded, true, null, false, false));
		}
	}

	/**
	 * Notifies all of the listeners to this manager that the set of defined
	 * command identifiers has changed.
	 * 
	 * @param commandManagerEvent
	 *            The event to send to all of the listeners; must not be
	 *            <code>null</code>.
	 */
	private final void fireCommandManagerChanged(
			final CommandManagerEvent commandManagerEvent) {
		if (commandManagerEvent == null)
			throw new NullPointerException();

		if (listeners != null) {
			final Iterator listenerItr = listeners.iterator();
			while (listenerItr.hasNext()) {
				final ICommandManagerListener listener = (ICommandManagerListener) listenerItr
						.next();
				listener.commandManagerChanged(commandManagerEvent);
			}
		}
	}

	/**
	 * Gets the category with the given identifier. If no such category
	 * currently exists, then the category will be created (but be undefined).
	 * 
	 * @param categoryId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return The category with the given identifier; this value will never be
	 *         <code>null</code>, but it might be undefined.
	 * @see Category
	 */
	public final Category getCategory(final String categoryId) {
		if (categoryId == null)
			throw new NullPointerException();

		Category category = (Category) categoriesById.get(categoryId);
		if (category == null) {
			category = new Category(categoryId);
			categoriesById.put(categoryId, category);
			category.addCategoryListener(this);
		}

		return category;
	}

	/**
	 * Gets the command with the given identifier. If no such command currently
	 * exists, then the command will be created (but be undefined).
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code> and
	 *            must not be zero-length.
	 * @return The command with the given identifier; this value will never be
	 *         <code>null</code>, but it might be undefined.
	 * @see Command
	 */
	public final Command getCommand(final String commandId) {
		if (commandId == null) {
			throw new NullPointerException(
					"A command may not have a null identifier"); //$NON-NLS-1$
		}

		if (commandId.length() < 1) {
			throw new IllegalArgumentException(
					"The command must not have a zero-length identifier"); //$NON-NLS-1$
		}

		Command command = (Command) commandsById.get(commandId);
		if (command == null) {
			command = new Command(commandId);
			commandsById.put(commandId, command);
			command.addCommandListener(this);
		}

		return command;
	}

	/**
	 * Returns the set of identifiers for those category that are defined.
	 * 
	 * @return The set of defined category identifiers; this value may be empty,
	 *         but it is never <code>null</code>.
	 */
	public final Set getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}

	/**
	 * Returns the set of identifiers for those commands that are defined.
	 * 
	 * @return The set of defined command identifiers; this value may be empty,
	 *         but it is never <code>null</code>.
	 */
	public final Set getDefinedCommandIds() {
		return Collections.unmodifiableSet(definedCommandIds);
	}

	/**
	 * Removes a listener from this command manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeCommandManagerListener(
			final ICommandManagerListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}

		if (listeners == null) {
			return;
		}

		listeners.remove(listener);

		if (listeners.isEmpty()) {
			listeners = null;
		}
	}
}
