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
package org.eclipse.core.commands;

/**
 * An instance of this class describes changes to an instance of
 * <code>IHandler</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see IHandlerListener#handlerChanged(HandlerEvent)
 */
public class HandlerEvent {

	/**
	 * Whether the enabled state of the handler has changed.
	 */
	private final boolean enabledChanged;

	/**
	 * The handler that changed; this value is never <code>null</code>.
	 */
	private final IHandler handler;

	/**
	 * Whether the handled state of the handler has changed.
	 */
	private final boolean handledChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param handler
	 *            the instance of the interface that changed.
	 * @param enabledChanged
	 *            Whether the enabled state of the handler has changed.
	 * @param handledChanged
	 *            Whether the handled state of the handler has changed.
	 */
	public HandlerEvent(final IHandler handler, final boolean enabledChanged,
			final boolean handledChanged) {
		if (handler == null)
			throw new NullPointerException();

		this.handler = handler;
		this.enabledChanged = enabledChanged;
		this.handledChanged = handledChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public IHandler getHandler() {
		return handler;
	}

	/**
	 * Returns whether or not the handled property changed.
	 * 
	 * @return <code>true</code>, iff the handled property changed.
	 */
	public boolean isEnabledChanged() {
		return enabledChanged;
	}

	/**
	 * Returns whether or not the handled property changed.
	 * 
	 * @return <code>true</code>, iff the handled property changed.
	 */
	public boolean isHandledChanged() {
		return handledChanged;
	}
}