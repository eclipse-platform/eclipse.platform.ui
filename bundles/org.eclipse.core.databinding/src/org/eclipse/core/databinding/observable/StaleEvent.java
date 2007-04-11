/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * Generic event denoting that the state of an {@link IObservable} object is
 * about to change. Note that this event is only fired when an observable
 * becomes stale, not when it becomes unstale; an observable that becomes
 * unstale should always fire a change event. Staleness can be used (for
 * example) to notify listeners when an observable has started a background
 * thread for updating its state. Clients can safely ignore staleness.
 * 
 * @see IObservable#isStale()
 * 
 * @since 1.0
 * 
 */
public class StaleEvent extends ObservableEvent {

	/**
	 * Creates a new stale event.
	 * 
	 * @param source
	 *            the source observable
	 */
	public StaleEvent(IObservable source) {
		super(source);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3491012225431471077L;

	static final Object TYPE = new Object();

	protected void dispatch(IObservablesListener listener) {
		((IStaleListener) listener).handleStale(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
