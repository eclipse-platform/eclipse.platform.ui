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
import org.eclipse.ui.internal.cheatsheets.dialogs.CheatSheetCategoryBasedSelectionDialog;
import org.eclipse.ui.internal.cheatsheets.registry.*;

/**
 * Action to programmatically open the CheatSheet selection dialog.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 */
public class CheatSheetCategoryBasedSelectionAction extends Action {

	/**
	 * Create a new <code>CheatSheetSelectionAction</code> action.
	 */
	public CheatSheetCategoryBasedSelectionAction() {
	}

	/**
	 * Constructor for CheatSheetSelectionAction.
	 * @param text
	 */
	public CheatSheetCategoryBasedSelectionAction(String text) {
		super(text);
	}

	/**
	 * Constructor for CheatSheetSelectionAction.
	 * @param text
	 * @param image
	 */
	public CheatSheetCategoryBasedSelectionAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		CheatSheetCollectionElement cheatSheets = (CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();

		CheatSheetCategoryBasedSelectionDialog dialog = new CheatSheetCategoryBasedSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), cheatSheets);

		if(dialog.open() != Window.OK || dialog.getResult().length != 1)
			return;

		CheatSheetElement result = (CheatSheetElement)dialog.getResult()[0];

		new LaunchCheatSheetAction(result.getID()).run(); //$NON-NLS-1$
	}
}

