/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 146397)
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * Event denoting that an {@link IObservable} object was disposed.
 * 
 * @since 1.2
 */
public class DisposeEvent extends ObservableEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3241193109844979384L;

	static final Object TYPE = new Object();

	/**
	 * Creates a new dispose event object.
	 * 
	 * @param source
	 *            the observable that was disposed
	 */
	public DisposeEvent(IObservable source) {
		super(source);
	}

	protected void dispatch(IObservablesListener listener) {
		((IDisposeListener) listener).handleDispose(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}
}
