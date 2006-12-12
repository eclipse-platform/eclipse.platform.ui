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

package org.eclipse.core.databinding.observable.list;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.ObservableEvent;

/**
 * @since 3.3
 * 
 */
public class ListChangeEvent extends ObservableEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9154315534258776672L;
	
	static final Object TYPE = new Object();
	/**
	 * 
	 */
	public ListDiff diff;

	/**
	 * @param source
	 * @param diff
	 */
	public ListChangeEvent(IObservableList source, ListDiff diff) {
		super(source);
		this.diff = diff;
	}

	/**
	 * @return
	 */
	public IObservableList getObservableList() {
		return (IObservableList) getSource();
	}

	protected void dispatch(IObservablesListener listener) {
		((IListChangeListener)listener).handleListChange(this);
	}

	protected Object getListenerType() {
		return TYPE;
	}

}
