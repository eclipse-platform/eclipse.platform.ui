/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.cheatsheets.actions;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.*;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.*;

/**
 * Action to programmatically open a perspective from a cheat sheet.
 * The perspective id must be passed to the action via param1.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 */
public class OpenPerspective extends Action implements ICheatSheetAction {

	/**
	 * Create a new <code>OpenPerspective</code> action.
	 */
	public OpenPerspective() {
	}


	/**
	 * @see Action#run()
	 */
	@Override
	public void run(String[] params, ICheatSheetManager manager) {
		try {
			if(params == null || params[0] == null) {
				return;
			}

			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			IPerspectiveDescriptor perspective = workbench.getPerspectiveRegistry().findPerspectiveWithId(params[0]);
			page.setPerspective(perspective);
		} catch(Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, Messages.ERROR_OPENING_PERSPECTIVE, null);
			CheatSheetPlugin.getPlugin().getLog().log(status);
		}
	}
}
