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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

/**
 * Sets the console to display output for the current/selected process.
 */
public class ShowCurrentProcessAction extends Action {
	
	private ConsoleView fView;

	public ShowCurrentProcessAction(ConsoleView view) {
		fView = view;
		setText(ActionMessages.getString("ShowCurrentProcessAction.Show_Selected/Current_Process_1")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fView.setMode(ConsoleView.MODE_CURRENT_PROCESS);
		fView.setViewerInput(DebugUITools.getCurrentProcess());
	}

}
