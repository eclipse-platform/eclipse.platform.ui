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
package org.eclipse.ui.tests.cheatsheets.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.cheatsheets.*;

public class NavigateAction extends Action implements ICheatSheetAction {

	static void go( String newSheet ) {
		System.out.println( "Test.go newSheet:(" + newSheet + ")" );
		final String finalNewSheet = newSheet;
		try {
			Display.getCurrent().asyncExec( new Runnable() {
				public void run() {
					OpenCheatSheetAction csAction = new OpenCheatSheetAction( finalNewSheet );
					csAction.run();
				}
			} );
		}
		catch( Exception ex ) { }
	}

	public void run( String[] params, ICheatSheetManager manager ) {
		System.out.println( "Test.run manager:(" + manager.toString() + ")" );
		System.out.println( "Test.run sheetID:(" + manager.getCheatSheetID() + ")" );
		go( params[ 0 ] );
	}
}
