/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.internal.cheatsheets.actions.CheatSheetCategoryBasedSelectionAction;

/**
 * Opens the cheatsheet identified by the parameter, or if no parameter is given
 * opens the dialog that allows the user to choose a cheatsheet.
 * 
 * @since 3.2
 */
public class OpenCheatSheetHandler extends AbstractHandler {

	private static final String PARAM_ID_CHEAT_SHEET_ID = "cheatSheetId"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String cheatSheetId = event.getParameter(PARAM_ID_CHEAT_SHEET_ID);

		if (cheatSheetId == null) {
			CheatSheetCategoryBasedSelectionAction action = new CheatSheetCategoryBasedSelectionAction();
			action.run();
		} else {
			OpenCheatSheetAction action = new OpenCheatSheetAction(cheatSheetId);
			action.run();
		}

		return null;
	}

}
