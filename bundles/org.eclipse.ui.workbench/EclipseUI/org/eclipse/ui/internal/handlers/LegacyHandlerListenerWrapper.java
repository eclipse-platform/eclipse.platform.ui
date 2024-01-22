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

import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.commands.HandlerEvent;
import org.eclipse.ui.commands.IHandlerListener;
import org.eclipse.ui.internal.commands.ILegacyAttributeNames;

/**
 * A wrapper so that the new handler listener can work with legacy handlers.
 * This class is only intended for backward compatibility with Eclipse 3.0.
 *
 * @since 3.1
 */
public final class LegacyHandlerListenerWrapper implements IHandlerListener {

	/**
	 * The handler on which this listener is listening; never <code>null</code>.
	 */
	private final IHandler handler;

	/**
	 * The wrapped listener; never <code>null</code>.
	 */
	private final org.eclipse.core.commands.IHandlerListener listener;

	/**
	 * Constructs a new instance of <code>LegacyHandlerListenerWrapper</code>.
	 *
	 * @param listener The listener to wrap; must not be <code>null</code>.
	 */
	public LegacyHandlerListenerWrapper(final IHandler handler,
			final org.eclipse.core.commands.IHandlerListener listener) {
		if (handler == null) {
			throw new NullPointerException("A listener wrapper cannot be created on a null handler"); //$NON-NLS-1$
		}

		if (listener == null) {
			throw new NullPointerException("A listener wrapper cannot be created on a null listener"); //$NON-NLS-1$
		}

		this.handler = handler;
		this.listener = listener;
	}

	@Override
	public void handlerChanged(HandlerEvent event) {
		final boolean enabledChanged = ((Boolean) event.getPreviousAttributeValuesByName()
				.get(ILegacyAttributeNames.ENABLED)).booleanValue() != handler.isEnabled();
		final boolean handledChanged = ((Boolean) event.getPreviousAttributeValuesByName()
				.get(ILegacyAttributeNames.HANDLED)).booleanValue() != handler.isHandled();
		listener.handlerChanged(new org.eclipse.core.commands.HandlerEvent(handler, enabledChanged, handledChanged));
	}
}
