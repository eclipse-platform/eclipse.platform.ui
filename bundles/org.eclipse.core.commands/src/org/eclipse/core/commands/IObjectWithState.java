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
	 * Usually, consumers will call <code>removeState()</code> with a matching id in
	 * the same lifecycle of IObjectWithState. However, this behavior is not
	 * guaranteed. Implementors should not rely on <code>removeState()</code> for
	 * resource disposal. The recommended practice is to free resources associated
	 * with non-removed states in some kind of dispose() method.
	 *
	 * @see AbstractHandlerWithState
	 *
	 * @param id    The identifier indicating the type of state being added; must
	 *              not be <code>null</code>.
	 * @param state The new state to add to this object; must not be
	 *              <code>null</code>.
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
