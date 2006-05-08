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
package org.eclipse.ui.internal.commands;

/**
 * <p>
 * An event indicating that the image bindings have changed.
 * </p>
 * <p>
 * Clients must neither instantiate nor extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is eventually intended to exist in
 * <code>org.eclipse.jface.commands</code>.
 * </p>
 * 
 * @since 3.2
 * @see ICommandImageManagerListener#commandImageManagerChanged(CommandImageManagerEvent)
 */
public final class CommandImageManagerEvent {

	/**
	 * The identifiers of the commands whose image bindings have changed. This
	 * value is never <code>null</code> and never empty.
	 */
	private final String[] changedCommandIds;

	/**
	 * The command image manager that has changed. This value is never
	 * <code>null</code>.
	 */
	private final CommandImageManager commandImageManager;

	/**
	 * The style of image that changed.
	 */
	private final String style;

	/**
	 * The type of image that changed.
	 */
	private final int type;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param commandImageManager
	 *            the instance of the manager that changed; must not be
	 *            <code>null</code>.
	 * @param changedCommandIds
	 *            The identifiers of the commands whose image bindings have
	 *            changed; this value must not be <code>null</code> and must
	 *            not be empty. This value is not copied.
	 */
	CommandImageManagerEvent(final CommandImageManager commandImageManager,
			final String[] changedCommandIds, final int type, final String style) {
		if (commandImageManager == null) {
			throw new NullPointerException("An event must refer to its manager"); //$NON-NLS-1$
		}

		if ((changedCommandIds == null) || (changedCommandIds.length < 1)) {
			throw new IllegalArgumentException(
					"There must be at least one change command identifier"); //$NON-NLS-1$
		}

		this.commandImageManager = commandImageManager;
		this.changedCommandIds = changedCommandIds;
		this.type = type;
		this.style = style;
	}

	/**
	 * Returns the identifiers of the commands whose bindings have changed.
	 * 
	 * @return The identifiers of the commands whose bindings have changed;
	 *         neither <code>null</code> nor empty.
	 */
	public final String[] getChangedCommandIds() {
		final String[] copy = new String[changedCommandIds.length];
		System.arraycopy(changedCommandIds, 0, copy, 0,
				changedCommandIds.length);
		return copy;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final CommandImageManager getCommandImageManager() {
		return commandImageManager;
	}

	/**
	 * Returns whether one of the images of the given command has changed.
	 * 
	 * @param commandId
	 *            The identifier of the command to check; must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if one of the command's images has changed;
	 *         <code>false</code> otherwise.
	 */
	public final boolean isCommandIdChanged(final String commandId) {
		// PERFORMANCE
		for (int i = 0; i < changedCommandIds.length; i++) {
			if (commandId.equals(changedCommandIds[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the image for the command has changed.
	 * 
	 * @param commandId
	 *            The identifier of the command to check; must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the command's image has changed
	 * @see CommandImageManager#getImageDescriptor(String)
	 */
	public final boolean isCommandImageChanged(final String commandId) {
		return isCommandIdChanged(commandId)
				&& (type == CommandImageManager.TYPE_DEFAULT)
				&& (style == null);
	}

	/**
	 * Returns whether the image of the given type for the command has changed.
	 * 
	 * @param commandId
	 *            The identifier of the command to check; must not be
	 *            <code>null</code>.
	 * @param type
	 *            The type of image, one of
	 *            {@link CommandImageManager#TYPE_DEFAULT},
	 *            {@link CommandImageManager#TYPE_DISABLED} or
	 *            {@link CommandImageManager#TYPE_HOVER}.
	 * @return <code>true</code> if the command's image of the given type has
	 *         changed.
	 * @see CommandImageManager#getImageDescriptor(String, int)
	 */
	public final boolean isCommandImageChanged(final String commandId,
			final int type) {
		return isCommandIdChanged(commandId)
				&& ((type == CommandImageManager.TYPE_DEFAULT) || (type == this.type))
				&& (style == null);
	}

	/**
	 * Returns whether the image of the given type and style for the command has
	 * changed.
	 * 
	 * @param commandId
	 *            The identifier of the command to check; must not be
	 *            <code>null</code>.
	 * @param type
	 *            The type of image, one of
	 *            {@link CommandImageManager#TYPE_DEFAULT},
	 *            {@link CommandImageManager#TYPE_DISABLED} or
	 *            {@link CommandImageManager#TYPE_HOVER}.
	 * @param style
	 *            The style of the image; may be anything.
	 * @return <code>true</code> if the command's image of the given type and
	 *         style has changed.
	 * @see CommandImageManager#getImageDescriptor(String, int, String)
	 */
	public final boolean isCommandImageChanged(final String commandId,
			final int type, final String style) {
		return isCommandIdChanged(commandId)
				&& ((type == CommandImageManager.TYPE_DEFAULT) || (type == this.type))
				&& ((style == null) || (style.equals(this.style)));
	}

	/**
	 * Returns whether the image of the given style for the command has changed.
	 * 
	 * @param commandId
	 *            The identifier of the command to check; must not be
	 *            <code>null</code>.
	 * @param style
	 *            The style of the image; may be anything.
	 * @return <code>true</code> if the command's image of the given style has
	 *         changed.
	 * @see CommandImageManager#getImageDescriptor(String, String)
	 */
	public final boolean isCommandImageChanged(final String commandId,
			final String style) {
		return isCommandIdChanged(commandId)
				&& (type == CommandImageManager.TYPE_DEFAULT)
				&& ((style == null) || (style.equals(this.style)));
	}
}
