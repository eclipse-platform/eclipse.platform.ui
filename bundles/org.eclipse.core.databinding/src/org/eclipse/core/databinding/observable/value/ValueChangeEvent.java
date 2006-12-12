/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 * @since 3.3
 *
 */
public class ValueChangeEvent extends ObservableEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2305345286999701156L;

	static final Object TYPE = new Object();

	/**
	 * 
	 */
	public ValueDiff diff;

	/**
	 * @param source
	 * @param diff 
	 */
	public ValueChangeEvent(IObservableValue source, ValueDiff diff) {
		super(source);
		this.diff = diff;
	}
	
	/**
	 * @return
	 */
	public IObservableValue getObservableValue() {
		return (IObservableValue) source;
	}

	protected void dispatch(IObservablesListener listener) {
		((IValueChangeListener)listener).handleValueChange(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
