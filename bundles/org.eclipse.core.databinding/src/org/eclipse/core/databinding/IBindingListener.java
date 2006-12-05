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
 * An interface for objects that need to listen to events that occur in the data
 * flow pipeline
 * 
 * @since 1.0
 */
public interface IBindingListener {
	/**
	 * Method bindingEvent. The method that is called when something interesting
	 * occurs in the data flow pipeline.
	 * 
	 * @param e
	 *            The IBindingEvent to handle.
	 * @return a status object. To abort the operation, a status object with an
	 *         error or cancel status is returned. The error will be propagated
	 *         to the data binding context's error message updatable.
	 */
	public IStatus bindingEvent(BindingEvent e);

}
