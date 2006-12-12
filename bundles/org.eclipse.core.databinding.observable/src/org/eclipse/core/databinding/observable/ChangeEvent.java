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
public class ChangeEvent extends ObservableEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3241193109844979384L;
	static final Object TYPE = new Object();

	/**
	 * @param source
	 */
	public ChangeEvent(IObservable source) {
		super(source);
	}

	protected void dispatch(IObservablesListener listener) {
		((IChangeListener) listener).handleChange(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
