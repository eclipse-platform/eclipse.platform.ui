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
 * Represents the API for a group of visual components
 * to access the dialog page that contains it.
 * <p>
 * This interface is not intended to be extended
 * nor implemented by clients.
 * </p>
 */
public interface IGroupDialogPage extends IMessageProvider {
	
	/**
	 * Sets the error message for this page
	 *
	 * @param errorMessage the message, or <code>null</code> to clear the
	 * message
	 */
	public void setErrorMessage(String errorMessage);

	/**
	 * Updates the page's valid state using the group's
	 * current valid state. This will cause the dialog's
	 * buttons dependent on the page's valid state to
	 * update to reflect the new state.
	 */
	public void updateValidState();
}
