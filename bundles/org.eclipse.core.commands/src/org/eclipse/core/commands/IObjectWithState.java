/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.commands;

/**
 * <p>
 * An object that holds zero or more state objects. This state information can
 * be shared between different instances of <code>IObjectWithState</code>.
 * </p>
 * <p>
 * Clients may implement, but must not extend this interface.
 * </p>
 * 
 * @see AbstractHandlerWithState
 * @since 3.2
 */
public interface IObjectWithState {

	/**
	 * Adds state to this object.
	 * 
	 * @param id
	 *            The identifier indicating the type of state being added; must
	 *            not be <code>null</code>.
	 * @param state
	 *            The new state to add to this object; must not be
	 *            <code>null</code>.
	 */
	public void addState(String id, State state);

	/**
	 * Gets the state with the given id.
	 * 
	 * @param stateId
	 *            The identifier of the state to retrieve; must not be
	 *            <code>null</code>.
	 * @return The state; may be <code>null</code> if there is no state with
	 *         the given id.
	 */
	public State getState(String stateId);

	/**
	 * Gets the identifiers for all of the state associated with this object.
	 * 
	 * @return All of the state identifiers; may be empty, but never
	 *         <code>null</code>.
	 */
	public String[] getStateIds();

	/**
	 * Removes state from this object.
	 * 
	 * @param stateId
	 *            The id of the state to remove from this object; must not be
	 *            <code>null</code>.
	 */
	public void removeState(String stateId);
}
