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

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;

/**
 * Listener for generic change events. Note that the change events do not carry information about
 * the change, they only specify the affected observable. To listen for specific change events, use
 * more specific change listeners.
 * 
 *  @see IValueChangeListener
 *  @see IListChangeListener
 *  @see ISetChangeListener
 *  @see IMapChangeListener
 * 
 * @since 1.0
 */
public interface IChangeListener {
	
	/**
	 * Handle a generic change to the given observable.
	 * 
	 * @param source
	 */
	public void handleChange(IObservable source);

}
