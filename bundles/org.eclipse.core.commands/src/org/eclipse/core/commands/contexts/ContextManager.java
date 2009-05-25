/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.commands.contexts;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.common.HandleObjectManager;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.internal.commands.util.Util;

/**
 * <p>
 * A context manager tracks the sets of defined and enabled contexts within the
 * application. The manager sends notification events to listeners when these
 * sets change. It is also possible to retrieve any given context with its
 * identifier.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 */
public final class ContextManager extends HandleObjectManager implements
		IContextListener {
	
	/**
	 * This flag can be set to <code>true</code> if the context manager should
	 * print information to <code>System.out</code> when certain boundary
	 * conditions occur.
	 */
	public static boolean DEBUG = false;

	/**
	 * The set of active context identifiers. This value may be empty, but it is
	 * never <code>null</code>.
	 */
	private Set activeContextIds = new HashSet();

	// allow the ContextManager to send one event for a larger delta
	private boolean caching = false;
	
	private int cachingRef = 0;

	private boolean activeContextsChange = false;
	
	private Set oldIds = null;

	/**
	 * Informs the manager that a batch operation has started.
	 * <p>
	 * <b>Note:</b> You must insure that if you call
	 * <code>deferUpdates(true)</code> that nothing in your batched operation
	 * will prevent the matching call to <code>deferUpdates(false)</code>.
	 * </p>
	 * 
	 * @param defer
	 *            true when starting a batch operation false when ending the
	 *            operation
	 * 
	 * @since 3.5
	 */
	public void deferUpdates(boolean defer) {
		if(defer) {
			cachingRef++;
			if (cachingRef==1) {
				setEventCaching(true);
			}
		}else {
			cachingRef--;
			if (cachingRef==0) {
				setEventCaching(false);
			}
		}
	}
	
	
	/**
	 * Activates a context in this context manager.
	 * 
	 * @param contextId
	 *            The identifier of the context to activate; must not be
	 *            <code>null</code>.
	 */
	public final void addActiveContext(final String contextId) {
		
		if (activeContextIds.contains(contextId)) {
			return;
		}
		activeContextsChange = true;

		if (caching) {
			activeContextIds.add(contextId);
		} else {
			final Set previouslyActiveContextIds = new HashSet(activeContextIds);
			activeContextIds.add(contextId);

			fireContextManagerChanged(new ContextManagerEvent(this, null,
					false, true, previouslyActiveContextIds));
		}

		if (DEBUG) {
			Tracing.printTrace("CONTEXTS", activeContextIds.toString()); //$NON-NLS-1$
		}

	}

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
		addListenerObject(listener);
	}

	public final void contextChanged(final ContextEvent contextEvent) {
		if (contextEvent.isDefinedChanged()) {
			final Context context = contextEvent.getContext();
			final String contextId = context.getId();
			final boolean contextIdAdded = context.isDefined();
			if (contextIdAdded) {
				definedHandleObjects.add(context);
			} else {
				definedHandleObjects.remove(context);
			}
			if (isListenerAttached()) {
				fireContextManagerChanged(new ContextManagerEvent(this,
						contextId, contextIdAdded, false, null));
			}
		}
	}

	/**
	 * Notifies all of the listeners to this manager that the set of defined
	 * context identifiers has changed.
	 * 
	 * @param event
	 *            The event to send to all of the listeners; must not be
	 *            <code>null</code>.
	 */
	private final void fireContextManagerChanged(final ContextManagerEvent event) {
		if (event == null) {
			throw new NullPointerException();
		}

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IContextManagerListener listener = (IContextManagerListener) listeners[i];
			listener.contextManagerChanged(event);
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
		return Collections.unmodifiableSet(activeContextIds);
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
		checkId(contextId);

		Context context = (Context) handleObjectsById.get(contextId);
		if (context == null) {
			context = new Context(contextId);
			handleObjectsById.put(contextId, context);
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
		return getDefinedHandleObjectIds();
	}

	/**
	 * Returns the those contexts that are defined.
	 * 
	 * @return The defined contexts; this value may be empty, but it is never
	 *         <code>null</code>.
	 * @since 3.2
	 */
	public final Context[] getDefinedContexts() {
		return (Context[]) definedHandleObjects
				.toArray(new Context[definedHandleObjects.size()]);
	}

	/**
	 * Deactivates a context in this context manager.
	 * 
	 * @param contextId
	 *            The identifier of the context to deactivate; must not be
	 *            <code>null</code>.
	 */
	public final void removeActiveContext(final String contextId) {
		if (!activeContextIds.contains(contextId)) {
			return;
		}

		activeContextsChange = true;
		if (caching) {
			activeContextIds.remove(contextId);
		} else {
			final Set previouslyActiveContextIds = new HashSet(activeContextIds);
			activeContextIds.remove(contextId);

			fireContextManagerChanged(new ContextManagerEvent(this, null,
					false, true, previouslyActiveContextIds));
		}

		if (DEBUG) {
			Tracing.printTrace("CONTEXTS", activeContextIds.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * Removes a listener from this context manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeContextManagerListener(
			final IContextManagerListener listener) {
		removeListenerObject(listener);
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

		activeContextsChange = true;
		
		final Set previouslyActiveContextIds = this.activeContextIds;
		if (activeContextIds != null) {
			this.activeContextIds = new HashSet();
			this.activeContextIds.addAll(activeContextIds);
		} else {
			this.activeContextIds = null;
		}

		if (DEBUG) {
			Tracing.printTrace("CONTEXTS", (activeContextIds == null) ? "none" //$NON-NLS-1$ //$NON-NLS-2$
					: activeContextIds.toString());
		}

		if (!caching) {
			fireContextManagerChanged(new ContextManagerEvent(this, null,
					false, true, previouslyActiveContextIds));
		}
	}
	
	/**
	 * Set the manager to cache context id changes.
	 * 
	 * @param cache
	 *            <code>true</code> to turn caching on, <code>false</code>
	 *            to turn caching off and send an event if necessary.
	 * @since 3.3
	 */
	private void setEventCaching(boolean cache) {
		if (caching == cache) {
			return;
		}
		caching = cache;
		boolean fireChange = activeContextsChange;
		Set holdOldIds = (oldIds==null?Collections.EMPTY_SET:oldIds);
		
		if (caching) {
			oldIds = new HashSet(activeContextIds);
		} else {
			oldIds = null;
		}
		activeContextsChange = false;

		if (!caching && fireChange) {
			fireContextManagerChanged(new ContextManagerEvent(this, null,
					false, true, holdOldIds));
		}
	}
}
