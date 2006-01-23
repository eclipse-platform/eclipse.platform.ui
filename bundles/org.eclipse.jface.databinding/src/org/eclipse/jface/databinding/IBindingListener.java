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
 * An interface for objects that need to listen to events that occur in the
 * data flow pipeline
 *  
 * @since 3.2
 */
public interface IBindingListener {
	/**
	 * Method bindingEvent.  The method that is called when something interesting
	 * occurs in the data flow pipeline.
	 * 
	 * @param e The IBindingEvent to handle.
	 * @return null if no error or an error message to abort the operation.  The
	 * error message will be propagated to the data binding context's error
	 * message updatable.
	 */
	public String bindingEvent(BindingEvent e);
	
}
