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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Edit the action sets.
 */
public class EditActionSetsAction  extends Action {
	private IWorkbenchWindow window;
/**
 * This default constructor allows the the action to be called from the welcome page.
 */
public EditActionSetsAction() {
	this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
}
/**
 * 
 */
public EditActionSetsAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("EditActionSetsAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("EditActionSetsAction.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	this.window = window;
	WorkbenchHelp.setHelp(this, IHelpContextIds.EDIT_ACTION_SETS_ACTION);
}
/**
 * Open the selected resource in the default page.
 */
public void run() {
	WorkbenchPage page = (WorkbenchPage)window.getActivePage();
	if (page == null)
		return;
	page.editActionSets();
}
}
