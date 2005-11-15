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

import org.eclipse.core.commands.util.ListenerList;
import org.eclipse.core.internal.commands.util.Util;

/**
 * <p>
 * An abstract implementation of {@link IState} that provides good default
 * implementations for all of the methods. It is recommended that developers
 * extend this class, rather than implementing {@link IState} themselves.
 * </p>
 * <p>
 * Clients may extend this class.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class AbstractState implements IState {

	/**
	 * The listeners to this piece of state. If there are no listeners, this
	 * value is <code>null</code>.
	 */
	private ListenerList listenerList;

	/**
	 * The piece of state to store. This may be anything at all.
	 */
	private Object value;

	public final void addListener(final IStateListener listener) {
		if (listenerList == null) {
			listenerList = new ListenerList(1);
		}

		listenerList.add(listener);
	}

	public void dispose() {
		// The default implementation does nothing.
	}

	/**
	 * Notifies listeners to this state that it has changed in some way.
	 * 
	 * @param oldValue
	 *            The old value; may be anything.
	 */
	protected final void fireHandlerStateChanged(final Object oldValue) {
		if (listenerList != null) {
			final Object[] listeners = listenerList.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final IStateListener listener = (IStateListener) listeners[i];
				listener.handleStateChange(this, oldValue);
			}
		}
	}

	public Object getValue() {
		return value;
	}

	public void setValue(final Object value) {
		if (!Util.equals(this.value, value)) {
			final Object oldValue = this.value;
			this.value = value;
			fireHandlerStateChanged(oldValue);
		}
	}

	public final void removeListener(final IStateListener listener) {
		if (listenerList != null) {
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				listenerList = null;
			}
		}
	}
}
