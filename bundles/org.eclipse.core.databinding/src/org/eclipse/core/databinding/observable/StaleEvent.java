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

package org.eclipse.core.databinding.observable;

/**
 * @since 3.3
 * 
 */
public class StaleEvent extends ObservableEvent {

	/**
	 * @param source
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
		((IStaleListener)listener).handleStale(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
