/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

public class CheatSheetEvent implements ICheatSheetEvent {
	int type;
	String cheatsheetID;
	ICheatSheetManager csm;


	public CheatSheetEvent(int eventType, String id, ICheatSheetManager csm) {
		super();
		this.csm = csm;
		this.type = eventType;
		this.cheatsheetID = id;
	}

	/**
	 * @return
	 */
	public int getEventType() {
		return type;
	}

	/**
	 * @return
	 */
	public String getCheatSheetID() {
		return cheatsheetID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.events.ICheatSheetEvent#getCheatSheetManager()
	 */
	public ICheatSheetManager getCheatSheetManager() {
		return csm;
	}

}
