/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.cheatsheets.events.ICheatSheetItemEvent;

public class CheatSheetItemEvent extends CheatSheetEvent implements ICheatSheetItemEvent {
	
	String fCheatsheetItemID;
	String fCheatsheetSubItemID;
	
	/**
	 * 
	 */
	public CheatSheetItemEvent() {
		super();
	}

	/**
	 * create new event.
	 * subItemID can be null if it is not an event from a sub item.
	 */
	public CheatSheetItemEvent(int eventType, String id, String itemid, String subItemID, ICheatSheetManager csm) {
		super();
		this.fCheatsheetEventType = eventType;
		this.fCheatsheetID = id;
		this.fCheatsheetItemID = itemid;
		this.fCheatsheetSubItemID = subItemID;
		this.csm = csm;
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

	/**
	 * @return
	 */
	public String getCheatSheetItemID() {
		return fCheatsheetItemID;
	}

	/**
	 * @return
	 */
	public String getCheatSheetSubItemID() {
		return fCheatsheetSubItemID;
	}
	
	/* (non-Javadoc)
		 * @see org.eclipse.ui.cheatsheets.events.ICheatSheetEvent#getCheatsheetManager()
		 */
		public ICheatSheetManager getCheatSheetManager() {
			return csm;
		}

}
