/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets.events;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;

/**
 *  <p>This is the interface for a cheat sheet event.  Cheat sheet item and view events
 *  are fired by the cheat sheets view to any registered listener classes implementing 
 *  either ICheatSheetItemListener or ICheatSheetViewListener interfaces.  The event
 * type may be accessed, as well as the id of the cheat sheet that was open when the 
 * event was fired.  A handle to the ICheatSheetManager is available from cheat sheet events.</p>
 */
public interface ICheatSheetEvent {
	
	/**
	 * This method returns an integer that corresponds to an event type.
	 * See ICheatSheetviewEvent and ICheatSheetItemEvent for the integer codes of event types.
	 * @return the event code
	 */
	public int getCheatSheetEventType();
	/**
	 * This method returns the id of the cheat sheet that generated the event.
	 * @return the id of the cheat sheet that fired the event
	 */
	public String getCheatSheetID();
	/**
	 * This method returns a handle to the implementation of ICheatSheetManager.
	 * @return a reference to the ICheatSheetManager
	 */
	public ICheatSheetManager getCheatSheetManager();

}
