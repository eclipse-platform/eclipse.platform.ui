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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An abstract implementation of {@link IObjectWithState}. This provides basic
 * handling for adding and remove state. When state is added, the handler
 * attaches itself as a listener and fire a handleStateChange event to notify
 * this handler. When state is removed, the handler removes itself as a
 * listener.
 * </p>
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * @since 3.2
 */
public abstract class AbstractHandlerWithState extends AbstractHandler
		implements IObjectWithState, IStateListener {

	/**
	 * The map of states currently held by this handler. If this handler has no
	 * state (generally, when inactive), then this will be <code>null</code>.
	 */
	private Map states = null;

	/**
	 * <p>
	 * Adds a state to this handler. This will add this handler as a listener to
	 * the state, and then fire a handleStateChange so that the handler can
	 * respond to the incoming state.
	 * </p>
	 * <p>
	 * Clients may extend this method, but they should call this super method
	 * first before doing anything else.
	 * </p>
	 * 
	 * @param stateId
	 *            The identifier indicating the type of state being added; must
	 *            not be <code>null</code>.
	 * @param state
	 *            The state to add; must not be <code>null</code>.
	 */
	public void addState(final String stateId, final State state) {
		if (state == null) {
			throw new NullPointerException("Cannot add a null state"); //$NON-NLS-1$
		}

		if (states == null) {
			states = new HashMap(3);
		}
		states.put(stateId, state);
		state.addListener(this);
		handleStateChange(state, null);
	}

	public final State getState(final String stateId) {
		if ((states == null) || (states.isEmpty())) {
			return null;
		}

		return (State) states.get(stateId);
	}

	public final String[] getStateIds() {
		if ((states == null) || (states.isEmpty())) {
			return null;
		}

		final Set stateIds = states.keySet();
		return (String[]) stateIds.toArray(new String[stateIds.size()]);
	}

	/**
	 * <p>
	 * Removes a state from this handler. This will remove this handler as a
	 * listener to the state. No event is fired to notify the handler of this
	 * change.
	 * </p>
	 * <p>
	 * Clients may extend this method, but they should call this super method
	 * first before doing anything else.
	 * </p>
	 * 
	 * @param stateId
	 *            The identifier of the state to remove; must not be
	 *            <code>null</code>.
	 */
	public void removeState(final String stateId) {
		if (stateId == null) {
			throw new NullPointerException("Cannot remove a null state"); //$NON-NLS-1$
		}

		final State state = (State) states.get(stateId);
		if (state != null) {
			state.removeListener(this);
			if (states != null) {
				states.remove(state);
				if (states.isEmpty()) {
					states = null;
				}
			}
		}
	}
}
