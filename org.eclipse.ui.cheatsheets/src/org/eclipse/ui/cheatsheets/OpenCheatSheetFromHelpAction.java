/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;

/**
 * <p>This action class can be used to launch a cheat sheet from the eclipse help system
 * using a live help link.  To use this action, the initialization string must correspond to the id of a 
 * cheat sheet that has been declared using the cheatsheetContent extension point.
 * See ILiveHelpAction for further details on how to run an action from eclipse help pages. </p>
 * 
 * @since 3.0
 */
public class OpenCheatSheetFromHelpAction implements ILiveHelpAction {
	private String cheatsheetID;

	/**
	 * Constructor for OpenCheatSheetFromHelpAction.
	 */
	public OpenCheatSheetFromHelpAction() {
		super();
	}

	/**
	 * This method is called by the eclipse framework.  The initialization string must be the id of a 
	 * registered cheat sheet in order for the action to work.
	 * @see org.eclipse.help.ILiveHelpAction#setInitializationString(String)
	 */
	public void setInitializationString(String data) {
		cheatsheetID = data;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Active help does not run on the UI thread, so we must use syncExec
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				new LaunchCheatSheetAction(cheatsheetID).run(); //$NON-NLS-1$
			}
		});
	}
}
