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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.commands.Command;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.commands.NotHandledException;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.ParseException;

/**
 * A wrapper around a core command so that it satisfies the deprecated
 * <code>ICommand</code> interface.
 * 
 * @since 3.1
 */
final class CommandWrapper implements ICommand {

    /**
     * The supporting binding manager; never <code>null</code>.
     */
    private final BindingManager bindingManager;

    /**
     * The wrapped command; never <code>null</code>.
     */
    private final Command command;

    /**
     * Constructs a new <code>CommandWrapper</code>
     * 
     * @param command
     *            The command to be wrapped; must not be <code>null</code>.
     * @param bindingManager
     *            The binding manager to support this wrapper; must not be
     *            <code>null</code>.
     */
    CommandWrapper(final Command command, final BindingManager bindingManager) {
        if (command == null) {
            throw new NullPointerException(
                    "The wrapped command cannot be <code>null</code>."); //$NON-NLS-1$
        }

        if (bindingManager == null) {
            throw new NullPointerException(
                    "A binding manager is required to wrap a command"); //$NON-NLS-1$
        }

        this.command = command;
        this.bindingManager = bindingManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#addCommandListener(org.eclipse.ui.commands.ICommandListener)
     */

    public final void addCommandListener(final ICommandListener commandListener) {
        command.addCommandListener(new CommandListenerWrapper(commandListener,
                bindingManager));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#execute(java.util.Map)
     */
    public final Object execute(Map parameterValuesByName)
            throws ExecutionException, NotHandledException {
        try {
            return command.execute(parameterValuesByName);
        } catch (final org.eclipse.commands.ExecutionException e) {
            throw new ExecutionException(e);
        } catch (final org.eclipse.commands.NotHandledException e) {
            throw new NotHandledException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#getAttributeValuesByName()
     */
    public final Map getAttributeValuesByName() throws NotHandledException {
        try {
            return command.getAttributeValuesByName();
        } catch (final org.eclipse.commands.NotHandledException e) {
            throw new NotHandledException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#getCategoryId()
     */
    public final String getCategoryId() {
        // TODO Does this break anybody?
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#getDescription()
     */
    public final String getDescription() throws NotDefinedException {
        try {
            return command.getDescription();
        } catch (final org.eclipse.commands.misc.NotDefinedException e) {
            throw new NotDefinedException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#getId()
     */
    public final String getId() {
        return command.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#getKeySequenceBindings()
     */
    public final List getKeySequenceBindings() {
        // TODO Make this go faster.
        final List bindings = new ArrayList();
        final Map allBindings = bindingManager.getActiveBindings();
        if (bindings == null) {
            return bindings;
        }

        final Iterator allBindingItr = allBindings.entrySet().iterator();
        while (allBindingItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) allBindingItr.next();
            final String commandId = (String) entry.getValue();
            if (getId().equals(commandId)) {
                try {
                    bindings.add(new KeySequenceBinding(KeySequence
                            .getInstance(entry.getKey().toString()), 0));
                } catch (final ParseException e) {
                    // Oh, well....
                }
            }
        }

        return bindings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#getName()
     */
    public final String getName() throws NotDefinedException {
        try {
            return command.getName();
        } catch (final org.eclipse.commands.misc.NotDefinedException e) {
            throw new NotDefinedException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#isDefined()
     */
    public final boolean isDefined() {
        return command.isDefined();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#isHandled()
     */
    public final boolean isHandled() {
        return command.isHandled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.ICommand#removeCommandListener(org.eclipse.ui.commands.ICommandListener)
     */
    public final void removeCommandListener(
            final ICommandListener commandListener) {
        command.removeCommandListener(new CommandListenerWrapper(
                commandListener, bindingManager));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public final int compareTo(final Object o) {
        return command.compareTo(o);
    }

}
