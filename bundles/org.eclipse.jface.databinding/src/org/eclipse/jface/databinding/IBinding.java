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

package org.eclipse.jface.databinding;

/**
 * The interface that represents a binding between a model and a target.
 * 
 * @since 3.2
 */
public interface IBinding {
	/**
	 * Add a listener to the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by this
	 * IBinding.
	 * 
	 * @param listener The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener);
	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by this
	 * IBinding.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener);
}
