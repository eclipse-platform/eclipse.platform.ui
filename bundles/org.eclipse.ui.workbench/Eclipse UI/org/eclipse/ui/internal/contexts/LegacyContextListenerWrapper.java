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
package org.eclipse.ui.internal.contexts;

import java.util.Set;

import org.eclipse.core.commands.contexts.ContextEvent;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextListener;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.ui.contexts.IContext;

/**
 * <p>
 * This wraps an old context listener so it supports the new API. This is used
 * to support attaching old-style listens to the new context objects.
 * </p>
 * 
 * @since 3.1
 */
public class LegacyContextListenerWrapper implements IContextListener,
		IContextManagerListener {

	/**
	 * The legacy context that this listener would previously have been attached
	 * to. This value is never <code>null</code>.
	 */
	private final IContext context;

	/**
	 * The context manager used for constructing the context wrapper when an
	 * event occurs; must not be <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * The listener to be wrapped. This value is never <code>null</code>.
	 */
	private final org.eclipse.ui.contexts.IContextListener wrappedListener;

	/**
	 * Constructs a new instance of <code>ContextListenerWrapper</code>.
	 * 
	 * @param listener
	 *            The listener to be wrapped. Must not be <code>null</code>.
	 * @param contextManager
	 *            The context manager used for constructing the context wrapper
	 *            when an event occurs; must not be <code>null</code>.
	 * @param context
	 *            The legacy context this listener will be listening to; must
	 *            not be <code>null</code>.
	 */
	public LegacyContextListenerWrapper(
			final org.eclipse.ui.contexts.IContextListener listener,
			final ContextManager contextManager, final IContext context) {
		if (listener == null) {
			throw new NullPointerException(
					"Cannot create a listener wrapper on a null listener"); //$NON-NLS-1$
		}

		if (contextManager == null) {
			throw new NullPointerException(
					"Cannot create a listener wrapper with a null context manager"); //$NON-NLS-1$
		}

		if (context == null) {
			throw new NullPointerException(
					"Cannot create a listener wrapper with a null context"); //$NON-NLS-1$
		}

		wrappedListener = listener;
		this.contextManager = contextManager;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.contexts.IContextListener#contextChanged(org.eclipse.core.commands.contexts.ContextEvent)
	 */
	public final void contextChanged(final ContextEvent contextEvent) {
		wrappedListener
				.contextChanged(new org.eclipse.ui.contexts.ContextEvent(
						new ContextLegacyWrapper(contextEvent.getContext(),
								contextManager), contextEvent
								.isDefinedChanged(), false, contextEvent
								.isNameChanged(), contextEvent
								.isParentIdChanged()));
	}

	public final void contextManagerChanged(final ContextManagerEvent event) {
		final String contextId = context.getId();
		final boolean enabledChanged;
		if (event.isActiveContextsChanged()) {
			final Set previousContexts = event.getPreviouslyActiveContextIds();
			final Set currentContexts = contextManager.getActiveContextIds();
			if ((previousContexts != null)
					&& (previousContexts.contains(contextId))
					&& ((currentContexts == null) || (currentContexts
							.contains(contextId)))) {
				enabledChanged = true;
			} else if ((currentContexts != null)
					&& (currentContexts.contains(contextId))
					&& ((previousContexts == null) || (previousContexts
							.contains(contextId)))) {
				enabledChanged = true;
			} else {
				enabledChanged = false;
			}
		} else {
			enabledChanged = false;
		}

		wrappedListener
				.contextChanged(new org.eclipse.ui.contexts.ContextEvent(
						context, false, enabledChanged, false, false));
	}

	public final boolean equals(final Object object) {
		if (object instanceof LegacyContextListenerWrapper) {
			final LegacyContextListenerWrapper other = (LegacyContextListenerWrapper) object;
			return wrappedListener.equals(other.wrappedListener);
		}

		if (object instanceof org.eclipse.ui.contexts.IContextListener) {
			final org.eclipse.ui.contexts.IContextListener other = (org.eclipse.ui.contexts.IContextListener) object;
			return wrappedListener.equals(other);
		}

		return false;
	}

	public final int hashCode() {
		return wrappedListener.hashCode();
	}
}
