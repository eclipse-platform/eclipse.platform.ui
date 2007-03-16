/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 * 
 * @since 3.1
 * @see ICommandManagerListener#commandManagerChanged(CommandManagerEvent)
 */
public final class CommandManagerEvent {

	/**
	 * The bit used to represent whether the given category has become defined.
	 * If this bit is not set and there is no category id, then no category has
	 * become defined nor undefined. If this bit is not set and there is a
	 * category id, then the category has become undefined.
	 */
	private static final int CHANGED_CATEGORY_DEFINED = 1;

	/**
     * The bit used to represent whether the given command has become defined.
     * If this bit is not set and there is no command id, then no command has
     * become defined nor undefined. If this bit is not set and there is a
     * command id, then the command has become undefined.
     */
	private static final int CHANGED_COMMAND_DEFINED = 1 << 1;
	
	/**
	 * The bit used to represent whether the given command parameter type has
	 * become defined. If this bit is not set and there is no parameter type id,
	 * then no parameter type has become defined nor undefined. If this bit is
	 * not set and there is a parameter type id, then the parameter type has
	 * become undefined.
	 * 
	 * @since 3.2
	 */
	private static final int CHANGED_PARAMETER_TYPE_DEFINED = 1 << 2;

	/**
	 * The category identifier that was added or removed from the list of
	 * defined category identifiers. This value is <code>null</code> if the
	 * list of defined category identifiers did not change.
	 */
	private final String categoryId;

	/**
	 * A collection of bits representing whether certain values have changed. A
	 * bit is set (i.e., <code>1</code>) if the corresponding property has
	 * changed.
	 */
	private final int changedValues;

	/**
	 * The command identifier that was added or removed from the list of defined
	 * command identifiers. This value is <code>null</code> if the list of
	 * defined command identifiers did not change.
	 */
	private final String commandId;
	
	/**
	 * The command parameter type identifier that was added or removed from the
	 * list of defined parameter type identifiers. This value is
	 * <code>null</code> if the list of defined parameter type identifiers did
	 * not change.
	 * 
	 * @since 3.2
	 */
	private final String parameterTypeId;

	/**
	 * The command manager that has changed.
	 */
	private final CommandManager commandManager;

	/**
	 * Creates a new <code>CommandManagerEvent</code> instance to describe
	 * changes to commands and/or categories.
	 * 
	 * @param commandManager
	 *            the instance of the interface that changed; must not be
	 *            <code>null</code>.
	 * @param commandId
	 *            The command identifier that was added or removed; must not be
	 *            <code>null</code> if commandIdChanged is <code>true</code>.
	 * @param commandIdAdded
	 *            Whether the command identifier became defined (otherwise, it
	 *            became undefined).
	 * @param commandIdChanged
	 *            Whether the list of defined command identifiers has changed.
	 * @param categoryId
	 *            The category identifier that was added or removed; must not be
	 *            <code>null</code> if categoryIdChanged is <code>true</code>.
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
		this.categoryId = categoryId;
		
		// this constructor only works for changes to commands and categories
		this.parameterTypeId = null;

		int changedValues = 0;
		if (categoryIdChanged && categoryIdAdded) {
			changedValues |= CHANGED_CATEGORY_DEFINED;
		}
		if (commandIdChanged && commandIdAdded) {
			changedValues |= CHANGED_COMMAND_DEFINED;
		}
		this.changedValues = changedValues;
	}
	
	/**
	 * Creates a new <code>CommandManagerEvent</code> instance to describe
	 * changes to command parameter types.
	 * 
	 * @param commandManager
	 *            the instance of the interface that changed; must not be
	 *            <code>null</code>.
	 * @param parameterTypeId
	 *            The command parameter type identifier that was added or
	 *            removed; must not be <code>null</code> if
	 *            parameterTypeIdChanged is <code>true</code>.
	 * @param parameterTypeIdAdded
	 *            Whether the parameter type identifier became defined
	 *            (otherwise, it became undefined).
	 * @param parameterTypeIdChanged
	 *            Whether the list of defined parameter type identifiers has
	 *            changed.
	 * 
	 * @since 3.2
	 */
	public CommandManagerEvent(final CommandManager commandManager,
			final String parameterTypeId, final boolean parameterTypeIdAdded,
			final boolean parameterTypeIdChanged) {

		if (commandManager == null) {
			throw new NullPointerException(
					"An event must refer to its command manager"); //$NON-NLS-1$
		}

		if (parameterTypeIdChanged && (parameterTypeId == null)) {
			throw new NullPointerException(
					"If the list of defined command parameter types changed, then the added/removed parameter type must be mentioned"); //$NON-NLS-1$
		}

		this.commandManager = commandManager;
		this.commandId = null;
		this.categoryId = null;

		this.parameterTypeId = parameterTypeId;

		int changedValues = 0;
		if (parameterTypeIdChanged && parameterTypeIdAdded) {
			changedValues |= CHANGED_PARAMETER_TYPE_DEFINED;
		}

		this.changedValues = changedValues;
	}

	/**
	 * Returns the category identifier that was added or removed.
	 * 
	 * @return The category identifier that was added or removed; may be
	 *         <code>null</code>.
	 */
	public final String getCategoryId() {
		return categoryId;
	}

	/**
	 * Returns the command identifier that was added or removed.
	 * 
	 * @return The command identifier that was added or removed; may be
	 *         <code>null</code>.
	 */
	public final String getCommandId() {
		return commandId;
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
	 * Returns the command parameter type identifier that was added or removed.
	 * 
	 * @return The command parameter type identifier that was added or removed;
	 *         may be <code>null</code>.
	 *         
	 * @since 3.2
	 */
	public final String getParameterTypeId() {
		return parameterTypeId;
	}

	/**
	 * Returns whether the list of defined category identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of category identifiers has
	 *         changed; <code>false</code> otherwise.
	 */
	public final boolean isCategoryChanged() {
		return (categoryId != null);
	}

	/**
	 * Returns whether the category identifier became defined. Otherwise, the
	 * category identifier became undefined.
	 * 
	 * @return <code>true</code> if the category identifier became defined;
	 *         <code>false</code> if the category identifier became undefined.
	 */
	public final boolean isCategoryDefined() {
		return (((changedValues & CHANGED_CATEGORY_DEFINED) != 0) && (categoryId != null));
	}

	/**
	 * Returns whether the list of defined command identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of command identifiers has
	 *         changed; <code>false</code> otherwise.
	 */
	public final boolean isCommandChanged() {
		return (commandId != null);
	}

	/**
	 * Returns whether the command identifier became defined. Otherwise, the
	 * command identifier became undefined.
	 * 
	 * @return <code>true</code> if the command identifier became defined;
	 *         <code>false</code> if the command identifier became undefined.
	 */
	public final boolean isCommandDefined() {
		return (((changedValues & CHANGED_COMMAND_DEFINED) != 0) && (commandId != null));
	}
	
	/**
	 * Returns whether the list of defined command parameter type identifiers
	 * has changed.
	 * 
	 * @return <code>true</code> if the list of command parameter type
	 *         identifiers has changed; <code>false</code> otherwise.
	 * 
	 * @since 3.2
	 */
	public final boolean isParameterTypeChanged() {
		return (parameterTypeId != null);
	}
	
	/**
	 * Returns whether the command parameter type identifier became defined.
	 * Otherwise, the command parameter type identifier became undefined.
	 * 
	 * @return <code>true</code> if the command parameter type identifier
	 *         became defined; <code>false</code> if the command parameter
	 *         type identifier became undefined.
	 * 
	 * @since 3.2
	 */
	public final boolean isParameterTypeDefined() {
		return (((changedValues & CHANGED_PARAMETER_TYPE_DEFINED) != 0) && (parameterTypeId != null));
	}
}
