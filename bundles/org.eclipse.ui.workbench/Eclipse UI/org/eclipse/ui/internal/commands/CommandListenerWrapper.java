/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.util.Map;

import org.eclipse.commands.CommandEvent;
import org.eclipse.commands.ICommandListener;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.ui.commands.ICommand;

/**
 * Wraps a legacy listener in a new listener interface. This simply forwards
 * incoming events through to the old interface.
 * 
 * @since 3.1
 */
final class CommandListenerWrapper implements ICommandListener {

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
    CommandListenerWrapper(
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
        final ICommand command = new CommandWrapper(commandEvent.getCommand(),
                bindingManager);
        final Map previousAttributeValuesByName = commandEvent
                .getPreviousAttributeValuesByName();
        final boolean attributeValuesByNameChanged = commandEvent
                .haveAttributeValuesByNameChanged();
        final boolean definedChanged = commandEvent.hasDefinedChanged();
        final boolean descriptionChanged = commandEvent.hasDescriptionChanged();
        final boolean handledChanged = commandEvent.hasHandledChanged();
        final boolean nameChanged = commandEvent.hasNameChanged();

        listener.commandChanged(new org.eclipse.ui.commands.CommandEvent(
                command, attributeValuesByNameChanged, false, definedChanged,
                descriptionChanged, handledChanged, false, nameChanged,
                previousAttributeValuesByName));

    }

    public final boolean equals(final Object object) {
        if (object instanceof SchemeListenerWrapper) {
            final CommandListenerWrapper wrapper = (CommandListenerWrapper) object;
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
