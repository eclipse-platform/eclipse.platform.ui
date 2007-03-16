/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.ObservableEvent;

/**
 * Value changing event describing a pending change of an
 * {@link IObservableValue} object's current value. Listeners can veto the
 * pending change by setting {@link #veto} to <code>true</code>.
 * 
 * @since 1.0
 * 
 */
public class ValueChangingEvent extends ObservableEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2305345286999701156L;

	static final Object TYPE = new Object();

	/**
	 * Description of the change to the source observable value. Listeners must
	 * not change this field.
	 */
	public ValueDiff diff;

	/**
	 * Flag for vetoing this change. Default value is <code>false</code>, can
	 * be set to <code>true</code> by listeners to veto this change.
	 */
	public boolean veto = false;

	/**
	 * Creates a new value changing event.
	 * 
	 * @param source
	 *            the source observable value
	 * @param diff
	 *            the value change
	 */
	public ValueChangingEvent(IObservableValue source, ValueDiff diff) {
		super(source);
		this.diff = diff;
	}

	/**
	 * @return the observable value from which this event originated
	 */
	public IObservableValue getObservableValue() {
		return (IObservableValue) source;
	}

	protected void dispatch(IObservablesListener listener) {
		((IValueChangingListener) listener).handleValueChanging(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
