/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.variables.details;

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Opens a dialog so that the user can enter the maximum length in characters that
 * the detail pane should display.
 * 
 * @see DetailPaneMaxLengthDialog
 * @since 3.0
 */
public class DetailPaneMaxLengthAction extends Action {
	
	private Shell fDialogShell;
	
	public DetailPaneMaxLengthAction(Shell dialogShell){
		super(ActionMessages.DetailPaneMaxLengthAction_0);
		fDialogShell = dialogShell;
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.DETAIL_PANE_MAX_LENGTH_ACTION);
		
	}
	
	public void run() {
		DetailPaneMaxLengthDialog dialog = new DetailPaneMaxLengthDialog(fDialogShell);
		dialog.open();
	}

}
