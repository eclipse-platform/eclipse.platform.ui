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
package org.eclipse.ui.internal.commands;

import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.ui.commands.ICommand;

/**
 * Wraps a legacy listener in a new listener interface. This simply forwards
 * incoming events through to the old interface.
 * 
 * @since 3.1
 */
final class LegacyCommandListenerWrapper implements ICommandListener {

	/**
	 * The supporting binding manager; never <code>null</code>.
	 */
	private final BindingManager bindingManager;

	/**
	 * The listener which is being wrapped. This value should never be
	 * <code>null</code>.
	 */
	private final org.eclipse.ui.commands.ICommandListener listener;

	/**
	 * Constructs a new instance of <code>CommandListenerWrapper</code> around
	 * a legacy listener.
	 * 
	 * @param listener
	 *            The listener to be wrapped; must not be <code>null</code>.
	 */
	LegacyCommandListenerWrapper(
			final org.eclipse.ui.commands.ICommandListener listener,
			final BindingManager bindingManager) {
		if (listener == null) {
			throw new NullPointerException("Cannot wrap a null listener."); //$NON-NLS-1$
		}

		if (bindingManager == null) {
			throw new NullPointerException(
					"Cannot create a listener wrapper without a binding manager"); //$NON-NLS-1$
		}

		this.listener = listener;
		this.bindingManager = bindingManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.commands.ICommandListener#commandChanged(org.eclipse.commands.CommandEvent)
	 */
	public final void commandChanged(final CommandEvent commandEvent) {
		final ICommand command = new CommandLegacyWrapper(commandEvent.getCommand(),
				bindingManager);
		final boolean definedChanged = commandEvent.isDefinedChanged();
		final boolean descriptionChanged = commandEvent.isDescriptionChanged();
		final boolean handledChanged = commandEvent.isHandledChanged();
		final boolean nameChanged = commandEvent.isNameChanged();

		listener.commandChanged(new org.eclipse.ui.commands.CommandEvent(
				command, false, false, definedChanged, descriptionChanged,
				handledChanged, false, nameChanged, null));

	}

	public final boolean equals(final Object object) {
		if (object instanceof LegacyCommandListenerWrapper) {
			final LegacyCommandListenerWrapper wrapper = (LegacyCommandListenerWrapper) object;
			return listener.equals(wrapper.listener);
		}

		if (object instanceof org.eclipse.ui.commands.ICommandListener) {
			final org.eclipse.ui.commands.ICommandListener other = (org.eclipse.ui.commands.ICommandListener) object;
			return listener.equals(other);
		}

		return false;
	}

	public final int hashCode() {
		return listener.hashCode();
	}
}
