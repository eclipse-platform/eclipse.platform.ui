/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

/**
 * <p>
 * An event indicating that the set of defined command identifiers has changed.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see ICommandManagerListener#commandManagerChanged(CommandManagerEvent)
 */
public final class CommandManagerEvent {

	/**
	 * The command manager that has changed.
	 */
	private final CommandManager commandManager;

	/**
	 * The category identifier that was added or removed from the list of
	 * defined category identifiers. This value is <code>null</code> if the
	 * list of defined category identifiers did not change.
	 */
	private final String categoryId;

	/**
	 * Whether a category identifier was added to the list of defined category
	 * identifiers. Otherwise, a category identifier was removed.
	 */
	private final boolean categoryIdAdded;

	/**
	 * Whether the list of defined category identifiers has changed.
	 */
	private final boolean categoryIdChanged;

	/**
	 * The command identifier that was added or removed from the list of defined
	 * command identifiers. This value is <code>null</code> if the list of
	 * defined category identifiers did not change.
	 */
	private final String commandId;

	/**
	 * Whether a command identifier was added to the list of defined command
	 * identifiers. Otherwise, a command identifier was removed.
	 */
	private final boolean commandIdAdded;

	/**
	 * Whether the list of defined command identifiers has changed.
	 */
	private final boolean commandIdChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param commandManager
	 *            the instance of the interface that changed; must not be
	 *            <code>null</code>.
	 * @param commandId
	 *            The command identifier that was added or removed; must not be
	 *            <code>null</code>.
	 * @param commandIdAdded
	 *            Whether the command identifier became defined (otherwise, it
	 *            became undefined).
	 * @param commandIdChanged
	 *            Whether the list of defined command identifiers has changed.
	 * @param categoryId
	 *            The category identifier that was added or removed; must not be
	 *            <code>null</code>.
	 * @param categoryIdAdded
	 *            Whether the category identifier became defined (otherwise, it
	 *            became undefined).
	 * @param categoryIdChanged
	 *            Whether the list of defined category identifiers has changed.
	 */
	public CommandManagerEvent(final CommandManager commandManager,
			final String commandId, final boolean commandIdAdded,
			final boolean commandIdChanged, final String categoryId,
			final boolean categoryIdAdded, final boolean categoryIdChanged) {
		if (commandManager == null) {
			throw new NullPointerException(
					"An event must refer to its command manager"); //$NON-NLS-1$
		}

		if (commandIdChanged && (commandId == null)) {
			throw new NullPointerException(
					"If the list of defined commands changed, then the added/removed command must be mentioned"); //$NON-NLS-1$
		}

		if (categoryIdChanged && (categoryId == null)) {
			throw new NullPointerException(
					"If the list of defined categories changed, then the added/removed category must be mentioned"); //$NON-NLS-1$
		}

		this.commandManager = commandManager;
		this.commandId = commandId;
		this.commandIdAdded = commandIdAdded;
		this.commandIdChanged = commandIdChanged;
		this.categoryId = categoryId;
		this.categoryIdAdded = categoryIdAdded;
		this.categoryIdChanged = categoryIdChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final CommandManager getCommandManager() {
		return commandManager;
	}

	/**
	 * Returns the category identifier that was added or removed.
	 * 
	 * @return The category identifier that was added or removed; never
	 *         <code>null</code>.
	 */
	public final String getCategoryId() {
		return categoryId;
	}

	/**
	 * Returns whether the category identifier became defined. Otherwise, the
	 * category identifier became undefined.
	 * 
	 * @return <code>true</code> if the category identifier became defined;
	 *         <code>false</code> if the category identifier became undefined.
	 */
	public final boolean isCategoryIdAdded() {
		return categoryIdAdded;
	}

	/**
	 * Returns whether the list of defined category identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of category identifiers has
	 *         changed; <code>false</code> otherwise.
	 */
	public final boolean isCategoryIdChanged() {
		return categoryIdChanged;
	}

	/**
	 * Returns the command identifier that was added or removed.
	 * 
	 * @return The command identifier that was added or removed; never
	 *         <code>null</code>.
	 */
	public final String getCommandId() {
		return commandId;
	}

	/**
	 * Returns whether the command identifier became defined. Otherwise, the
	 * command identifier became undefined.
	 * 
	 * @return <code>true</code> if the command identifier became defined;
	 *         <code>false</code> if the command identifier became undefined.
	 */
	public final boolean isCommandIdAdded() {
		return commandIdAdded;
	}

	/**
	 * Returns whether the list of defined command identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of command identifiers has
	 *         changed; <code>false</code> otherwise.
	 */
	public final boolean isCommandIdChanged() {
		return commandIdChanged;
	}
}
