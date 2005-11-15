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
 * A piece of state information that can be shared between objects, and might be
 * persisted between sessions. This can be used for commands that toggle between
 * two states and wish to pass this state information between different
 * handlers.
 * </p>
 * <p>
 * This state object can either be used as a single state object shared between
 * several commands, or one state object per command -- depending on the needs
 * of the application.
 * </p>
 * <p>
 * Clients may implement or extend this interface.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @see AbstractState
 * @since 3.2
 */
public interface IState {

	/**
	 * Adds a listener to changes for this state.
	 * 
	 * @param listener
	 *            The listener to add; must not be <code>null</code>.
	 */
	public void addListener(IStateListener listener);

	/**
	 * Disposes of this state. This allows the state to unregister itself with
	 * any managers or as a listener.
	 */
	public void dispose();

	/**
	 * The current value associated with this state. This can be any type of
	 * object, but implementations will usually restrict this value to a
	 * particular type.
	 * 
	 * @return The current value; may be anything.
	 */
	public Object getValue();

	/**
	 * Sets the value for this state object.
	 * 
	 * @param value
	 *            The value to set; may be anything.
	 */
	public void setValue(Object value);

	/**
	 * Removes a listener to changes from this state.
	 * 
	 * @param listener
	 *            The listener to remove; must not be <code>null</code>.
	 */
	public void removeListener(IStateListener listener);
}
