/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

/**
 * <p>
 * An instance of <code>CommandEvent</code> describes changes to an instance
 * of <code>ICommand</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICommand
 * @see ICommandListener#commandChanged
 */
public final class CommandEvent {

	private boolean activeChanged;
	private boolean activityBindingsChanged;
	private boolean categoryIdChanged;
	private ICommand command;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean imageBindingsChanged;
	private boolean keySequenceBindingsChanged;
	private boolean nameChanged;

	/**
	 * TODO javadoc
	 * 
	 * @param command
	 * @param activeChanged
	 * @param activityBindingsChanged
	 * @param categoryIdChanged
	 * @param definedChanged
	 * @param descriptionChanged
	 * @param imageBindingsChanged
	 * @param keySequenceBindingsChanged
	 * @param nameChanged
	 */
	public CommandEvent(
		ICommand command,
		boolean activeChanged,
		boolean activityBindingsChanged,
		boolean categoryIdChanged,
		boolean definedChanged,
		boolean descriptionChanged,
		boolean imageBindingsChanged,
		boolean keySequenceBindingsChanged,
		boolean nameChanged) {
		if (command == null)
			throw new NullPointerException();

		this.command = command;
		this.activeChanged = activeChanged;
		this.activityBindingsChanged = activityBindingsChanged;
		this.categoryIdChanged = categoryIdChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.imageBindingsChanged = imageBindingsChanged;
		this.keySequenceBindingsChanged = keySequenceBindingsChanged;
		this.nameChanged = nameChanged;
	}

	/**
	 * Returns the instance of <code>ICommand</code> that has changed.
	 * 
	 * @return the instance of <code>ICommand</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public ICommand getCommand() {
		return command;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasActiveChanged() {
		return activeChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasCategoryIdChanged() {
		return categoryIdChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActivityBindingsChanged() {
		return activityBindingsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveImageBindingsChanged() {
		return imageBindingsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveKeySequenceBindingsChanged() {
		return keySequenceBindingsChanged;
	}
}
