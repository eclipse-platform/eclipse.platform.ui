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

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;

/**
 * <p>
 * A named handle object that can carry state with it. This state can be used to
 * override the name or description.
 * </p>
 * <p>
 * Clients may neither instantiate nor extend this class.
 * </p>
 * 
 * @since 3.2
 */
abstract class NamedHandleObjectWithState extends NamedHandleObject implements
		IObjectWithState {

	/**
	 * An empty string array, which can be returned from {@link #getStateIds()}
	 * if there is no state.
	 */
	private static final String[] NO_STATE = new String[0];

	/**
	 * The map of states currently held by this command. If this command has no
	 * state, then this will be <code>null</code>.
	 */
	private Map states = null;

	/**
	 * Constructs a new instance of <code>NamedHandleObject<WithState/code>.
	 * 
	 * @param id
	 *            The identifier for this handle; must not be <code>null</code>.
	 */
	protected NamedHandleObjectWithState(final String id) {
		super(id);
	}

	public void addState(final String stateId, final State state) {
		if (state == null) {
			throw new NullPointerException("Cannot add a null state"); //$NON-NLS-1$
		}

		if (states == null) {
			states = new HashMap(3);
		}
		states.put(stateId, state);
	}

	public final String getDescription() throws NotDefinedException {
		final String description = super.getDescription(); // Trigger a NDE.

		final State descriptionState = getState(INamedHandleStateIds.DESCRIPTION);
		if (descriptionState != null) {
			final Object value = descriptionState.getValue();
			if (value != null) {
				return value.toString();
			}
		}

		return description;
	}

	public final String getName() throws NotDefinedException {
		final String name = super.getName(); // Trigger a NDE, if necessary.

		final State nameState = getState(INamedHandleStateIds.NAME);
		if (nameState != null) {
			final Object value = nameState.getValue();
			if (value != null) {
				return value.toString();
			}
		}

		return name;
	}

	public final State getState(final String stateId) {
		if ((states == null) || (states.isEmpty())) {
			return null;
		}

		return (State) states.get(stateId);
	}

	public final String[] getStateIds() {
		if ((states == null) || (states.isEmpty())) {
			return NO_STATE;
		}

		final Set stateIds = states.keySet();
		return (String[]) stateIds.toArray(new String[stateIds.size()]);
	}

	public void removeState(final String id) {
		if (id == null) {
			throw new NullPointerException("Cannot remove a null id"); //$NON-NLS-1$
		}

		if (states != null) {
			states.remove(id);
			if (states.isEmpty()) {
				states = null;
			}
		}
	}

}
