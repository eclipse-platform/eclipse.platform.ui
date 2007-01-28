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

package org.eclipse.core.databinding;

import org.eclipse.core.runtime.IStatus;

/**
 * An interface for objects that need to listen to events that occur while
 * synchronizing the state of two observables in a binding.
 * 
 * @since 1.0
 */
public interface IBindingListener {
	/**
	 * This method is called when a binding event occurred.
	 * 
	 * @param e
	 *            The IBindingEvent to handle.
	 * @return a status object. To abort the operation, a status object with an
	 *         error or cancel status is returned. The error will be propagated
	 *         to the data binding context's error message updatable.
	 */
	public IStatus handleBindingEvent(BindingEvent e);

}
