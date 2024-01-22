/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.util.Tracing;
import org.eclipse.ui.internal.commands.ILegacyAttributeNames;
import org.eclipse.ui.internal.misc.Policy;

/**
 * A handler that wraps a legacy handler. This provide backward compatibility
 * with the handlers release in Eclipse 3.0.
 *
 * @since 3.1
 */
public final class LegacyHandlerWrapper implements IHandler {

	/**
	 * This flag can be set to <code>true</code> if commands should print
	 * information to <code>System.out</code> when changing handlers.
	 */
	private static final boolean DEBUG_HANDLERS = Policy.DEBUG_HANDLERS && Policy.DEBUG_HANDLERS_VERBOSE;

	/**
	 * The wrapped handler; never <code>null</code>.
	 */
	private final org.eclipse.ui.commands.IHandler handler;

	/**
	 * Constructs a new instance of <code>HandlerWrapper</code>.
	 *
	 * @param handler The handler that should be wrapped; must not be
	 *                <code>null</code>.
	 */
	public LegacyHandlerWrapper(final org.eclipse.ui.commands.IHandler handler) {
		if (handler == null) {
			throw new NullPointerException("A handler wrapper cannot be constructed on a null handler"); //$NON-NLS-1$
		}

		this.handler = handler;
	}

	@Override
	public void addHandlerListener(final IHandlerListener handlerListener) {
		handler.addHandlerListener(new LegacyHandlerListenerWrapper(this, handlerListener));
	}

	@Override
	public void dispose() {
		handler.dispose();
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof org.eclipse.ui.commands.IHandler) {
			return this.handler == object;
		}

		if (object instanceof LegacyHandlerWrapper) {
			return this.handler == ((LegacyHandlerWrapper) object).handler;
		}

		return false;
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// Debugging output
		if (DEBUG_HANDLERS) {
			final StringBuilder buffer = new StringBuilder("Executing LegacyHandlerWrapper for "); //$NON-NLS-1$
			if (handler == null) {
				buffer.append("no handler"); //$NON-NLS-1$
			} else {
				buffer.append('\'');
				buffer.append(handler.getClass().getName());
				buffer.append('\'');
			}
			Tracing.printTrace("HANDLERS", buffer.toString()); //$NON-NLS-1$
		}

		try {
			return handler.execute(event.getParameters());
		} catch (final org.eclipse.ui.commands.ExecutionException e) {
			throw new ExecutionException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public int hashCode() {
		return this.handler.hashCode();
	}

	@Override
	public boolean isEnabled() {
		final Object enabled = handler.getAttributeValuesByName().get(ILegacyAttributeNames.ENABLED);
		if (enabled instanceof Boolean) {
			return ((Boolean) enabled).booleanValue();
		}

		return true;
	}

	@Override
	public boolean isHandled() {
		final Object handled = handler.getAttributeValuesByName().get(ILegacyAttributeNames.HANDLED);
		if (handled instanceof Boolean) {
			return ((Boolean) handled).booleanValue();
		}

		return true;
	}

	@Override
	public void removeHandlerListener(final IHandlerListener handlerListener) {
		handler.removeHandlerListener(new LegacyHandlerListenerWrapper(this, handlerListener));
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();

		buffer.append("LegacyHandlerWrapper("); //$NON-NLS-1$
		buffer.append(handler);
		buffer.append(')');

		return buffer.toString();
	}
}
