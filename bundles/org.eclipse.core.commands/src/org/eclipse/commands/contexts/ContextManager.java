/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.commands.contexts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.commands.misc.Util;

/**
 * <p>
 * A context manager tracks the sets of defined and enabled contexts within the
 * application. The manager sends notification events to listeners when these
 * sets change. It is also possible to retrieve any given context with its
 * identifier.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.1
 */
public final class ContextManager implements IContextListener {

    /**
     * The set of active context identifiers. This value may be empty, but it is
     * never <code>null</code>.
     */
    private Set activeContextIds = new HashSet();

    /**
     * The collection of listener to this context manager. This collection is
     * <code>null</code> if there are no listeners.
     */
    private Collection listeners = null;

    /**
     * The map of context identifiers (<code>String</code>) to contexts (
     * <code>Context</code>). This collection may be empty, but it is never
     * <code>null</code>.
     */
    private final Map contextsById = new HashMap();

    /**
     * The set of identifiers for those contexts that are defined. This value
     * may be empty, but it is never <code>null</code>.
     */
    private final Set definedContextIds = new HashSet();

    /**
     * Adds a listener to this context manager. The listener will be notified
     * when the set of defined contexts changes. This can be used to track the
     * global appearance and disappearance of contexts.
     * 
     * @param listener
     *            The listener to attach; must not be <code>null</code>.
     */
    public final void addContextManagerListener(
            final IContextManagerListener listener) {
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
     * @see org.eclipse.commands.contexts.IContextListener#contextChanged(org.eclipse.commands.contexts.ContextEvent)
     */
    public final void contextChanged(final ContextEvent contextEvent) {
        if (contextEvent.hasDefinedChanged()) {
            final Context context = contextEvent.getContext();
            final String contextId = context.getId();
            final boolean contextIdAdded = context.isDefined();
            if (contextIdAdded) {
                definedContextIds.add(contextId);
            } else {
                definedContextIds.remove(contextId);
            }
            fireContextManagerChanged(new ContextManagerEvent(this, contextId,
                    contextIdAdded, false));
        }
    }

    /**
     * Notifies all of the listeners to this manager that the set of defined
     * context identifiers has changed.
     * 
     * @param contextManagerEvent
     *            The event to send to all of the listeners; must not be
     *            <code>null</code>.
     */
    private final void fireContextManagerChanged(
            final ContextManagerEvent contextManagerEvent) {
        if (contextManagerEvent == null)
            throw new NullPointerException();

        if (listeners != null) {
            final Iterator listenerItr = listeners.iterator();
            while (listenerItr.hasNext()) {
                final IContextManagerListener listener = (IContextManagerListener) listenerItr
                        .next();
                listener.contextManagerChanged(contextManagerEvent);
            }
        }
    }

    /**
     * Returns the set of active context identifiers.
     * 
     * @return The set of active context identifiers; this value may be
     *         <code>null</code> if no active contexts have been set yet. If
     *         the set is not <code>null</code>, then it contains only
     *         instances of <code>String</code>.
     */
    public final Set getActiveContextIds() {
        return activeContextIds;
    }

    /**
     * Gets the context with the given identifier. If no such context currently
     * exists, then the context will be created (but be undefined).
     * 
     * @param contextId
     *            The identifier to find; must not be <code>null</code>.
     * @return The context with the given identifier; this value will never be
     *         <code>null</code>, but it might be undefined.
     * @see Context
     */
    public final Context getContext(final String contextId) {
        if (contextId == null)
            throw new NullPointerException();

        Context context = (Context) contextsById.get(contextId);
        if (context == null) {
            context = new Context(contextId);
            contextsById.put(contextId, context);
            context.addContextListener(this);
        }

        return context;
    }

    /**
     * Returns the set of identifiers for those contexts that are defined.
     * 
     * @return The set of defined context identifiers; this value may be empty,
     *         but it is never <code>null</code>.
     */
    public final Set getDefinedContextIds() {
        return Collections.unmodifiableSet(definedContextIds);
    }

    /**
     * Removes a listener from this context manager.
     * 
     * @param listener
     *            The listener to be removed; must not be <code>null</code>.
     */
    public final void removeContextManagerListener(
            final IContextManagerListener listener) {
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

    /**
     * Changes the set of active contexts for this context manager. The whole
     * set is required so that internal consistency can be maintained and so
     * that excessive recomputations do nothing occur.
     * 
     * @param activeContextIds
     *            The new set of active context identifiers; may be
     *            <code>null</code>.
     */
    public final void setActiveContextIds(final Set activeContextIds) {
        if (Util.equals(this.activeContextIds, activeContextIds)) {
            return;
        }

        if (activeContextIds != null) {
            this.activeContextIds = Collections
                    .unmodifiableSet(activeContextIds);
        } else {
            this.activeContextIds = null;
        }
        fireContextManagerChanged(new ContextManagerEvent(this, null, false,
                true));
    }
}
