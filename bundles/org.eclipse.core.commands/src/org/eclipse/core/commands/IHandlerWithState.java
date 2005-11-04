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

package org.eclipse.core.commands;

/**
 * <p>
 * A handler that is capable of receiving state information from the command.
 * This state information is shared between handlers as they switch which
 * handler is active.
 * </p>
 * <p>
 * Clients may implement, but must not extend this interface.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @see AbstractHandlerWithState
 * @since 3.2
 */
public interface IHandlerWithState extends IHandler {

	/**
	 * Adds state to the handler. This method is called once for every piece of
	 * handler state when the handler becomes active.
	 * 
	 * @param state
	 *            The new state to add to this handler; must not be
	 *            <code>null</code>.
	 */
	public void addState(IHandlerState state);

	/**
	 * Removes state from this handler. This method is called once for every
	 * piece of handler state when the becomes inactive.
	 * 
	 * @param state
	 *            The state to remove from this handler; must not be
	 *            <code>null</code>.
	 */
	public void removeState(IHandlerState state);
}
