/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 */
public class EditBookmarksAction extends Action {

	public EditBookmarksAction() {
		setText(UpdateUI.getString("EditBookmarksAction.edit")); //$NON-NLS-1$
	}
	
	public void run() {
		WebBookmarksDialog dialog = new WebBookmarksDialog(UpdateUI.getActiveWorkbenchShell());
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString("EditBookmarksAction.title")); //$NON-NLS-1$
		dialog.getShell().setSize(300,300);
		dialog.open();
	}


}
