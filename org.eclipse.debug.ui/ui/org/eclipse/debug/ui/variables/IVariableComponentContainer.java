/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.variables;


import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Represents the API for an <code>IVariableComponent</code> to
 * access the visual component that contains it.
 * 
 * This interface is intended to be implemented by clients implementing
 * visual components that contain <code>IVariableComponent</code>s.
 * @see IVariableComponent
 */
public interface IVariableComponentContainer extends IMessageProvider {
	
	/**
	 * Sets the error message that corresponds to an IVariableComponent.
	 *
	 * @param errorMessage the message, or <code>null</code> to clear the
	 * message
	 */
	public void setErrorMessage(String errorMessage);

	/**
	 * Informs this container that it should update it's valid
	 * state. Typically called when the valid state of the
	 * contained <code>IVariableComponent</code> changes.
	 */
	public void updateValidState();
}
