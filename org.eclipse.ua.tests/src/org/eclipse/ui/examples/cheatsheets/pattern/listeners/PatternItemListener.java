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
package org.eclipse.ui.examples.cheatsheets.pattern.listeners;

import org.eclipse.ui.cheatsheets.*;

public class PatternItemListener extends CheatSheetListener {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.CheatSheetListener#cheatSheetEvent(org.eclipse.ui.cheatsheets.ICheatSheetEvent)
	 */
	public void cheatSheetEvent(ICheatSheetEvent event) {
		ICheatSheetManager csm = event.getCheatSheetManager();
		System.out.print("CheatSheetEvent for ");
		System.out.println(event.getCheatSheetID());
		System.out.print("Event type: ");
		System.out.print(event.getEventType());
		System.out.print(" - ");
		switch (event.getEventType()) {
			case ICheatSheetEvent.CHEATSHEET_OPENED :
				System.out.println("OPENED");
				break;
			case ICheatSheetEvent.CHEATSHEET_CLOSED :
				System.out.println("CLOSED");
				break;
			case ICheatSheetEvent.CHEATSHEET_STARTED :
				System.out.println("STARTED");
				break;
			case ICheatSheetEvent.CHEATSHEET_RESTARTED :
				System.out.println("RESTARTED");
				break;
			case ICheatSheetEvent.CHEATSHEET_COMPLETED :
				System.out.println("COMPLETED");
				break;
			case ICheatSheetEvent.CHEATSHEET_RESTORED :
				System.out.println("RESTORED");
				break;
			default :
				System.out.println("UNKNOWN");
				break;
		}
	}
}
