/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.cheatsheets.events.ICheatSheetViewEvent;

public class CheatSheetViewEvent extends CheatSheetEvent implements ICheatSheetViewEvent {

	public CheatSheetViewEvent(int eventType, String id, ICheatSheetManager csm) {
		super();
		this.csm = csm;
		this.fCheatsheetEventType = eventType;
		this.fCheatsheetID = id;
	}

	/**
	 * @return
	 */
	public int getCheatSheetEventType() {
		return fCheatsheetEventType;
	}

	/**
	 * @return
	 */
	public String getCheatSheetID() {
		return fCheatsheetID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.events.ICheatSheetEvent#getCheatSheetManager()
	 */
	public ICheatSheetManager getCheatSheetManager() {
		return csm;
	}

}
