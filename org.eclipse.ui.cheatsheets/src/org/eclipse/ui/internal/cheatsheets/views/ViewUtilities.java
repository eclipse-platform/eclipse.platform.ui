/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;

/**
 * Contains static functions used in cheat sheet display
 */
public class ViewUtilities {

	/**
	* Escape any ampersands used in a label
	**/
	public static String escapeForLabel(String message) {
		// Make the most common case - i.e. no ampersand the
		// most efficient
		if (message.indexOf('&') < 0) {
			return message;
		}
		
		int next = 0;
		StringBuffer result = new StringBuffer();
		int index = message.indexOf('&');
		while (index >= 0) {
			result.append(message.substring(next, index + 1));
			result.append('&');
			next = index + 1;
			index = message.indexOf('&', next);
		}
		result.append(message.substring(next));
		return result.toString();
	}
	
	/**
	 * Get the cheaetSheetView, opening it if necessary and making visible
	 * @return The cheat sheet view, or null if it could not be opened.
	 */
	public static CheatSheetView showCheatSheetView() {
		CheatSheetView view;
		IWorkbench workbench = CheatSheetPlugin.getPlugin().getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		view = (CheatSheetView) page.findView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
		if (view == null) {
			try {
				view = (CheatSheetView)page.showView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
				page.activate(view);
			} catch (PartInitException pie) {
				String message = Messages.LAUNCH_SHEET_ERROR;
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, pie);
				CheatSheetPlugin.getPlugin().getLog().log(status);
				org.eclipse.jface.dialogs.ErrorDialog.openError(window.getShell(), Messages.CHEAT_SHEET_ERROR_OPENING, null, pie.getStatus());
				return null;
			}
		}
		return view;
	}
    
}
