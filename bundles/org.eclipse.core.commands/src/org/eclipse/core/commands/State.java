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

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.internal.commands.util.Util;

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
 * Clients may instantiate or extend this class.
 * </p>
 * 
 * @since 3.2
 */
public class State extends EventManager {

	/**
	 * The identifier of the state; may be <code>null</code> if it has not
	 * been initialized.
	 */
	private String id;

	/**
	 * The value held by this state; may be anything at all.
	 */
	private Object value;

	/**
	 * Adds a listener to changes for this state.
	 * 
	 * @param listener
	 *            The listener to add; must not be <code>null</code>.
	 */
	public void addListener(final IStateListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Disposes of this state. This allows the state to unregister itself with
	 * any managers or as a listener.
	 */
	public void dispose() {
		// The default implementation does nothing.
	}

	/**
	 * Notifies listeners to this state that it has changed in some way.
	 * 
	 * @param oldValue
	 *            The old value; may be anything.
	 */
	protected final void fireStateChanged(final Object oldValue) {
		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IStateListener listener = (IStateListener) listeners[i];
			listener.handleStateChange(this, oldValue);
		}
	}

	/**
	 * Returns the identifier for this state.
	 * 
	 * @return The id; may be <code>null</code>.
	 */
	public final String getId() {
		return id;
	}

	/**
	 * The current value associated with this state. This can be any type of
	 * object, but implementations will usually restrict this value to a
	 * particular type.
	 * 
	 * @return The current value; may be anything.
	 */

	public Object getValue() {
		return value;
	}

	/**
	 * Removes a listener to changes from this state.
	 * 
	 * @param listener
	 *            The listener to remove; must not be <code>null</code>.
	 */

	public void removeListener(final IStateListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Sets the identifier for this object.  This method should only be called
	 * by the command framework.  Clients should not call this method.
	 * 
	 * @param id
	 *            The id; must not be <code>null</code>.
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the value for this state object.
	 * 
	 * @param value
	 *            The value to set; may be anything.
	 */
	public void setValue(final Object value) {
		if (!Util.equals(this.value, value)) {
			final Object oldValue = this.value;
			this.value = value;
			fireStateChanged(oldValue);
		}
	}
}
