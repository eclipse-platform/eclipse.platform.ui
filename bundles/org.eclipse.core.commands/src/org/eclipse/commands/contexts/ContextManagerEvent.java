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
package org.eclipse.commands.contexts;

/**
 * An event indicating that the set of defined context identifiers has changed.
 * 
 * @since 3.1
 * @see IContextManagerListener#contextManagerChanged(ContextManagerEvent)
 */
public final class ContextManagerEvent {

    /**
     * Whether the list of active context identifiers has changed.
     */
    private final boolean activeContextsChanged;

    /**
     * The context manager that has changed.
     */
    private final ContextManager contextManager;

    /**
     * The context identifier that was added or removed from the list of defined
     * context identifiers.
     */
    private final String contextId;

    /**
     * Whether a context identifier was added to the list of defined command
     * identifier. Otherwise, a command identifier was removed.
     */
    private final boolean contextIdAdded;

    /**
     * Creates a new instance of this class.
     * 
     * @param contextManager
     *            the instance of the interface that changed; must not be
     *            <code>null</code>.
     * @param contextId
     *            The context identifier that was added or removed; may be
     *            <code>null</code> if the active contexts are changing.
     * @param contextIdAdded
     *            Whether the context identifier became defined (otherwise, it
     *            became undefined).
     * @param activeContextsChanged
     *            Whether the list of active contexts has changed.
     */
    public ContextManagerEvent(final ContextManager contextManager,
            final String contextId, final boolean contextIdAdded,
            final boolean activeContextsChanged) {
        if (contextManager == null) {
            throw new NullPointerException();
        }

        this.contextManager = contextManager;
        this.contextId = contextId;
        this.contextIdAdded = contextIdAdded;
        this.activeContextsChanged = activeContextsChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public final ContextManager getContextManager() {
        return contextManager;
    }

    /**
     * Returns the context identifier that was added or removed.
     * 
     * @return The context identifier that was added or removed; never
     *         <code>null</code>.
     */
    public final String getContextId() {
        return contextId;
    }

    /**
     * Returns whether the active context identifiers have changed.
     * 
     * @return <code>true</code> if the collection of active contexts changed;
     *         <code>false</code> otherwise.
     */
    public final boolean haveActiveContextsChanged() {
        return activeContextsChanged;
    }

    /**
     * Returns whether the context identifier became defined. Otherwise, the
     * context identifier became undefined.
     * 
     * @return <code>true</code> if the context identifier became defined;
     *         <code>false</code> if the context identifier became undefined.
     */
    public final boolean isContextIdAdded() {
        return contextIdAdded;
    }
}
