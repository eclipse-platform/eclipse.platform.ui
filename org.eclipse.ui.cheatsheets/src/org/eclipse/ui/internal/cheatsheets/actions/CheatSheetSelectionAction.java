/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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

		if(dialog.open() != Window.OK || dialog.getResult().length != 1) {
			notifyResult(false);
			return;
		}

		notifyResult(true);

		new OpenCheatSheetAction(((CheatSheetElement)dialog.getResult()[0]).getID()).run();
	}
}

