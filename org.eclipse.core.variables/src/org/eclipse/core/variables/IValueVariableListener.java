/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.variables;

/**
 * A string variable listener is notified of variables as they are added
 * and removed from the string variable manager. As well, listeners are
 * notified when a value variable changes value.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 */
public interface IValueVariableListener {
	
	/**
	 * Notification the given variables have been added to the variable
	 * manager.
	 * 
	 * @param variables added variables
	 */
	public void variablesAdded(IValueVariable[] variables);
	
	/**
	 * Notification the given variables have been removed from the variable
	 * manager.
	 * 
	 * @param variables removed variables
	 */
	public void variablesRemoved(IValueVariable[] variables);
	
	/**
	 * Notification the given variables have been changed value.
	 * 
	 * @param variables changed variables
	 */
	public void variablesChanged(IValueVariable[] variables);

}
