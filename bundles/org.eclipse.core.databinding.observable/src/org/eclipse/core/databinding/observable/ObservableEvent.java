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

import java.util.EventObject;

/**
 * Abstract event object for events fired by {@link IObservable} objects. All
 * events fired by observables must be derived from this class so that the way
 * of dispatching events can be improved in later versions of the framework.
 *
 * @since 1.0
 */
public abstract class ObservableEvent extends EventObject {

	/**
	 * Creates a new observable event.
	 *
	 * @param source source of this event
	 */
	public ObservableEvent(IObservable source) {
		super(source);
	}

	private static final long serialVersionUID = 7693906965267871813L;

	/**
	 * Returns the observable that generated this event.
	 *
	 * @return the observable that generated this event
	 */
	public IObservable getObservable() {
		return (IObservable) getSource();
	}

	/**
	 * Dispatch this event to the given listener. Subclasses must implement this
	 * method by calling the appropriate type-safe event handling method on the
	 * given listener according to the type of this event.
	 *
	 * @param listener
	 *            the listener that should handle the event
	 */
	protected abstract void dispatch(IObservablesListener listener);

	/**
	 * Returns a unique object used for distinguishing this event type from
	 * others.
	 *
	 * @return a unique object representing the concrete type of this event.
	 */
	protected abstract Object getListenerType();

}
