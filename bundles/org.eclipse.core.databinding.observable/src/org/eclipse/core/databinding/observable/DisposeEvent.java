/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	protected void dispatch(IObservablesListener listener) {
		((IDisposeListener) listener).handleDispose(this);
	}

	@Override
	protected Object getListenerType() {
		return TYPE;
	}
}
