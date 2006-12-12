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

import java.util.EventObject;

/**
 * @since 3.3
 * 
 */
public abstract class ObservableEvent extends EventObject {

	/**
	 * @param source
	 */
	public ObservableEvent(IObservable source) {
		super(source);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7693906965267871813L;

	/**
	 * @return
	 */
	public IObservable getObservable() {
		return (IObservable) getSource();
	}

	/**
	 * 
	 * @param listener
	 */
	protected abstract void dispatch(IObservablesListener listener);

	/**
	 * @return
	 */
	protected abstract Object getListenerType();

}
