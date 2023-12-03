/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.core.databinding.observable;

/**
 * Generic change event denoting that the state of an {@link IObservable} object
 * has changed. This event does not carry information about the kind of change
 * that occurred.
 *
 * @since 1.0
 */
public class ChangeEvent extends ObservableEvent {

	private static final long serialVersionUID = -3241193109844979384L;
	static final Object TYPE = new Object();

	/**
	 * Creates a new change event object.
	 *
	 * @param source
	 *            the observable that changed state
	 */
	public ChangeEvent(IObservable source) {
		super(source);
	}

	@Override
	protected void dispatch(IObservablesListener listener) {
		((IChangeListener) listener).handleChange(this);
	}

	@Override
	protected Object getListenerType() {
		return TYPE;
	}

}
