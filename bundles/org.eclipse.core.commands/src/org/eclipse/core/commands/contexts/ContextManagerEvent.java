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
package org.eclipse.core.commands.contexts;

import java.util.Set;

/**
 * <p>
 * An event indicating that the set of defined context identifiers has changed.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
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
	 * The set of context identifiers (strings) that were active before the
	 * change occurred. If the active contexts did not changed, then this value
	 * is <code>null</code>.
	 */
	private final Set previouslyActiveContextIds;

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
	 * @param previouslyActiveContextIds
	 *            the set of identifiers of previously active contexts. This set
	 *            may be empty. If this set is not empty, it must only contain
	 *            instances of <code>String</code>. This set must be
	 *            <code>null</code> if activeContextChanged is
	 *            <code>false</code> and must not be null if
	 *            activeContextChanged is <code>true</code>.
	 */
	public ContextManagerEvent(final ContextManager contextManager,
			final String contextId, final boolean contextIdAdded,
			final boolean activeContextsChanged,
			final Set previouslyActiveContextIds) {
		if (contextManager == null) {
			throw new NullPointerException();
		}

		this.contextManager = contextManager;
		this.contextId = contextId;
		this.contextIdAdded = contextIdAdded;
		this.activeContextsChanged = activeContextsChanged;
		this.previouslyActiveContextIds = previouslyActiveContextIds;
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
	 * @return The context identifier that was added or removed. This value may
	 *         be <code>null</code> if no context identifier was added or
	 *         removed.
	 */
	public final String getContextId() {
		return contextId;
	}

	/**
	 * Returns the set of identifiers to previously active contexts.
	 * 
	 * @return the set of identifiers to previously active contexts. This set
	 *         may be empty. If this set is not empty, it is guaranteed to only
	 *         contain instances of <code>String</code>. This set is
	 *         guaranteed to be <code>null</code> if
	 *         haveActiveContextChanged() is <code>false</code> and is
	 *         guaranteed to not be null if haveActiveContextsChanged() is
	 *         <code>true</code>.
	 */
	public final Set getPreviouslyActiveContextIds() {
		return previouslyActiveContextIds;
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
