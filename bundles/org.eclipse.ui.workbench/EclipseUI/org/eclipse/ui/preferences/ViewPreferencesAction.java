/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.preferences;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The ViewPreferencesAction is the action for opening a view preferences dialog
 * on a class.
 *
 * @since 3.1
 */
public abstract class ViewPreferencesAction extends Action {

	/**
	 * Create a new instance of the receiver.
	 */
	public ViewPreferencesAction() {
		super(WorkbenchMessages.OpenPreferences_text);
	}

	@Override
	public void run() {
		openViewPreferencesDialog();
	}

	/**
	 * Open a view preferences dialog for the receiver.
	 */
	public abstract void openViewPreferencesDialog();

}
