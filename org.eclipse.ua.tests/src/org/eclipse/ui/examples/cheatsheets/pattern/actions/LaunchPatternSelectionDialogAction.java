/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.cheatsheets.pattern.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.examples.cheatsheets.pattern.dialogs.PatternSelectionDialog;

public class LaunchPatternSelectionDialogAction extends Action implements ICheatSheetAction {
	private ICheatSheetManager csmanager;
	/**
	 * The constructor.
	 */
	public LaunchPatternSelectionDialogAction() {
	}
	
	public void run(String[] s, ICheatSheetManager csm){
		csmanager = csm;

		try {
			Shell shell = Display.getCurrent().getActiveShell();
			PatternSelectionDialog p = new PatternSelectionDialog(shell);
			p.setCSM(csmanager);
			p.create();
			p.open();
		} catch(Exception e) {
			
		}
	}
}