/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.dialogs.CheatSheetSelectionDialog;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;

/**
 * Action to programmatically open the CheatSheet selection dialog.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 */
public class CheatSheetSelectionAction extends Action {

	/**
	 * Create a new <code>CheatSheetSelectionAction</code> action.
	 */
	public CheatSheetSelectionAction() {
	}

	/**
	 * Constructor for CheatSheetSelectionAction.
	 * @param text
	 */
	public CheatSheetSelectionAction(String text) {
		super(text);
	}

	/**
	 * Constructor for CheatSheetSelectionAction.
	 * @param text
	 * @param image
	 */
	public CheatSheetSelectionAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		CheatSheetSelectionDialog dialog = new CheatSheetSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

		if(dialog.open() != Window.OK || dialog.getResult().length != 1)
			return;

		new LaunchCheatSheetAction(((CheatSheetElement)dialog.getResult()[0]).getID()).run(); //$NON-NLS-1$
	}
}

