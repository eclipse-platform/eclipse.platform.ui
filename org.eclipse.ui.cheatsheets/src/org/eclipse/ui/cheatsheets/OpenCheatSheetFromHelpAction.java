/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;

/**
 * <p>This action class can be used to launch a cheat sheet from the eclipse help system
 * using a live help link.  To use this action, the initialization string must correspond to the id of a 
 * cheat sheet that has been declared using the cheatsheetContent extension point.
 * See ILiveHelpAction for further details on how to run an action from eclipse help pages. </p>
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
