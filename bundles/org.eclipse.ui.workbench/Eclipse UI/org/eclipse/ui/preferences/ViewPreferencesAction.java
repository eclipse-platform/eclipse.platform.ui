/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.preferences;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The ViewPreferencesAction is the action for opening
 * a view preferences dialog on a class.
 */
public abstract class ViewPreferencesAction extends Action {

	/**
	 * Create a new instance of the receiver.
	 */
	public ViewPreferencesAction() {
		super(WorkbenchMessages.getString("OpenPreferences.text")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		openViewPreferencesDialog();
	}

	/**
	 * Open a view preferences dialog for the receiver.
	 */
	public abstract void openViewPreferencesDialog();

}
