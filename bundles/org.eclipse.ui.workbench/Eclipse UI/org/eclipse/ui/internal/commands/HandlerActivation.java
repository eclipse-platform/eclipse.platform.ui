/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.commands.IHandlerActivation;

/**
 * <p>
 * A token representing the activation of a handler. This token can later be
 * used to cancel that activation. Without this token, then handler will only
 * become inactive if the component in which the handler was activated is
 * destroyed.
 * </p>
 * <p>
 * This caches the command id and the handler, so that they can later be
 * identifier.
 * </p>
 * <p>
 * TODO This mechanism will not actually work strictly according to
 * specifications. If someone submits the exact same handler, then some further
 * distinguishing characteristic must be used.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public final class HandlerActivation implements IHandlerActivation {

	/**
	 * The identifier for the command which the activated handler handles. This
	 * value is never <code>null</code>.
	 */
	private final String commandId;

	/**
	 * The handler that has been activated. This value is never
	 * <code>null</code>.
	 */
	private final IHandler handler;

	/**
	 * Constructs a new instance of <code>HandlerActivation</code>.
	 * 
	 * @param commandId
	 *            The identifier for the command which the activated handler
	 *            handles. This value must not be <code>null</code>.
	 * @param handler
	 *            The handler that has been activated. This value must not be
	 *            <code>null</code>.
	 */
	public HandlerActivation(final String commandId, final IHandler handler) {
		this.commandId = commandId;
		this.handler = handler;
	}
}
