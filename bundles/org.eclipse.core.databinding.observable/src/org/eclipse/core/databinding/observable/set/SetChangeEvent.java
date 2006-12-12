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

package org.eclipse.core.databinding.observable.set;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.ObservableEvent;

/**
 * @since 3.3
 * 
 */
public class SetChangeEvent extends ObservableEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7436547103857482256L;
	static final Object TYPE = new Object();
	/**
	 * 
	 */
	public SetDiff diff;

	/**
	 * @param source
	 * @param diff
	 */
	public SetChangeEvent(IObservableSet source, SetDiff diff) {
		super(source);
		this.diff = diff;
	}
	
	/**
	 * @return
	 */
	public IObservableSet getObservableSet() {
		return (IObservableSet) getSource();
	}

	protected void dispatch(IObservablesListener listener) {
		((ISetChangeListener)listener).handleSetChange(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
