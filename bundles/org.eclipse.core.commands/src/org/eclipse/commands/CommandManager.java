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
package org.eclipse.commands;

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
 * 
 * @see CommandManager#getCommand(String)
 * @since 3.1
 */
public final class CommandManager implements ICommandListener {

    /**
     * The collection of listener to this command manager. This collection is
     * <code>null</code> if there are no listeners.
     */
    private Collection listeners = null;

    /**
     * The map of command identifiers (<code>String</code>) to commands (
     * <code>Command</code>). This collection may be empty, but it is never
     * <code>null</code>.
     */
    private final Map commandsById = new HashMap();

    /**
     * The set of identifiers for those commands that are defined. This value
     * may be empty, but it is never <code>null</code>.
     */
    private final Set definedCommandIds = new HashSet();

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
                    commandIdAdded));
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
     * Gets the command with the given identifier. If no such command currently
     * exists, then the command will be created (but be undefined).
     * 
     * @param commandId
     *            The identifier to find; must not be <code>null</code>.
     * @return The command with the given identifier; this value will never be
     *         <code>null</code>, but it might be undefined.
     * @see Command
     */
    public final Command getCommand(final String commandId) {
        if (commandId == null)
            throw new NullPointerException();

        Command command = (Command) commandsById.get(commandId);
        if (command == null) {
            command = new Command(commandId);
            commandsById.put(commandId, command);
            command.addCommandListener(this);
        }

        return command;
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
