/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.commands;

import org.eclipse.core.commands.common.AbstractNamedHandleEvent;

/**
 * An instance of this class describes changes to an instance of
 * <code>Command</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see ICommandListener#commandChanged(CommandEvent)
 */
public class CommandEvent extends AbstractNamedHandleEvent {

	/**
	 * The bit used to represent whether the command has changed its category.
	 */
	private static final int CHANGED_CATEGORY = LAST_USED_BIT << 1;

	/**
	 * The bit used to represent whether the command has changed its handler.
	 */
	private static final int CHANGED_HANDLED = LAST_USED_BIT << 2;

	/**
	 * The command that has changed; this value is never <code>null</code>.
	 */
	private final Command command;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param command
	 *            the instance of the interface that changed.
	 * @param categoryChanged
	 *            true, iff the category property changed.
	 * @param definedChanged
	 *            true, iff the defined property changed.
	 * @param descriptionChanged
	 *            true, iff the description property changed.
	 * @param handledChanged
	 *            true, iff the handled property changed.
	 * @param nameChanged
	 *            true, iff the name property changed.
	 */
	public CommandEvent(final Command command, final boolean categoryChanged,
			final boolean definedChanged, final boolean descriptionChanged,
			final boolean handledChanged, final boolean nameChanged) {
		super(definedChanged, descriptionChanged, nameChanged);

		if (command == null)
			throw new NullPointerException();
		this.command = command;

		if (categoryChanged) {
			changedValues |= CHANGED_CATEGORY;
		}
		if (handledChanged) {
			changedValues |= CHANGED_HANDLED;
		}
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final Command getCommand() {
		return command;
	}

	/**
	 * Returns whether or not the category property changed.
	 * 
	 * @return true, iff the category property changed.
	 */
	public final boolean isCategoryChanged() {
		return ((changedValues & CHANGED_CATEGORY) != 0);
	}

	/**
	 * Returns whether or not the handled property changed.
	 * 
	 * @return true, iff the handled property changed.
	 */
	public final boolean isHandledChanged() {
		return ((changedValues & CHANGED_HANDLED) != 0);
	}
}
