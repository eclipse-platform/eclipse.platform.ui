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
package org.eclipse.ui.tests.cheatsheets.parameters;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.cheatsheets.*;

public class GatherDisplayDataAction extends Action implements ICheatSheetAction {
	private ICheatSheetManager csmanager;
	/**
	 * The constructor.
	 */
	public GatherDisplayDataAction() {
	}
	
	public void run(String[] s, ICheatSheetManager csm){
		csmanager = csm;

		try {
			Shell shell = Display.getCurrent().getActiveShell();
			Dialog dialog = null;
			if(s[0].equals("gather")) {
				dialog = new GatherDataDialog(shell, csmanager);
			} else { 
				dialog = new DisplayDataDialog(shell, csmanager, s);
			}
			dialog.create();
			int result = dialog.open();
			notifyResult(result == Window.OK ? true : false);
		} catch(Exception e) {
			
		}
	}
}