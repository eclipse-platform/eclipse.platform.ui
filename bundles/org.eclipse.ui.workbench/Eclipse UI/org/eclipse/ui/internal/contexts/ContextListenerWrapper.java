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
package org.eclipse.ui.internal.contexts;

import org.eclipse.core.commands.contexts.ContextEvent;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.IContextListener;

/**
 * <p>
 * This wraps an old context listener so it supports the new API. This is used
 * to support attaching old-style listens to the new context objects.
 * </p>
 * 
 * @since 3.1
 */
public class ContextListenerWrapper implements IContextListener {

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
	 */
	public ContextListenerWrapper(
			final org.eclipse.ui.contexts.IContextListener listener,
			final ContextManager contextManager) {
		if (listener == null) {
			throw new NullPointerException(
					"Cannot create a listener wrapper on a null listener"); //$NON-NLS-1$
		}

		if (contextManager == null) {
			throw new NullPointerException(
					"Cannot create a listener wrapper with a null context manager"); //$NON-NLS-1$
		}

		wrappedListener = listener;
		this.contextManager = contextManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.contexts.IContextListener#contextChanged(org.eclipse.core.commands.contexts.ContextEvent)
	 */
	public void contextChanged(ContextEvent contextEvent) {
		wrappedListener
				.contextChanged(new org.eclipse.ui.contexts.ContextEvent(
						new ContextWrapper(contextEvent.getContext(),
								contextManager), contextEvent
								.hasDefinedChanged(), contextEvent
								.hasEnabledChanged(), contextEvent
								.hasNameChanged(), contextEvent
								.hasParentIdChanged()));
	}

	public final boolean equals(final Object object) {
		if (object instanceof ContextListenerWrapper) {
			final ContextListenerWrapper other = (ContextListenerWrapper) object;
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
