/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.commands;

/**
 * An event indicating that the set of defined command identifiers has changed.
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
     * The command identifier that was added or removed from the list of defined
     * command identifiers.
     */
    private final String commandId;

    /**
     * Whether a command identifier was added to the list of defined command
     * identifier. Otherwise, a command identifier was removed.
     */
    private final boolean commandIdAdded;

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
     */
    public CommandManagerEvent(final CommandManager commandManager,
            final String commandId, final boolean commandIdAdded) {
        if ((commandManager == null) || (commandId == null))
            throw new NullPointerException();

        this.commandManager = commandManager;
        this.commandId = commandId;
        this.commandIdAdded = commandIdAdded;
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
}
